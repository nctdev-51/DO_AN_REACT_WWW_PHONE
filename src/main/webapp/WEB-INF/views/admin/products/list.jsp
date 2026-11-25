<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<jsp:include page="/WEB-INF/views/shared/admin/header.jsp" />
<div class="section-heading">
  <p class="muted">Quản lý thông tin sản phẩm; giá từng phiên bản và tồn kho nằm trong mục Phiên bản.</p>
  <a class="btn btn-brand" href="${pageContext.request.contextPath}/admin/products/new">+ Thêm sản phẩm</a>
</div>
<section class="panel">
    <ui:adminNameSearch action="/admin/products" label="Tên sản phẩm" query="${q}"/>
  <ui:adminBatchForm action="/admin/products/batch">
    <jsp:attribute name="fields">
      <div data-batch-field="category">
        <label class="form-label" for="bulk-category">Danh mục đích</label>
        <select id="bulk-category" class="form-select" name="categoryId">
          <option value="">Chọn danh mục</option>
          <c:forEach var="category" items="${categories}">
            <option value="${category.id}"><c:out value="${category.name}"/></option>
          </c:forEach>
        </select>
      </div>
    </jsp:attribute>
    <jsp:body>
      <option value="enable">Chuyển sang đang bán</option>
      <option value="disable">Ngừng bán</option>
      <option value="category">Chuyển danh mục</option>
      <option value="delete" data-destructive>Xóa sản phẩm</option>
    </jsp:body>
  </ui:adminBatchForm>
  <div class="table-responsive">
    <table class="table align-middle">
      <thead>
        <tr>
          <th scope="col"><input type="checkbox" data-batch-all aria-label="Chọn tất cả kết quả"></th>
          <th>Sản phẩm</th>
          <th>Thương hiệu</th>
          <th>Giá từ</th>
          <th>Tồn kho</th>
          <th>Trạng thái</th>
          <th>Thao tác</th>
        </tr>
      </thead>
      <tbody id="admin-name-results">
        <c:forEach var="product" items="${products}">
          <tr>
            <td><input type="checkbox" name="ids" value="${product.id}" form="admin-batch" data-batch-label="<c:out value='${product.name}'/>"
                aria-label="Chọn mục ${product.id}"></td>
            <td>
              <div class="table-product">
                <ui:adminProductImage path="${product.image}"/>
                <div>
                  <strong>
                    <c:out value="${product.name}"/>
                  </strong>
                  <small>
                    <c:out value="${product.sku}"/>
                  </small>
                </div>
              </div>
            </td>
            <td>
              <c:out value="${product.brand}"/>
            </td>
            <td>
              <ui:money value="${product.price}"/>
            </td>
            <td>${product.stock}</td>
            <td>${product.status == 1 ? 'Đang bán' : 'Ngừng bán'}</td>
            <td>
              <div class="table-actions">
                <a class="btn btn-light btn-sm" href="${pageContext.request.contextPath}/admin/products/${product.id}/edit">Sửa</a>
                <a class="btn btn-light btn-sm" href="${pageContext.request.contextPath}/admin/products/${product.id}/variants">Phiên bản</a>
                <form action="${pageContext.request.contextPath}/admin/products/${product.id}/delete" method="post" data-confirm="Xóa hoặc ngừng kinh doanh sản phẩm này?">
                  <ui:csrf/>
                  <button class="btn btn-outline-danger btn-sm">Xóa</button>
                </form>
              </div>
            </td>
          </tr>
        </c:forEach>
      <c:if test="${empty products}"><tr><td colspan="7">Không tìm thấy sản phẩm phù hợp.</td></tr></c:if></tbody>
    </table>
  </div>
</section>
<jsp:include page="/WEB-INF/views/shared/admin/footer.jsp" />
