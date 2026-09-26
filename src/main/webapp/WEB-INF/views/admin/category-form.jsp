<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<jsp:include page="../components/header.jsp" />

<section class="container py-5" style="max-width: 750px">
    <h1 class="fw-bold mb-4">${empty categoryId ? 'Thêm danh mục' : 'Cập nhật danh mục'}</h1>
    <form:form modelAttribute="categoryForm" action="${pageContext.request.contextPath}/admin/categories/save" method="post" cssClass="card card-body border-0 shadow-sm">
        <c:if test="${not empty categoryId}"><input type="hidden" name="id" value="${categoryId}"></c:if>
        <div class="mb-3"><label class="form-label" for="name">Tên danh mục</label><form:input path="name" cssClass="form-control" id="name"/><form:errors path="name" cssClass="text-danger small"/></div>
        <div class="mb-3"><label class="form-label" for="description">Mô tả</label><form:textarea path="description" cssClass="form-control" id="description" rows="4"/></div>
        <div class="d-flex gap-2"><button class="btn btn-primary" type="submit">Lưu danh mục</button><a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/admin/categories">Hủy</a></div>
    </form:form>
</section>
<jsp:include page="../components/footer.jsp" />
