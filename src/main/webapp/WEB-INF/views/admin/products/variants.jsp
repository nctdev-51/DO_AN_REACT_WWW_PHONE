<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
  <%@ taglib prefix="c" uri="jakarta.tags.core" %>
    <%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
      <jsp:include page="/WEB-INF/views/shared/admin/header.jsp" />
      <div class="d-flex flex-wrap gap-2 align-items-center mb-3">
        <a class="btn btn-light btn-sm" href="${pageContext.request.contextPath}/admin/products/${product.id}/edit">←
          Sửa sản phẩm</a>
        <a class="btn btn-outline-dark btn-sm"
          href="${pageContext.request.contextPath}/admin/products/${product.id}/media">Quản lý ảnh &amp; 360°</a>
      </div>
      <h2 class="my-4">
        <c:out value="${product.name}" />
      </h2>
      <ui:adminBatchForm action="/admin/products/${product.id}/variants/batch">
        <jsp:attribute name="fields">
          <div data-batch-field="stock">
            <label class="form-label" for="bulk-stock">Tồn kho mới mỗi phiên bản</label>
            <input id="bulk-stock" class="form-control" name="stock" type="number" min="0" max="1000000">
          </div>
        </jsp:attribute>
        <jsp:body>
          <option value="stock">Cập nhật tồn kho</option>
          <option value="enable">Bật bán</option>
          <option value="disable" data-destructive>Tạm ngưng bán</option>
        </jsp:body>
      </ui:adminBatchForm>
      <label class="d-inline-flex align-items-center gap-2 mb-3">
        <input type="checkbox" data-batch-all> Chọn tất cả phiên bản
      </label>
      <c:forEach var="variant" items="${variants}">
        <article class="panel mb-3" data-batch-row>
          <label class="d-inline-flex align-items-center gap-2 mb-2">
            <input type="checkbox" name="ids" value="${variant.id}" form="admin-batch"
              data-batch-label="<c:out value='${variant.sku}'/>"
              data-batch-actions="${variant.active ? 'disable' : 'enable'}|stock">
            Chọn
            <c:out value="${variant.sku}" />
          </label>
          <span class="status-badge ${variant.active ? 'status-success' : 'status-danger'}">${variant.active ? 'Đang
            bán' : 'Ngừng bán'}</span>
          <form action="${pageContext.request.contextPath}/admin/products/${product.id}/variants" method="post">
            <ui:csrf />
            <input type="hidden" name="id" value="${variant.id}">
            <input type="hidden" name="version" value="${variant.version}">
            <div class="variant-form-grid">
              <div>
                <label>SKU<input class="form-control" name="sku" value="<c:out value='${variant.sku}'/>" required
                    maxlength="120">
                </label>
              </div>
              <div>
                <label>Màu sắc<input class="form-control" name="color" value="<c:out value='${variant.color}'/>"
                    required maxlength="80">
                </label>
              </div>
              <div>
                <label>Dung lượng<input class="form-control" name="storage" value="<c:out value='${variant.storage}'/>"
                    required maxlength="40">
                </label>
              </div>
              <div>
                <label>RAM<input class="form-control" name="ram" value="<c:out value='${variant.ram}'/>" maxlength="40">
                </label>
              </div>
              <div>
                <label>Giá bán<input class="form-control" name="price" type="number" value="${variant.price}" min="1"
                    max="1000000000" required>
                </label>
              </div>
              <div>
                <label>Tồn kho<input class="form-control" name="stock" type="number" value="${variant.stock}" min="0"
                    max="1000000" required>
                </label>
              </div>
              <label class="payment-choice">
                <input type="hidden" name="_active" value="on">
                <input type="checkbox" name="active" value="true" ${variant.active ? 'checked' : '' }>Đang bán</label>
            </div>
            <div class="d-flex flex-wrap gap-2 mt-3">
              <button class="btn btn-brand">Lưu phiên bản</button>
              <c:if test="${variant.active}">
                <button class="btn btn-outline-danger" type="submit"
                  formaction="${pageContext.request.contextPath}/admin/products/${product.id}/variants/${variant.id}/disable"
                  formmethod="post" formnovalidate>Tạm ngưng bán</button>
              </c:if>
            </div>
          </form>
        </article>
      </c:forEach>
      <section class="panel">
        <h2>Thêm phiên bản mới</h2>
        <form action="${pageContext.request.contextPath}/admin/products/${product.id}/variants" method="post">
          <ui:csrf />
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
              <input type="hidden" name="_active" value="on">
              <input type="checkbox" name="active" value="true" checked>Đang bán</label>
          </div>
          <button class="btn btn-brand mt-3">Thêm phiên bản</button>
        </form>
      </section>
      <jsp:include page="/WEB-INF/views/shared/admin/footer.jsp" />