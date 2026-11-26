<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
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