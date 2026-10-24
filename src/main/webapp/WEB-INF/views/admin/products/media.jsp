<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<jsp:include page="/WEB-INF/views/shared/admin/header.jsp"/>
<a href="${pageContext.request.contextPath}/admin/products/${product.id}/edit">← <c:out value="${product.name}"/></a>
<section class="panel mt-4"><h2>Thêm bộ ảnh</h2><form action="${pageContext.request.contextPath}/admin/products/${product.id}/media" method="post" enctype="multipart/form-data"><ui:csrf/>
  <div class="field"><label for="media-color">Màu sản phẩm</label><select id="media-color" name="color" class="form-select"><option value="">Ảnh giới thiệu chung</option><c:forEach var="color" items="${colors}"><option value="<c:out value='${color}'/>"><c:out value="${color}"/></option></c:forEach></select></div>
  <div class="field"><label for="media-kind">Loại bộ ảnh</label><select id="media-kind" name="kind" class="form-select"><option value="PHOTO">Ảnh chụp nhiều góc</option><option value="SPIN">Ảnh quay 360° theo thứ tự</option></select></div>
  <div class="field"><label for="media-files">Tệp ảnh</label><input id="media-files" name="files" type="file" multiple required accept="image/jpeg,image/png" class="form-control"><p class="muted small mt-2">Ảnh chụp: tối đa 20 ảnh. Bộ xoay 360°: 12–120 ảnh chụp liên tiếp quanh máy, đặt tên 001, 002… Mỗi ảnh tối đa 5 MB, cả bộ tối đa 64 MB.</p></div>
  <button class="btn btn-brand">Lưu bộ ảnh</button>
</form></section>
<section class="panel mt-4"><h2>Ảnh hiện có</h2><div class="media-admin-grid"><c:forEach var="item" items="${media}"><article><c:choose><c:when test="${item.kind == 'MODEL'}"><strong>Mô hình 3D</strong><p><c:out value="${item.caption}"/></p></c:when><c:otherwise><img src="${pageContext.request.contextPath}${item.path}" alt="" loading="lazy"></c:otherwise></c:choose><p><c:out value="${empty item.color ? 'Ảnh chung' : item.color}"/> · <c:out value="${item.kind}"/></p><form action="${pageContext.request.contextPath}/admin/products/${product.id}/media/${item.id}/delete" method="post" data-confirm="Xóa ảnh này?"><ui:csrf/><button class="btn btn-sm btn-outline-danger">Xóa</button></form></article></c:forEach></div></section>
<jsp:include page="/WEB-INF/views/shared/admin/footer.jsp"/>
