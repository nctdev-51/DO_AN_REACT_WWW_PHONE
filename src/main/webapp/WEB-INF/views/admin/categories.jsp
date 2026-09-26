<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="../components/header.jsp" />

<section class="container py-5">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <div><h1 class="fw-bold mb-1">Quản lý danh mục</h1><a href="${pageContext.request.contextPath}/admin/products">Quản lý sản phẩm</a></div>
        <div class="d-flex gap-2">
            <form action="${pageContext.request.contextPath}/logout" method="post">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                <button class="btn btn-outline-secondary" type="submit">Đăng xuất</button>
            </form>
            <a class="btn btn-primary" href="${pageContext.request.contextPath}/admin/categories/new"><i class="bi bi-plus-lg me-1"></i>Thêm danh mục</a>
        </div>
    </div>
    <c:if test="${not empty message}"><div class="alert alert-success"><c:out value="${message}"/></div></c:if>
    <c:if test="${not empty error}"><div class="alert alert-danger"><c:out value="${error}"/></div></c:if>
    <div class="table-responsive bg-white shadow-sm rounded-3">
        <table class="table align-middle mb-0">
            <thead class="table-light"><tr><th>Tên danh mục</th><th>Mô tả</th><th class="text-end">Thao tác</th></tr></thead>
            <tbody>
            <c:forEach items="${categories}" var="category">
                <tr>
                    <td><c:out value="${category.name}"/></td>
                    <td><c:out value="${category.description}"/></td>
                    <td class="text-end text-nowrap">
                        <a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/admin/categories/${category.id}/edit">Sửa</a>
                        <form class="d-inline" action="${pageContext.request.contextPath}/admin/categories/${category.id}/delete" method="post" onsubmit="return confirm('Xóa danh mục này?')">
                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                            <button class="btn btn-sm btn-outline-danger" type="submit">Xóa</button>
                        </form>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty categories}"><tr><td colspan="3" class="text-center py-4 text-muted">Chưa có danh mục.</td></tr></c:if>
            </tbody>
        </table>
    </div>
</section>
<jsp:include page="../components/footer.jsp" />
