<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<jsp:include page="/WEB-INF/views/shared/admin/header.jsp" />
<section class="panel form-panel">
  <h2>${empty categoryId ? 'Thêm danh mục' : 'Sửa danh mục'}</h2>
  <form:form action="${pageContext.request.contextPath}/admin/categories/save" modelAttribute="categoryForm" method="post">
    <form:errors path="*" element="div" cssClass="alert alert-danger"/>
    <c:if test="${not empty categoryId}">
      <input type="hidden" name="id" value="${categoryId}">
    </c:if>
    <div class="field">
      <label for="name">Tên danh mục</label>
      <form:input id="name" path="name" cssClass="form-control" required="required" maxlength="100"/>
    </div>
    <div class="field">
      <label for="description">Mô tả</label>
      <form:textarea id="description" path="description" cssClass="form-control" rows="3"/>
    </div>
    <button class="btn btn-brand">Lưu danh mục</button>
    <a class="btn btn-light" href="${pageContext.request.contextPath}/admin/categories">Quay lại</a>
  </form:form>
</section>
<jsp:include page="/WEB-INF/views/shared/admin/footer.jsp" />
