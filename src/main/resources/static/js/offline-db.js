(function (global) {
  'use strict';

  const DATABASE_NAME = 'ptv_offline_db';
  const DATABASE_VERSION = 1;
  const STORE_NAME = 'sync_queue';
  let databasePromise;

  function openDatabase() {
    if (!('indexedDB' in global)) {
      return Promise.reject(new Error('Trình duyệt không hỗ trợ IndexedDB.'));
    }
    if (!databasePromise) {
      databasePromise = new Promise(function (resolve, reject) {
        const request = global.indexedDB.open(DATABASE_NAME, DATABASE_VERSION);
        request.onupgradeneeded = function () {
          const database = request.result;
          if (!database.objectStoreNames.contains(STORE_NAME)) {
            const store = database.createObjectStore(STORE_NAME, {keyPath: 'id'});
            store.createIndex('createdAt', 'createdAt', {unique: false});
            store.createIndex('status', 'status', {unique: false});
          }
        };
        request.onsuccess = function () {
          const database = request.result;
          database.onversionchange = function () {
            database.close();
            databasePromise = null;
          };
          resolve(database);
        };
        request.onerror = function () {
          databasePromise = null;
          reject(request.error || new Error('Không thể mở hàng đợi ngoại tuyến.'));
        };
        request.onblocked = function () {
          reject(new Error('Hãy tải lại các tab PTV Mobile khác để mở hàng đợi ngoại tuyến.'));
        };
      });
    }
    return databasePromise;
  }

  function transact(mode, operation) {
    return openDatabase().then(function (database) {
      return new Promise(function (resolve, reject) {
        const transaction = database.transaction(STORE_NAME, mode);
        const store = transaction.objectStore(STORE_NAME);
        try {
          operation(store);
        } catch (error) {
          reject(error);
          return;
        }
        transaction.oncomplete = function () { resolve(); };
        transaction.onerror = function () {
          reject(transaction.error || new Error('Không thể cập nhật hàng đợi ngoại tuyến.'));
        };
        transaction.onabort = function () {
          reject(transaction.error || new Error('Cập nhật hàng đợi ngoại tuyến bị hủy.'));
        };
      });
    });
  }

  function notifyQueueChanged() {
    global.dispatchEvent(new CustomEvent('offline:queue-updated'));
  }

  function encodePayload(payload) {
    if (payload instanceof FormData) {
      return {
        kind: 'form',
        entries: Array.from(payload.entries())
      };
    }
    if (payload instanceof URLSearchParams) {
      return {kind: 'form', entries: Array.from(payload.entries())};
    }
    if (typeof payload === 'string') {
      return {kind: 'text', value: payload};
    }
    if (payload == null) {
      return {kind: 'empty', value: null};
    }
    return {kind: 'json', value: payload};
  }

  const api = {
    databaseName: DATABASE_NAME,
    storeName: STORE_NAME,

    enqueue: function (request) {
      if (!request || typeof request.url !== 'string' || !request.url.trim()) {
        return Promise.reject(new TypeError('Request ngoại tuyến cần có URL hợp lệ.'));
      }
      const method = String(request.method || 'POST').toUpperCase();
      if (!['POST', 'PUT', 'PATCH', 'DELETE'].includes(method)) {
        return Promise.reject(new TypeError('Phương thức này không được hỗ trợ trong hàng đợi.'));
      }
      const clientRequestId = request.clientRequestId
        || (global.crypto && global.crypto.randomUUID
          ? global.crypto.randomUUID()
          : Date.now().toString(36) + '-' + Math.random().toString(36).slice(2));
      const item = {
        id: clientRequestId,
        clientRequestId: clientRequestId,
        url: request.url,
        method: method,
        payload: encodePayload(request.payload),
        createdAt: new Date().toISOString(),
        status: 'pending',
        errorMsg: null
      };
      return openDatabase().then(function (database) {
        return new Promise(function (resolve, reject) {
          const transaction = database.transaction(STORE_NAME, 'readwrite');
          transaction.objectStore(STORE_NAME).add(item);
          transaction.oncomplete = function () {
            notifyQueueChanged();
            resolve(item);
          };
          transaction.onerror = function () {
            reject(transaction.error || new Error('Không thể lưu thao tác ngoại tuyến.'));
          };
          transaction.onabort = function () {
            reject(transaction.error || new Error('Lưu thao tác ngoại tuyến bị hủy.'));
          };
        });
      });
    },

    list: function () {
      return openDatabase().then(function (database) {
        return new Promise(function (resolve, reject) {
          const transaction = database.transaction(STORE_NAME, 'readonly');
          const request = transaction.objectStore(STORE_NAME).getAll();
          request.onsuccess = function () {
            resolve(request.result.sort(function (a, b) {
              return a.createdAt.localeCompare(b.createdAt) || a.id.localeCompare(b.id);
            }));
          };
          request.onerror = function () {
            reject(request.error || new Error('Không thể đọc hàng đợi ngoại tuyến.'));
          };
        });
      });
    },

    pending: function () {
      return api.list().then(function (items) {
        return items.filter(function (item) { return item.status === 'pending'; });
      });
    },

    count: function () {
      return api.list().then(function (items) {
        return items.filter(function (item) { return item.status === 'pending'; }).length;
      });
    },

    update: function (id, changes) {
      return openDatabase().then(function (database) {
        return new Promise(function (resolve, reject) {
          const transaction = database.transaction(STORE_NAME, 'readwrite');
          const store = transaction.objectStore(STORE_NAME);
          const getRequest = store.get(id);
          let updated;
          getRequest.onsuccess = function () {
            if (!getRequest.result) {
              transaction.abort();
              reject(new Error('Không tìm thấy thao tác trong hàng đợi.'));
              return;
            }
            updated = Object.assign({}, getRequest.result, changes);
            store.put(updated);
          };
          transaction.oncomplete = function () {
            notifyQueueChanged();
            resolve(updated);
          };
          transaction.onerror = function () {
            reject(transaction.error || new Error('Không thể cập nhật thao tác ngoại tuyến.'));
          };
          transaction.onabort = function () {
            if (updated) reject(transaction.error || new Error('Cập nhật thao tác bị hủy.'));
          };
        });
      });
    },

    remove: function (id) {
      return transact('readwrite', function (store) {
        return store.delete(id);
      }).then(function (result) {
        notifyQueueChanged();
        return result;
      });
    },

    payloadToBody: function (payload) {
      if (!payload || payload.kind === 'empty') return null;
      if (payload.kind === 'json') return JSON.stringify(payload.value);
      if (payload.kind === 'text') return payload.value;
      if (payload.kind === 'form') {
        const form = new FormData();
        payload.entries.forEach(function (entry) {
          form.append(entry[0], entry[1]);
        });
        return form;
      }
      throw new TypeError('Định dạng payload trong hàng đợi không hợp lệ.');
    }
  };

  global.OfflineDB = api;
})(window);
