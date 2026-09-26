<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<jsp:include page="components/header.jsp" />

<section class="container py-5">
    <div class="row g-5">
        <div class="col-md-6">
            <div class="bg-white rounded-4 shadow-sm p-4 text-center">
                <c:choose>
                    <c:when test="${not empty product.image}">
                        <img class="img-fluid" src="${pageContext.request.contextPath}${product.image}" alt="<c:out value='${product.name}'/>" style="max-height: 440px; object-fit: contain;">
                    </c:when>
                    <c:otherwise><i class="bi bi-phone text-secondary" style="font-size: 12rem;"></i></c:otherwise>
                </c:choose>
            </div>
        </div>
        <div class="col-md-6">
            <a class="text-decoration-none" href="${pageContext.request.contextPath}/products">← Tất cả sản phẩm</a>
            <div class="text-uppercase text-primary fw-semibold mt-4"><c:out value="${product.brand}"/></div>
            <h1 class="fw-bold mt-2"><c:out value="${product.name}"/></h1>
            <div class="text-danger fs-2 fw-bold my-3"><fmt:formatNumber value="${product.price}" pattern="#,##0"/>₫</div>
            <div class="text-muted mb-4">
                <strong>Danh mục:</strong>
                <c:out value="${product.category.name}" default="Chưa phân loại"/>
            </div>
            <h5 class="fw-bold">Mô tả sản phẩm</h5>
            <p class="text-muted" style="white-space: pre-line"><c:out value="${product.description}" default="Chưa có mô tả."/></p>
            <form class="d-flex gap-3 mt-4" action="${pageContext.request.contextPath}/cart/add" method="post">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                <input type="hidden" name="productId" value="${product.id}">
                <input class="form-control" style="max-width: 100px" type="number" name="quantity" value="1" min="1" max="99" aria-label="Số lượng">
                <button class="btn btn-primary px-4" type="submit"><i class="bi bi-cart-plus me-2"></i>Thêm vào giỏ</button>
            </form>
        </div>
    </div>
</section>
<jsp:include page="components/footer.jsp" />
