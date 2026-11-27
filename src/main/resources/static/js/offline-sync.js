(function (global) {
  'use strict';

  const HEARTBEAT_INTERVAL_MS = 7000;
  const HEARTBEAT_TIMEOUT_MS = 3000;
  const CONTEXT_PATH = (document.querySelector('meta[name="ctx"]') || {}).content || '';
  let serverOnline = null;
  let heartbeatTimer = null;
  let replaying = false;
  let stoppedForAuthentication = false;

  function endpoint(path) {
    return CONTEXT_PATH + path;
  }

  function emit(name, detail) {
    global.dispatchEvent(new CustomEvent(name, {detail: detail || {}}));
  }

  function setServerStatus(online) {
    if (serverOnline === online) return;
    serverOnline = online;
    emit('offline:server-status', {online: online});
    if (online) {
      emit('offline:server-online');
    } else {
      emit('offline:server-offline');
    }
  }

  function heartbeat() {
    const controller = new AbortController();
    const timeout = global.setTimeout(function () {
      controller.abort();
    }, HEARTBEAT_TIMEOUT_MS);
    return fetch(endpoint('/actuator/health'), {
      method: 'GET',
      cache: 'no-store',
      credentials: 'same-origin',
      signal: controller.signal,
      headers: {'Accept': 'application/json'}
    }).then(function (response) {
      setServerStatus(response.ok);
      if (response.ok) syncNow();
      return response.ok;
    }).catch(function () {
      setServerStatus(false);
      return false;
    }).finally(function () {
      global.clearTimeout(timeout);
    });
  }

  function getCsrf() {
    return fetch(endpoint('/api/csrf'), {
      method: 'GET',
      cache: 'no-store',
      credentials: 'same-origin',
      headers: {'Accept': 'application/json'}
    }).then(function (response) {
      if (response.status === 401) {
        const error = new Error('Phiên đăng nhập hết hạn. Hãy đăng nhập lại để đồng bộ.');
        error.authenticationRequired = true;
        throw error;
      }
      if (!response.ok) throw new Error('Không lấy được CSRF token mới.');
      return response.json();
    }).then(function (csrf) {
      if (!csrf || !csrf.parameterName || !csrf.token) {
        throw new Error('Phản hồi CSRF thiếu parameterName hoặc token.');
      }
      return csrf;
    });
  }

  function setCsrf(payload, csrf, clientRequestId) {
    if (payload && payload.kind === 'form') {
      const entries = payload.entries.filter(function (entry) {
        return entry[0] !== csrf.parameterName && entry[0] !== 'clientRequestId';
      });
      entries.push([csrf.parameterName, csrf.token]);
      entries.push(['clientRequestId', clientRequestId]);
      return {kind: 'form', entries: entries};
    }
    if (payload && payload.kind === 'json') {
      return {
        kind: 'json',
        value: Object.assign({}, payload.value, {clientRequestId: clientRequestId})
      };
    }
    return payload;
  }

  function requestUrl(path) {
    const url = new URL(path, global.location.origin);
    if (url.origin !== global.location.origin) {
      throw new Error('Chỉ có thể đồng bộ request đến máy chủ hiện tại.');
    }
    if (CONTEXT_PATH && url.pathname !== CONTEXT_PATH
        && !url.pathname.startsWith(CONTEXT_PATH + '/')) {
      url.pathname = CONTEXT_PATH + url.pathname;
    }
    return url.toString();
  }

  function sendItem(item, csrf) {
    const payload = setCsrf(item.payload, csrf, item.clientRequestId);
    const body = global.OfflineDB.payloadToBody(payload);
    const headers = {
      'Accept': 'application/json',
      [csrf.headerName || 'X-CSRF-TOKEN']: csrf.token,
      'X-Client-Request-Id': item.clientRequestId
    };
    if (payload && payload.kind === 'json') headers['Content-Type'] = 'application/json';
    if (payload && payload.kind === 'text') headers['Content-Type'] = 'text/plain;charset=UTF-8';

    return fetch(requestUrl(item.url), {
      method: item.method,
      credentials: 'same-origin',
      headers: headers,
      body: body
    });
  }

  function sendItemWithCsrfRefresh(item, csrf) {
    return sendItem(item, csrf).then(function (response) {
      if (response.status !== 403) return {response: response, csrf: csrf};
      return getCsrf().then(function (refreshedCsrf) {
        return sendItem(item, refreshedCsrf).then(function (retryResponse) {
          return {response: retryResponse, csrf: refreshedCsrf};
        });
      });
    });
  }

  function responseMessage(response) {
    return response.clone().json().then(function (body) {
      return body && (body.message || body.error || body.code)
        ? String(body.message || body.error || body.code)
        : 'Máy chủ từ chối thao tác (' + response.status + ').';
    }).catch(function () {
      return 'Máy chủ từ chối thao tác (' + response.status + ').';
    });
  }

  function syncNow() {
    if (!global.OfflineDB || replaying || stoppedForAuthentication || !serverOnline) {
      return Promise.resolve(false);
    }
    replaying = true;
    emit('offline:sync-started');
    return global.OfflineDB.pending().then(function (items) {
      if (items.length === 0) return true;
      return getCsrf().then(function (csrf) {
        return items.reduce(function (chain, item) {
          return chain.then(function (state) {
            if (!state.shouldContinue) return state;
            return sendItemWithCsrfRefresh(item, state.csrf).then(function (result) {
              const response = result.response;
              if (response.status === 200 || response.status === 201 || response.status === 204) {
                return global.OfflineDB.remove(item.id).then(function () {
                  emit('offline:sync-item', {
                    item: item,
                    status: 'synced',
                    responseStatus: response.status
                  });
                  return {shouldContinue: true, csrf: result.csrf};
                });
              }
              if (response.status === 401) {
                stoppedForAuthentication = true;
                emit('offline:authentication-required', {item: item});
                return {shouldContinue: false, csrf: result.csrf};
              }
              return responseMessage(response).then(function (message) {
                if (response.status === 409) {
                  return global.OfflineDB.update(item.id, {
                    status: 'conflict',
                    errorMsg: message
                  }).then(function () {
                    emit('offline:sync-item', {
                      item: item,
                      status: 'conflict',
                      responseStatus: response.status,
                      errorMsg: message
                    });
                    return {shouldContinue: true, csrf: result.csrf};
                  });
                }
                if (response.status >= 400 && response.status < 500) {
                  return global.OfflineDB.update(item.id, {
                    status: 'error',
                    errorMsg: message
                  }).then(function () {
                    emit('offline:sync-item', {
                      item: item,
                      status: 'error',
                      responseStatus: response.status,
                      errorMsg: message
                    });
                    return {shouldContinue: true, csrf: result.csrf};
                  });
                }
                emit('offline:sync-paused', {
                  item: item,
                  errorMsg: message,
                  responseStatus: response.status
                });
                return {shouldContinue: false, csrf: result.csrf};
              });
            }).catch(function (error) {
              if (error && error.authenticationRequired) {
                stoppedForAuthentication = true;
                emit('offline:authentication-required', {item: item, error: error});
                return {shouldContinue: false, csrf: state.csrf};
              }
              setServerStatus(false);
              emit('offline:sync-paused', {item: item, error: error});
              return {shouldContinue: false, csrf: state.csrf};
            });
          });
        }, Promise.resolve({shouldContinue: true, csrf: csrf})).then(function (state) {
          return state.shouldContinue;
        });
      });
    }).catch(function (error) {
      if (error && error.authenticationRequired) {
        stoppedForAuthentication = true;
        emit('offline:authentication-required', {error: error});
      } else {
        emit('offline:sync-error', {error: error});
      }
      return false;
    }).finally(function () {
      replaying = false;
      emit('offline:sync-finished');
    });
  }

  function start() {
    if (heartbeatTimer !== null) return;
    heartbeat();
    heartbeatTimer = global.setInterval(heartbeat, HEARTBEAT_INTERVAL_MS);
  }

  global.addEventListener('offline:queue-updated', function () {
    if (serverOnline) syncNow();
  });
  global.addEventListener('offline:server-online', function () {
    stoppedForAuthentication = false;
  });
  global.addEventListener('offline:authentication-restored', function () {
    stoppedForAuthentication = false;
    syncNow();
  });
  global.addEventListener('online', heartbeat);

  global.OfflineSync = {
    start: start,
    heartbeat: heartbeat,
    syncNow: syncNow,
    isServerOnline: function () { return serverOnline; },
    isSyncing: function () { return replaying; },
    resumeAfterAuthentication: function () {
      stoppedForAuthentication = false;
      return syncNow();
    }
  };

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', start, {once: true});
  } else {
    start();
  }
})(window);
