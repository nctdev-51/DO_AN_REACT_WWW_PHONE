<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<<<<<<< HEAD
  <%@ taglib prefix="c" uri="jakarta.tags.core" %>
    <%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
      <%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
        <%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
          <jsp:include page="/WEB-INF/views/shared/admin/header.jsp" />
          <c:if test="${not empty successMsg}">
            <div class="alert alert-success alert-dismissible fade show" role="alert">
              <c:out value="${successMsg}" /><button type="button" class="btn-close" data-bs-dismiss="alert"
                aria-label="Close"></button>
            </div>
          </c:if>
          <c:if test="${not empty errorMsg}">
            <div class="alert alert-danger alert-dismissible fade show" role="alert">
              <c:out value="${errorMsg}" /><button type="button" class="btn-close" data-bs-dismiss="alert"
                aria-label="Close"></button>
            </div>
          </c:if>
          <c:if test="${not empty param.success}">
            <div class="alert alert-success alert-dismissible fade show" role="alert">
              <c:out value="${param.success}" /><button type="button" class="btn-close" data-bs-dismiss="alert"
                aria-label="Close"></button>
            </div>
          </c:if>
          <c:if test="${not empty param.error}">
            <div class="alert alert-danger alert-dismissible fade show" role="alert">
              <c:out value="${param.error}" /><button type="button" class="btn-close" data-bs-dismiss="alert"
                aria-label="Close"></button>
            </div>
          </c:if>
          <div class="section-heading">
            <p class="mb-0">Lịch hiển thị theo giờ Việt Nam. Thứ tự nhỏ hơn được ưu tiên trước.</p>
            <div class="d-flex flex-wrap align-items-center gap-2">
              <form method="post" action="${pageContext.request.contextPath}/admin/system/migrate-cloudinary"
                class="m-0" data-confirm="Đồng bộ toàn bộ ảnh cũ từ hệ thống lên Cloudinary?">
                <ui:csrf />
                <button type="submit" class="btn btn-outline-primary"
                  style="min-height: 44px; display: inline-flex; align-items: center; gap: 6px;">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                    stroke-linecap="round" stroke-linejoin="round">
                    <path d="M17.5 19H9a7 7 0 1 1 6.71-9h1.79a4.5 4.5 0 1 1 0 9Z" />
                    <polyline points="12 13 12 9 10 11" />
                    <polyline points="12 9 14 11" />
                  </svg>
                  Đồng bộ ảnh Cloudinary
                </button>
              </form>
              <a class="btn btn-brand" href="${pageContext.request.contextPath}/admin/banners/new"
                style="min-height: 44px; display: inline-flex; align-items: center;">Thêm banner</a>
            </div>
          </div>
          <div class="table-responsive" tabindex="0" role="region" aria-label="Danh sách banner">
            <table class="table align-middle campaign-admin-table">
              <thead>
                <tr>
                  <th>Ảnh banner</th>
                  <th>Tiêu đề chiến dịch</th>
                  <th>Vị trí & Màu</th>
                  <th>Lịch phát (GMT+7)</th>
                  <th>Thứ tự</th>
                  <th>Trạng thái</th>
                  <th class="text-end">Thao tác</th>
                </tr>
              </thead>
              <tbody id="admin-name-results">
                <c:forEach var="b" items="${banners}">
                  <tr>
                    <td><input type="checkbox" name="ids" value="${b.id}" form="admin-batch"
                        data-batch-label="<c:out value='${b.title}'/>" data-batch-version="${b.version}"
                        data-batch-actions="${b.active ? 'disable' : 'enable'}|delete" aria-label="Chọn mục ${b.id}">
                      <input type="hidden" name="version_${b.id}" value="${b.version}" form="admin-batch"
                        data-batch-version-field>
                    </td>
                    <td>
                      <c:choose>
                        <c:when test="${not empty b.imagePath}">
                          <c:set var="resolvedImg"
                            value="${fn:startsWith(b.imagePath, 'http') ? b.imagePath : pageContext.request.contextPath.concat(b.imagePath)}" />
                          <img src="${resolvedImg}" alt="<c:out value='${b.imageLabel}'/>"
                            class="campaign-admin-thumbnail">
                        </c:when>
                        <c:otherwise>
                          <span class="text-muted small">Mặc định</span>
                        </c:otherwise>
                      </c:choose>
                    </td>
                    <td>
                      <strong>
                        <c:out value="${b.title}" />
                      </strong>
                      <c:if test="${not empty b.subtitle}"><br><small class="text-muted">
                          <c:out value="${b.subtitle}" />
                        </small></c:if>
                      <c:if test="${b.target == 'SEARCH'}"><br><small class="text-muted">Từ khóa đích:
                          <c:out value="${b.searchKeyword}" />
                        </small></c:if>
                    </td>
                    <td>
                      <span class="badge ${b.placement == 'HERO' ? 'bg-primary' : 'bg-info'} text-white">
                        ${b.placement == 'HERO' ? 'Khối chính (Hero)' : 'Thẻ (Tile)'}
                      </span>
                      <br><small class="text-muted">${b.theme == 'DARK' ? 'Nền tối' : 'Nền sáng'}</small>
                    </td>
                    <td><small>
                        <c:out value="${b.scheduleLabel}" />
                      </small></td>
                    <td><strong>${b.position}</strong></td>
                    <td>
                      <c:choose>
                        <c:when test="${not b.active}">
                          <span class="status-badge status-danger">Đang tắt</span>
                          <form method="post" action="${pageContext.request.contextPath}/admin/banners/${b.id}/enable"
                            class="mt-2">
                            <ui:csrf />
                            <input type="hidden" name="version" value="${b.version}">
                            <input type="hidden" name="q" value="<c:out value='${q}'/>">
                            <input type="hidden" name="from" value="${from}">
                            <input type="hidden" name="to" value="${to}">
                            <input type="hidden" name="state" value="${state}">
                            <button type="submit" class="btn btn-sm btn-outline-success"
                              aria-label="Bật banner <c:out value='${b.title}'/>">Bật</button>
                          </form>
                        </c:when>
                        <c:when test="${b.startsAt.isAfter(bannerNow)}">
                          <span class="status-badge status-warning">Chờ đến lịch</span>
                        </c:when>
                        <c:when test="${not b.endsAt.isAfter(bannerNow)}">
                          <span class="status-badge">Hết lịch</span>
                        </c:when>
                        <c:otherwise>
                          <span class="status-badge status-success">Trong lịch phát</span>
                        </c:otherwise>
                      </c:choose>
                    </td>
                    <td style="text-align: right;">
                      <div class="d-inline-flex gap-2">
                        <a class="btn btn-sm btn-outline-dark"
                          href="${pageContext.request.contextPath}/admin/banners/${b.id}/edit"
                          style="min-height: 44px; display: inline-flex; align-items: center; padding: 0 12px;">Sửa</a>
                        <form method="post" action="${pageContext.request.contextPath}/admin/banners/${b.id}/delete"
                          data-confirm="Xóa banner này?" class="m-0">
                          <ui:csrf />
                          <input type="hidden" name="version" value="${b.version}">
                          <button class="btn btn-sm btn-outline-danger"
                            style="min-height: 44px; display: inline-flex; align-items: center; padding: 0 12px;">Xóa</button>
                        </form>
                      </div>
                    </td>
                  </tr>
                </c:forEach>
                <c:if test="${empty banners}">
                  <tr>
                    <td colspan="8">
                      <c:choose>
                        <c:when test="${not empty filterError}"><span class="text-danger" role="alert">
                            <c:out value="${filterError}" />
                          </span></c:when>
                        <c:otherwise><span class="muted">${hasFilters ? 'Không tìm thấy chiến dịch phù hợp.' : 'Chưa có
                            chiến dịch.'}</span></c:otherwise>
                      </c:choose>
                    </td>
                  </tr>
                </c:if>
              </tbody>
            </table>
          </div>
          <jsp:include page="/WEB-INF/views/shared/admin/footer.jsp" />
=======
<%@ taglib prefix="c" uri="jakarta.tags.core" %><%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %><%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %><%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<jsp:include page="/WEB-INF/views/shared/admin/header.jsp"/>
<c:if test="${not empty successMsg}"><div class="alert alert-success alert-dismissible fade show" role="alert"><c:out value="${successMsg}"/><button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button></div></c:if>
<c:if test="${not empty errorMsg}"><div class="alert alert-danger alert-dismissible fade show" role="alert"><c:out value="${errorMsg}"/><button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button></div></c:if>
<c:if test="${not empty param.success}"><div class="alert alert-success alert-dismissible fade show" role="alert"><c:out value="${param.success}"/><button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button></div></c:if>
<c:if test="${not empty param.error}"><div class="alert alert-danger alert-dismissible fade show" role="alert"><c:out value="${param.error}"/><button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button></div></c:if>
<div class="section-heading">
  <p class="mb-0">Lịch hiển thị theo giờ Việt Nam. Thứ tự nhỏ hơn được ưu tiên trước.</p>
  <div class="d-flex flex-wrap align-items-center gap-2">
    <form method="post" action="${pageContext.request.contextPath}/admin/system/migrate-cloudinary" class="m-0" data-confirm="Đồng bộ toàn bộ ảnh cũ từ hệ thống lên Cloudinary?">
      <ui:csrf/>
      <button type="submit" class="btn btn-outline-primary" style="min-height: 44px; display: inline-flex; align-items: center; gap: 6px;">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17.5 19H9a7 7 0 1 1 6.71-9h1.79a4.5 4.5 0 1 1 0 9Z"/><polyline points="12 13 12 9 10 11"/><polyline points="12 9 14 11"/></svg>
        Đồng bộ ảnh Cloudinary
      </button>
    </form>
    <a class="btn btn-brand" href="${pageContext.request.contextPath}/admin/banners/new" style="min-height: 44px; display: inline-flex; align-items: center;">Thêm banner</a>
  </div>
</div>
<form method="get" action="${pageContext.request.contextPath}/admin/banners" class="row g-2 align-items-end mb-3" data-admin-name-search>
  <div class="col-sm-6 col-xl-4">
    <label for="banner-title-search" class="form-label">Tên chiến dịch</label>
    <input id="banner-title-search" class="form-control" type="search" name="q"
           value="<c:out value='${q}'/>" placeholder="Ví dụ Galaxy S25"
           autocomplete="off" aria-controls="admin-name-results">
  </div>
  <div class="col-sm-6 col-xl-auto">
    <label for="banner-from" class="form-label">Từ ngày</label>
    <input id="banner-from" class="form-control" type="date" name="from" value="${from}">
  </div>
  <div class="col-sm-6 col-xl-auto">
    <label for="banner-to" class="form-label">Đến ngày</label>
    <input id="banner-to" class="form-control" type="date" name="to" value="${to}">
  </div>
  <div class="col-sm-6 col-xl-auto">
    <label for="banner-state" class="form-label">Trạng thái</label>
    <select id="banner-state" class="form-select" name="state">
      <option value="">Tất cả trạng thái</option>
      <option value="off" ${state == 'off' ? 'selected' : ''}>Đang tắt</option>
      <option value="scheduled" ${state == 'scheduled' ? 'selected' : ''}>Chờ đến lịch</option>
      <option value="live" ${state == 'live' ? 'selected' : ''}>Trong lịch phát</option>
      <option value="expired" ${state == 'expired' ? 'selected' : ''}>Hết lịch</option>
    </select>
  </div>
  <div class="col-auto">
    <button type="submit" class="btn btn-brand">Lọc</button>
    <a class="btn btn-light ${hasFilters ? '' : 'd-none'}" href="${pageContext.request.contextPath}/admin/banners" data-search-clear>Xóa bộ lọc</a>
  </div>
  <span class="col-auto small muted" data-search-status role="status"></span>
</form>
  <ui:adminBatchForm action="/admin/banners/batch">
    <option value="enable">Bật</option>
    <option value="disable">Tắt</option>
    <option value="delete" data-destructive>Xóa mục đã chọn</option>
  </ui:adminBatchForm>
  <div class="table-responsive" tabindex="0" role="region" aria-label="Danh sách banner">
  <table class="table align-middle campaign-admin-table">
    <thead>
      <tr>
          <th scope="col"><input type="checkbox" data-batch-all aria-label="Chọn tất cả kết quả"></th>
        <th style="min-width: 100px;">Ảnh banner</th>
        <th style="min-width: 180px;">Tiêu đề chiến dịch</th>
        <th style="min-width: 140px;">Vị trí & Màu</th>
        <th style="min-width: 180px;">Lịch phát (GMT+7)</th>
        <th style="min-width: 110px;">Thứ tự</th>
        <th style="min-width: 130px;">Trạng thái</th>
        <th class="text-end" style="min-width: 150px;">Thao tác</th>
      </tr>
    </thead>
    <tbody id="admin-name-results">
      <c:forEach var="b" items="${banners}">
        <tr>
            <td><input type="checkbox" name="ids" value="${b.id}" form="admin-batch" data-batch-label="<c:out value='${b.title}'/>" data-batch-version="${b.version}"
                data-batch-actions="${b.active ? 'disable' : 'enable'}|delete" aria-label="Chọn mục ${b.id}">
                <input type="hidden" name="version_${b.id}" value="${b.version}" form="admin-batch" data-batch-version-field></td>
          <td>
            <c:choose>
              <c:when test="${not empty b.imagePath}">
                <c:set var="resolvedImg" value="${fn:startsWith(b.imagePath, 'http') ? b.imagePath : pageContext.request.contextPath.concat(b.imagePath)}" />
                <img src="${resolvedImg}" alt="<c:out value='${b.imageLabel}'/>" class="campaign-admin-thumbnail">
              </c:when>
              <c:otherwise>
                <span class="text-muted small">Mặc định</span>
              </c:otherwise>
            </c:choose>
          </td>
          <td>
            <strong><c:out value="${b.title}"/></strong>
            <c:if test="${not empty b.subtitle}"><br><small class="text-muted"><c:out value="${b.subtitle}"/></small></c:if>
            <c:if test="${b.target == 'SEARCH'}"><br><small class="text-muted">Từ khóa đích: <c:out value="${b.searchKeyword}"/></small></c:if>
          </td>
          <td>
            <span class="badge ${b.placement == 'HERO' ? 'bg-primary' : 'bg-info'} text-white">
              ${b.placement == 'HERO' ? 'Khối chính (Hero)' : 'Thẻ (Tile)'}
            </span>
            <br><small class="text-muted">${b.theme == 'DARK' ? 'Nền tối' : 'Nền sáng'}</small>
          </td>
          <td><small><c:out value="${b.scheduleLabel}"/></small></td>
          <td><strong>${b.position}</strong></td>
          <td>
            <c:choose>
              <c:when test="${not b.active}">
                <span class="status-badge status-danger">Đang tắt</span>
                <form method="post" action="${pageContext.request.contextPath}/admin/banners/${b.id}/enable" class="mt-2">
                  <ui:csrf/>
                  <input type="hidden" name="version" value="${b.version}">
                  <input type="hidden" name="q" value="<c:out value='${q}'/>">
                  <input type="hidden" name="from" value="${from}">
                  <input type="hidden" name="to" value="${to}">
                  <input type="hidden" name="state" value="${state}">
                  <button type="submit" class="btn btn-sm btn-outline-success" aria-label="Bật banner <c:out value='${b.title}'/>">Bật</button>
                </form>
              </c:when>
              <c:when test="${b.startsAt.isAfter(bannerNow)}">
                <span class="status-badge status-warning">Chờ đến lịch</span>
              </c:when>
              <c:when test="${not b.endsAt.isAfter(bannerNow)}">
                <span class="status-badge">Hết lịch</span>
              </c:when>
              <c:otherwise>
                <span class="status-badge status-success">Trong lịch phát</span>
              </c:otherwise>
            </c:choose>
          </td>
          <td style="text-align: right;">
            <div class="d-inline-flex gap-2">
              <a class="btn btn-sm btn-outline-dark" href="${pageContext.request.contextPath}/admin/banners/${b.id}/edit" style="min-height: 44px; display: inline-flex; align-items: center; padding: 0 12px;">Sửa</a>
              <form method="post" action="${pageContext.request.contextPath}/admin/banners/${b.id}/delete" data-confirm="Xóa banner này?" class="m-0">
                <ui:csrf/>
                <input type="hidden" name="version" value="${b.version}">
                <button class="btn btn-sm btn-outline-danger" style="min-height: 44px; display: inline-flex; align-items: center; padding: 0 12px;">Xóa</button>
              </form>
            </div>
          </td>
        </tr>
      </c:forEach>
      <c:if test="${empty banners}">
        <tr><td colspan="8">
          <c:choose>
            <c:when test="${not empty filterError}"><span class="text-danger" role="alert"><c:out value="${filterError}"/></span></c:when>
            <c:otherwise><span class="muted">${hasFilters ? 'Không tìm thấy chiến dịch phù hợp.' : 'Chưa có chiến dịch.'}</span></c:otherwise>
          </c:choose>
        </td></tr>
      </c:if>
    </tbody>
  </table>
</div>
<jsp:include page="/WEB-INF/views/shared/admin/footer.jsp"/>
>>>>>>> origin/taidev
