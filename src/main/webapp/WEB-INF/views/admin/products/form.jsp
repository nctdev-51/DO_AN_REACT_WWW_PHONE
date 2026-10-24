<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<jsp:include page="/WEB-INF/views/shared/admin/header.jsp" />
<a href="${pageContext.request.contextPath}/admin/products">← Danh sách sản phẩm</a>
<section class="panel mt-3">
  <h2>${empty productId ? 'Thêm sản phẩm' : 'Sửa sản phẩm'}</h2>
  <form:form action="${pageContext.request.contextPath}/admin/products/save" method="post" modelAttribute="productForm" enctype="multipart/form-data">
    <form:errors path="*" element="div" cssClass="alert alert-danger"/>
    <c:if test="${not empty productId}">
      <input type="hidden" name="id" value="${productId}">
    </c:if>
    <div class="field"><label for="kind">Loại sản phẩm</label><form:select path="kind" id="kind" cssClass="form-select"><form:option value="PHONE">Điện thoại</form:option><form:option value="ACCESSORY">Phụ kiện</form:option></form:select></div><div class="field"><label for="compatibleSkus">SKU điện thoại tương thích (phụ kiện), cách nhau dấu phẩy</label><form:input path="compatibleSkus" id="compatibleSkus" cssClass="form-control" maxlength="1500"/><small>Chỉ thêm tương thích đã xác minh. Không suy đoán từ tên hãng.</small></div>
    <div class="field"><label for="usageTags">Nhóm nhu cầu, cách nhau dấu phẩy</label><form:input path="usageTags" id="usageTags" cssClass="form-control" maxlength="200"/><small>EVERYDAY: hằng ngày; CAMERA: chụp ảnh; GAMING: chơi game; BATTERY: pin; COMPACT: gọn. Chỉ phân loại dựa trên thông số và kiểm tra thực tế của cửa hàng.</small></div>
    <div class="form-grid">
      <div class="field">
        <label for="name">Tên sản phẩm</label>
        <form:input id="name" path="name" cssClass="form-control"/>
      </div>
      <div class="field">
        <label for="brand">Thương hiệu</label>
        <form:input id="brand" path="brand" cssClass="form-control"/>
      </div>
      <div class="field">
        <label for="sku">SKU sản phẩm</label>
        <form:input id="sku" path="sku" cssClass="form-control"/>
      </div>
      <div class="field">
        <label for="price">Giá ban đầu (VNĐ)</label>
        <form:input id="price" path="price" cssClass="form-control"/>
      </div>
      <div class="field">
        <label for="chipset">Chip xử lý</label>
        <form:input id="chipset" path="chipset" cssClass="form-control"/>
      </div>
      <div class="field">
        <label for="display">Màn hình</label>
        <form:input id="display" path="display" cssClass="form-control"/>
      </div>
      <div class="field">
        <label for="camera">Camera</label>
        <form:input id="camera" path="camera" cssClass="form-control"/>
      </div>
      <div class="field">
        <label for="battery">Pin</label>
        <form:input id="battery" path="battery" cssClass="form-control"/>
      </div>
      <div class="field">
        <label for="operatingSystem">Hệ điều hành</label>
        <form:input id="operatingSystem" path="operatingSystem" cssClass="form-control"/>
      </div>
      <div class="field">
        <label for="warrantyMonths">Bảo hành (tháng)</label>
        <form:input id="warrantyMonths" path="warrantyMonths" cssClass="form-control"/>
      </div>
      <div class="field">
        <label for="categoryId">Danh mục</label>
        <form:select id="categoryId" path="categoryId" cssClass="form-select">
          <form:option value="" label="Chọn danh mục"/>
          <form:options items="${categories}" itemValue="id" itemLabel="name"/>
        </form:select>
      </div>
      <div class="field">
        <label for="status">Trạng thái</label>
        <form:select id="status" path="status" cssClass="form-select">
          <form:option value="1" label="Đang bán"/>
          <form:option value="0" label="Ngừng bán"/>
        </form:select>
      </div>
    </div>
    <div class="field">
      <label for="description">Mô tả sản phẩm</label>
      <form:textarea id="description" path="description" cssClass="form-control" rows="5"/>
    </div>
    <div class="field">
      <label for="image">Ảnh (PNG/JPEG, tối đa 5 MB)</label>
      <input id="image" name="image" type="file" class="form-control" accept="image/png,image/jpeg">
      <c:if test="${not empty image}">
        <img class="image-preview" src="${pageContext.request.contextPath}${image}" alt="Ảnh hiện tại">
      </c:if>
    </div>
    <button class="btn btn-brand">Lưu sản phẩm</button>
    <c:if test="${not empty productId}">
      <a class="btn btn-light" href="${pageContext.request.contextPath}/admin/products/${productId}/variants">Quản lý phiên bản</a>
    </c:if>
  </form:form>
</section>
<c:if test="${productId != null}"><p><a href="${pageContext.request.contextPath}/admin/products/${productId}/media">Quản lý ảnh & 360°</a></p></c:if>
<jsp:include page="/WEB-INF/views/shared/admin/footer.jsp" />
