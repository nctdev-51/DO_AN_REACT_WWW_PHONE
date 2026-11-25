<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<jsp:include page="/WEB-INF/views/shared/admin/header.jsp" />
<div class="section-heading">
  <p class="muted">Các nhóm sản phẩm trong cửa hàng.</p>
  <a class="btn btn-brand" href="${pageContext.request.contextPath}/admin/categories/new">+ Thêm danh mục</a>
</div>
<section class="panel">
    <ui:adminNameSearch action="/admin/categories" label="Tên danh mục" query="${q}"/>
  <ui:adminBatchForm action="/admin/categories/batch">
    <jsp:attribute name="fields">
      <div data-batch-field="description">
        <label class="form-label" for="bulk-description">Mô tả mới</label>
        <input id="bulk-description" class="form-control" name="description">
      </div>
    </jsp:attribute>
    <jsp:body>
      <option value="description">Cập nhật mô tả</option>
      <option value="delete" data-destructive>Xóa danh mục</option>
    </jsp:body>
  </ui:adminBatchForm>
  <div class="table-responsive">
  <table class="table">
    <thead>
      <tr>
          <th scope="col"><input type="checkbox" data-batch-all aria-label="Chọn tất cả kết quả"></th>
        <th>Tên danh mục</th>
        <th>Mô tả</th>
        <th>Thao tác</th>
      </tr>
    </thead>
    <tbody id="admin-name-results">
      <c:forEach var="category" items="${categories}">
        <tr>
            <td><input type="checkbox" name="ids" value="${category.id}" form="admin-batch" data-batch-label="<c:out value='${category.name}'/>"
                aria-label="Chọn mục ${category.id}"></td>
          <td>
            <c:out value="${category.name}"/>
          </td>
          <td>
            <c:out value="${category.description}"/>
          </td>
          <td>
            <div class="table-actions">
              <a class="btn btn-light btn-sm" href="${pageContext.request.contextPath}/admin/categories/${category.id}/edit">Sửa</a>
              <form action="${pageContext.request.contextPath}/admin/categories/${category.id}/delete" method="post" data-confirm="Xóa danh mục này?">
                <ui:csrf/>
                <button class="btn btn-outline-danger btn-sm">Xóa</button>
              </form>
            </div>
          </td>
        </tr>
      </c:forEach>
    <c:if test="${empty categories}"><tr><td colspan="4">Không tìm thấy danh mục phù hợp.</td></tr></c:if></tbody>
  </table>
  </div>
</section>
<jsp:include page="/WEB-INF/views/shared/admin/footer.jsp" />
