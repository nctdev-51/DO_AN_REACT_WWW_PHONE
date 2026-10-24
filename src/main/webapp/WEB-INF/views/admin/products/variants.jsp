<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<jsp:include page="/WEB-INF/views/shared/admin/header.jsp" />
<a href="${pageContext.request.contextPath}/admin/products/${product.id}/edit">← Sửa sản phẩm</a>
<a class="btn btn-outline-dark ms-3" href="${pageContext.request.contextPath}/admin/products/${product.id}/media">Quản lý ảnh & 360°</a>
<h2 class="my-4">
  <c:out value="${product.name}"/>
</h2>
<c:forEach var="variant" items="${variants}">
  <article class="panel mb-3">
    <form action="${pageContext.request.contextPath}/admin/products/${product.id}/variants" method="post">
      <ui:csrf/>
      <input type="hidden" name="id" value="${variant.id}">
      <input type="hidden" name="version" value="${variant.version}">
      <div class="variant-form-grid">
        <div>
          <label>SKU<input class="form-control" name="sku" value="<c:out value='${variant.sku}'/>" required maxlength="120">
          </label>
        </div>
        <div>
          <label>Màu sắc<input class="form-control" name="color" value="<c:out value='${variant.color}'/>" required maxlength="80">
          </label>
        </div>
        <div>
          <label>Dung lượng<input class="form-control" name="storage" value="<c:out value='${variant.storage}'/>" required maxlength="40">
          </label>
        </div>
        <div>
          <label>RAM<input class="form-control" name="ram" value="<c:out value='${variant.ram}'/>" maxlength="40">
          </label>
        </div>
        <div>
          <label>Giá bán<input class="form-control" name="price" type="number" value="${variant.price}" min="1" max="1000000000" required>
          </label>
        </div>
        <div>
          <label>Tồn kho<input class="form-control" name="stock" type="number" value="${variant.stock}" min="0" max="1000000" required>
          </label>
        </div>
        <label class="payment-choice">
          <input type="checkbox" name="active" value="true" ${variant.active ? 'checked' : ''}>Đang bán</label>
        </div>
        <button class="btn btn-brand mt-3">Lưu phiên bản</button>
      </form>
    </article>
  </c:forEach>
  <section class="panel">
    <h2>Thêm phiên bản mới</h2>
    <form action="${pageContext.request.contextPath}/admin/products/${product.id}/variants" method="post">
      <ui:csrf/>
      <div class="variant-form-grid">
        <label>SKU<input class="form-control" name="sku" required maxlength="120">
        </label>
        <label>Màu sắc<input class="form-control" name="color" required maxlength="80">
        </label>
        <label>Dung lượng<input class="form-control" name="storage" required maxlength="40" placeholder="256GB">
        </label>
        <label>RAM<input class="form-control" name="ram" maxlength="40" placeholder="8GB">
        </label>
        <label>Giá bán<input class="form-control" name="price" type="number" min="1" max="1000000000" required>
        </label>
        <label>Tồn kho<input class="form-control" name="stock" type="number" min="0" max="1000000" required>
        </label>
        <label class="payment-choice">
          <input type="checkbox" name="active" value="true" checked>Đang bán</label>
        </div>
        <button class="btn btn-brand mt-3">Thêm phiên bản</button>
      </form>
    </section>
    <jsp:include page="/WEB-INF/views/shared/admin/footer.jsp" />
