<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<jsp:include page="/WEB-INF/views/shared/admin/header.jsp" />
<div class="section-heading">
  <p class="muted">Các nhóm sản phẩm trong cửa hàng.</p>
  <a class="btn btn-brand" href="${pageContext.request.contextPath}/admin/categories/new">+ Thêm danh mục</a>
</div>
<section class="panel">
  <div class="table-responsive">
  <table class="table">
    <thead>
      <tr>
        <th>Tên danh mục</th>
        <th>Mô tả</th>
        <th>Thao tác</th>
      </tr>
    </thead>
    <tbody>
      <c:forEach var="category" items="${categories}">
        <tr>
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
    </tbody>
  </table>
  </div>
</section>
<jsp:include page="/WEB-INF/views/shared/admin/footer.jsp" />
