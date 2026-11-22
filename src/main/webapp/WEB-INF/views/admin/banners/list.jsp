<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %><%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %><%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<jsp:include page="/WEB-INF/views/shared/admin/header.jsp"/>
<c:if test="${not empty successMsg}"><div class="alert alert-success alert-dismissible fade show" role="alert"><c:out value="${successMsg}"/><button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button></div></c:if>
<c:if test="${not empty errorMsg}"><div class="alert alert-danger alert-dismissible fade show" role="alert"><c:out value="${errorMsg}"/><button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button></div></c:if>
<c:if test="${not empty param.success}"><div class="alert alert-success alert-dismissible fade show" role="alert"><c:out value="${param.success}"/><button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button></div></c:if>
<c:if test="${not empty param.error}"><div class="alert alert-danger alert-dismissible fade show" role="alert"><c:out value="${param.error}"/><button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button></div></c:if>
<div class="section-heading">
  <p class="mb-0">Lịch hiển thị theo giờ Việt Nam. Thứ tự nhỏ hơn được ưu tiên trước.</p>
  <a class="btn btn-brand" href="${pageContext.request.contextPath}/admin/banners/new" style="min-height: 44px; display: inline-flex; align-items: center;">Thêm banner</a>
</div>
<div class="table-responsive">
  <table class="table align-middle">
    <thead>
      <tr>
        <th style="min-width: 100px;">Ảnh banner</th>
        <th style="min-width: 180px;">Tiêu đề chiến dịch</th>
        <th style="min-width: 140px;">Vị trí & Màu</th>
        <th style="min-width: 180px;">Lịch phát (GMT+7)</th>
        <th style="min-width: 110px;">Thứ tự</th>
        <th style="min-width: 130px;">Trạng thái</th>
        <th style="min-width: 150px; text-align: right;">Thao tác</th>
      </tr>
    </thead>
    <tbody>
      <c:forEach var="b" items="${banners}">
        <tr>
          <td>
            <c:choose>
              <c:when test="${not empty b.imagePath}">
                <img src="${pageContext.request.contextPath}${b.imagePath}" alt="<c:out value='${b.imageLabel}'/>" style="width: 80px; height: 50px; object-fit: cover; border-radius: 6px; border: 1px solid #ddd;">
              </c:when>
              <c:otherwise>
                <span class="text-muted small">Mặc định</span>
              </c:otherwise>
            </c:choose>
          </td>
          <td>
            <strong><c:out value="${b.title}"/></strong>
            <c:if test="${not empty b.subtitle}"><br><small class="text-muted"><c:out value="${b.subtitle}"/></small></c:if>
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
              <c:when test="${b.active}">
                <span class="badge bg-success text-white">Đang bật</span>
              </c:when>
              <c:otherwise>
                <span class="badge bg-secondary text-white">Đang tắt</span>
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
    </tbody>
  </table>
</div>
<c:if test="${empty banners}">
  <div class="panel empty-state mt-3">
    <h2>Chưa có banner chiến dịch</h2>
    <p>Trang chủ đang dùng sản phẩm trình bày trong Nội dung cửa hàng. Thêm banner để thay khối giới thiệu theo lịch.</p>
  </div>
</c:if>
<jsp:include page="/WEB-INF/views/shared/admin/footer.jsp"/>
