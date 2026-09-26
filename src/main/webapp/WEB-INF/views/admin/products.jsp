<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<jsp:include page="../components/header.jsp" />

<section class="container py-5">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <div><h1 class="fw-bold mb-1">Quản lý sản phẩm</h1><a href="${pageContext.request.contextPath}/admin/categories">Quản lý danh mục</a></div>
        <div class="d-flex gap-2">
            <form action="${pageContext.request.contextPath}/admin/logout" method="post">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                <button class="btn btn-outline-secondary" type="submit">Đăng xuất</button>
            </form>
            <a class="btn btn-primary" href="${pageContext.request.contextPath}/admin/products/new"><i class="bi bi-plus-lg me-1"></i>Thêm sản phẩm</a>
        </div>
    </div>
    <c:if test="${not empty message}"><div class="alert alert-success"><c:out value="${message}"/></div></c:if>
    <div class="table-responsive bg-white shadow-sm rounded-3">
        <table class="table align-middle mb-0">
            <thead class="table-light"><tr><th>Sản phẩm</th><th>Hãng</th><th>Danh mục</th><th>Giá</th><th>Trạng thái</th><th class="text-end">Thao tác</th></tr></thead>
            <tbody>
            <c:forEach items="${products}" var="product">
                <tr>
                    <td><c:out value="${product.name}"/></td>
                    <td><c:out value="${product.brand}"/></td>
                    <td><c:out value="${product.category.name}" default="—"/></td>
                    <td><fmt:formatNumber value="${product.price}" pattern="#,##0"/>₫</td>
                    <td><span class="badge ${product.status == 1 ? 'text-bg-success' : 'text-bg-secondary'}">${product.status == 1 ? 'Đang bán' : 'Ngừng bán'}</span></td>
                    <td class="text-end text-nowrap">
                        <a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/admin/products/${product.id}/edit">Sửa</a>
                        <form class="d-inline" action="${pageContext.request.contextPath}/admin/products/${product.id}/delete" method="post" onsubmit="return confirm('Xóa hoặc ngừng kinh doanh sản phẩm này?')">
                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                            <button class="btn btn-sm btn-outline-danger" type="submit">Xóa</button>
                        </form>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty products}"><tr><td colspan="6" class="text-center py-4 text-muted">Chưa có sản phẩm.</td></tr></c:if>
            </tbody>
        </table>
    </div>
</section>
<jsp:include page="../components/footer.jsp" />
