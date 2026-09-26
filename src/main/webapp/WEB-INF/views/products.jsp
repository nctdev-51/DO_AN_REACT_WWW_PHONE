<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<jsp:include page="components/header.jsp" />

<section class="container py-5">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <div>
            <h1 class="fw-bold mb-1">Điện thoại</h1>
            <p class="text-muted mb-0">Tìm kiếm và lọc sản phẩm theo hãng, danh mục, khoảng giá.</p>
        </div>
        <span class="text-muted">${products.totalElements} sản phẩm</span>
    </div>

    <form class="card card-body border-0 shadow-sm mb-4" action="${pageContext.request.contextPath}/products" method="get">
        <div class="row g-3 align-items-end">
            <div class="col-lg-3 col-md-6">
                <label class="form-label" for="keyword">Tên sản phẩm</label>
                <input class="form-control" id="keyword" name="keyword" value="<c:out value='${keyword}'/>" placeholder="Ví dụ: Galaxy S25">
            </div>
            <div class="col-lg-2 col-md-6">
                <label class="form-label" for="brand">Hãng</label>
                <select class="form-select" id="brand" name="brand">
                    <option value="">Tất cả hãng</option>
                    <c:forEach items="${brands}" var="item">
                        <option value="<c:out value='${item}'/>" ${brand == item ? 'selected' : ''}><c:out value="${item}"/></option>
                    </c:forEach>
                </select>
            </div>
            <div class="col-lg-2 col-md-6">
                <label class="form-label" for="categoryId">Danh mục</label>
                <select class="form-select" id="categoryId" name="categoryId">
                    <option value="">Tất cả danh mục</option>
                    <c:forEach items="${categories}" var="category">
                        <option value="${category.id}" ${categoryId == category.id ? 'selected' : ''}><c:out value="${category.name}"/></option>
                    </c:forEach>
                </select>
            </div>
            <div class="col-lg-2 col-md-6">
                <label class="form-label" for="minPrice">Giá từ</label>
                <input class="form-control" type="number" min="0" step="1000" id="minPrice" name="minPrice" value="${minPrice}">
            </div>
            <div class="col-lg-2 col-md-6">
                <label class="form-label" for="maxPrice">Giá đến</label>
                <input class="form-control" type="number" min="0" step="1000" id="maxPrice" name="maxPrice" value="${maxPrice}">
            </div>
            <div class="col-lg-1 col-md-6 d-grid">
                <button class="btn btn-primary" type="submit">Lọc</button>
            </div>
        </div>
    </form>

    <div class="row g-4">
        <c:forEach items="${products.content}" var="product">
            <div class="col-xl-3 col-lg-4 col-md-6">
                <div class="card h-100 border-0 shadow-sm">
                    <a class="bg-light text-center p-4 d-block" href="${pageContext.request.contextPath}/products/${product.id}">
                        <c:choose>
                            <c:when test="${not empty product.image}">
                                <img src="${pageContext.request.contextPath}${product.image}" alt="<c:out value='${product.name}'/>" class="img-fluid" style="height: 180px; object-fit: contain;">
                            </c:when>
                            <c:otherwise><i class="bi bi-phone text-secondary" style="font-size: 6rem;"></i></c:otherwise>
                        </c:choose>
                    </a>
                    <div class="card-body d-flex flex-column">
                        <small class="text-muted text-uppercase"><c:out value="${product.brand}"/></small>
                        <h5 class="mt-1"><a class="text-decoration-none text-dark" href="${pageContext.request.contextPath}/products/${product.id}"><c:out value="${product.name}"/></a></h5>
                        <div class="text-danger fs-5 fw-bold mb-3"><fmt:formatNumber value="${product.price}" pattern="#,##0"/>₫</div>
                        <a class="btn btn-outline-primary mt-auto" href="${pageContext.request.contextPath}/products/${product.id}">Xem chi tiết</a>
                    </div>
                </div>
            </div>
        </c:forEach>
        <c:if test="${empty products.content}">
            <div class="col-12"><div class="alert alert-light text-center">Không tìm thấy sản phẩm phù hợp.</div></div>
        </c:if>
    </div>

    <c:if test="${products.totalPages > 1}">
        <nav class="mt-4" aria-label="Phân trang sản phẩm">
            <ul class="pagination justify-content-center">
                <li class="page-item ${products.first ? 'disabled' : ''}">
                    <c:url var="previousUrl" value="/products">
                        <c:param name="keyword" value="${keyword}"/><c:param name="brand" value="${brand}"/>
                        <c:param name="categoryId" value="${categoryId}"/><c:param name="minPrice" value="${minPrice}"/>
                        <c:param name="maxPrice" value="${maxPrice}"/><c:param name="page" value="${products.number - 1}"/>
                    </c:url>
                    <a class="page-link" href="${previousUrl}">Trước</a>
                </li>
                <c:forEach begin="0" end="${products.totalPages - 1}" var="pageNumber">
                    <li class="page-item ${products.number == pageNumber ? 'active' : ''}">
                        <c:url var="pageUrl" value="/products">
                            <c:param name="keyword" value="${keyword}"/><c:param name="brand" value="${brand}"/>
                            <c:param name="categoryId" value="${categoryId}"/><c:param name="minPrice" value="${minPrice}"/>
                            <c:param name="maxPrice" value="${maxPrice}"/><c:param name="page" value="${pageNumber}"/>
                        </c:url>
                        <a class="page-link" href="${pageUrl}">${pageNumber + 1}</a>
                    </li>
                </c:forEach>
                <li class="page-item ${products.last ? 'disabled' : ''}">
                    <c:url var="nextUrl" value="/products">
                        <c:param name="keyword" value="${keyword}"/><c:param name="brand" value="${brand}"/>
                        <c:param name="categoryId" value="${categoryId}"/><c:param name="minPrice" value="${minPrice}"/>
                        <c:param name="maxPrice" value="${maxPrice}"/><c:param name="page" value="${products.number + 1}"/>
                    </c:url>
                    <a class="page-link" href="${nextUrl}">Sau</a>
                </li>
            </ul>
        </nav>
    </c:if>
</section>
<jsp:include page="components/footer.jsp" />
