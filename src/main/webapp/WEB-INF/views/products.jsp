<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<jsp:include page="components/header.jsp" />

<section class="container" style="padding-top:50px;padding-bottom:80px;">
    <!-- Page header -->
    <div class="d-flex justify-content-between align-items-end mb-5">
        <div>
            <span class="section-badge section-badge-primary mb-3">
                <i class="bi bi-grid-3x3-gap me-1"></i> Khám phá
            </span>
            <h1 style="font-weight:800;font-size:2.2rem;letter-spacing:-0.5px;margin-bottom:8px;color:var(--text-primary);">Điện thoại</h1>
            <p style="color:var(--text-secondary);margin-bottom:0;font-size:0.95rem;">Tìm kiếm và lọc sản phẩm theo hãng, danh mục, khoảng giá.</p>
        </div>
        <span style="background:var(--brand-yellow-muted);color:var(--brand-yellow);padding:10px 20px;border-radius:999px;font-size:0.9rem;font-weight:600;border:1px solid var(--brand-yellow-glow);">${products.totalElements} sản phẩm</span>
    </div>

    <!-- Filter Form -->
    <form class="card-modern" style="padding:28px;margin-bottom:40px;border-radius:20px;" action="${pageContext.request.contextPath}/products" method="get">
        <div class="row g-4 align-items-end">
            <div class="col-lg-3 col-md-6">
                <label style="font-size:0.85rem;font-weight:600;color:var(--text-secondary);margin-bottom:8px;display:block;" for="keyword">Tên sản phẩm</label>
                <input style="background:var(--surface-primary);color:var(--text-primary);border-radius:12px;border:1.5px solid var(--border-primary);padding:12px 16px;font-size:0.95rem;width:100%;transition:var(--transition-base);outline:none;" class="form-control" id="keyword" name="keyword" value="<c:out value='${keyword}'/>" placeholder="Ví dụ: Galaxy S25" onfocus="this.style.borderColor='var(--brand-yellow)';this.style.boxShadow='0 0 0 3px var(--brand-yellow-muted)'" onblur="this.style.borderColor='var(--border-primary)';this.style.boxShadow='none'">
            </div>
            <div class="col-lg-2 col-md-6">
                <label style="font-size:0.85rem;font-weight:600;color:var(--text-secondary);margin-bottom:8px;display:block;" for="brand">Hãng</label>
                <select style="background:var(--surface-primary);color:var(--text-primary);border-radius:12px;border:1.5px solid var(--border-primary);padding:12px 16px;font-size:0.95rem;width:100%;outline:none;" class="form-select" id="brand" name="brand">
                    <option value="">Tất cả hãng</option>
                    <c:forEach items="${brands}" var="item">
                        <option value="<c:out value='${item}'/>" ${brand == item ? 'selected' : ''}><c:out value="${item}"/></option>
                    </c:forEach>
                </select>
            </div>
            <div class="col-lg-2 col-md-6">
                <label style="font-size:0.85rem;font-weight:600;color:var(--text-secondary);margin-bottom:8px;display:block;" for="categoryId">Danh mục</label>
                <select style="background:var(--surface-primary);color:var(--text-primary);border-radius:12px;border:1.5px solid var(--border-primary);padding:12px 16px;font-size:0.95rem;width:100%;outline:none;" class="form-select" id="categoryId" name="categoryId">
                    <option value="">Tất cả danh mục</option>
                    <c:forEach items="${categories}" var="category">
                        <option value="${category.id}" ${categoryId == category.id ? 'selected' : ''}><c:out value="${category.name}"/></option>
                    </c:forEach>
                </select>
            </div>
            <div class="col-lg-2 col-md-6">
                <label style="font-size:0.85rem;font-weight:600;color:var(--text-secondary);margin-bottom:8px;display:block;" for="minPrice">Giá từ</label>
                <input style="background:var(--surface-primary);color:var(--text-primary);border-radius:12px;border:1.5px solid var(--border-primary);padding:12px 16px;font-size:0.95rem;width:100%;outline:none;" class="form-control" type="number" min="0" step="1000" id="minPrice" name="minPrice" value="${minPrice}">
            </div>
            <div class="col-lg-2 col-md-6">
                <label style="font-size:0.85rem;font-weight:600;color:var(--text-secondary);margin-bottom:8px;display:block;" for="maxPrice">Giá đến</label>
                <input style="background:var(--surface-primary);color:var(--text-primary);border-radius:12px;border:1.5px solid var(--border-primary);padding:12px 16px;font-size:0.95rem;width:100%;outline:none;" class="form-control" type="number" min="0" step="1000" id="maxPrice" name="maxPrice" value="${maxPrice}">
            </div>
            <div class="col-lg-1 col-md-6 d-grid">
                <button class="btn-gradient" type="submit" style="padding:12px;font-size:0.95rem;border-radius:12px;"><i class="bi bi-funnel me-1"></i>Lọc</button>
            </div>
        </div>
    </form>

    <!-- Products Grid -->
    <div class="row g-4">
        <c:forEach items="${products.content}" var="product" varStatus="loop">
            <div class="col-xl-3 col-lg-4 col-md-6 animate-fadeInUp" style="animation-delay:${loop.index * 0.05}s;">
                <div class="card-modern h-100" style="border-radius:20px;">
                    <a class="d-block" href="${pageContext.request.contextPath}/products/${product.id}" style="padding:30px;background:var(--surface-secondary);text-align:center;text-decoration:none;border-bottom:1px solid var(--border-primary);">
                        <c:choose>
                            <c:when test="${not empty product.image}">
                                <img src="${pageContext.request.contextPath}${product.image}" alt="<c:out value='${product.name}'/>" class="img-fluid" style="height:180px;object-fit:contain;transition:transform 0.4s ease;" onmouseover="this.style.transform='scale(1.08)'" onmouseout="this.style.transform='scale(1)'">
                            </c:when>
                            <c:otherwise><i class="bi bi-phone" style="font-size:6rem;color:var(--text-tertiary);"></i></c:otherwise>
                        </c:choose>
                    </a>
                    <div style="padding:24px;">
                        <div style="font-size:0.75rem;font-weight:700;text-transform:uppercase;letter-spacing:1px;color:var(--text-secondary);margin-bottom:8px;">
                            <c:out value="${product.brand}"/>
                        </div>
                        <h5 style="font-weight:700;font-size:1.1rem;margin-bottom:12px;line-height:1.4;">
                            <a style="color:var(--text-primary);text-decoration:none;transition:color 0.2s ease;" href="${pageContext.request.contextPath}/products/${product.id}" onmouseover="this.style.color='var(--brand-yellow)'" onmouseout="this.style.color='var(--text-primary)'">
                                <c:out value="${product.name}"/>
                            </a>
                        </h5>
                        <div class="price-tag" style="font-size:1.25rem;margin-bottom:20px;">
                            <fmt:formatNumber value="${product.price}" pattern="#,##0"/> ₫
                        </div>
                        <a class="btn-ghost d-block text-center w-100" style="padding:12px;border-radius:12px;" href="${pageContext.request.contextPath}/products/${product.id}">
                            Xem chi tiết
                        </a>
                    </div>
                </div>
            </div>
        </c:forEach>
        <c:if test="${empty products.content}">
            <div class="col-12">
                <div class="card-modern" style="text-align:center;padding:80px 20px;">
                    <i class="bi bi-search" style="font-size:3.5rem;color:var(--text-tertiary);display:block;margin-bottom:16px;"></i>
                    <p style="color:var(--text-secondary);font-size:1.1rem;margin:0;">Không tìm thấy sản phẩm phù hợp.</p>
                </div>
            </div>
        </c:if>
    </div>

    <!-- Pagination -->
    <c:if test="${products.totalPages > 1}">
        <nav class="mt-5 pt-4" aria-label="Phân trang sản phẩm">
            <ul class="pagination justify-content-center gap-2" style="margin:0;">
                <li class="page-item ${products.first ? 'disabled' : ''}">
                    <c:url var="previousUrl" value="/products">
                        <c:param name="keyword" value="${keyword}"/><c:param name="brand" value="${brand}"/>
                        <c:param name="categoryId" value="${categoryId}"/><c:param name="minPrice" value="${minPrice}"/>
                        <c:param name="maxPrice" value="${maxPrice}"/><c:param name="page" value="${products.number - 1}"/>
                    </c:url>
                    <a class="btn-ghost" style="padding:8px 16px;${products.first ? 'opacity:0.5;pointer-events:none;' : ''}" href="${previousUrl}">Trước</a>
                </li>
                <c:forEach begin="0" end="${products.totalPages - 1}" var="pageNumber">
                    <li class="page-item">
                        <c:url var="pageUrl" value="/products">
                            <c:param name="keyword" value="${keyword}"/><c:param name="brand" value="${brand}"/>
                            <c:param name="categoryId" value="${categoryId}"/><c:param name="minPrice" value="${minPrice}"/>
                            <c:param name="maxPrice" value="${maxPrice}"/><c:param name="page" value="${pageNumber}"/>
                        </c:url>
                        <a class="${products.number == pageNumber ? 'btn-gradient' : 'btn-ghost'}" style="padding:8px 16px;" href="${pageUrl}">${pageNumber + 1}</a>
                    </li>
                </c:forEach>
                <li class="page-item ${products.last ? 'disabled' : ''}">
                    <c:url var="nextUrl" value="/products">
                        <c:param name="keyword" value="${keyword}"/><c:param name="brand" value="${brand}"/>
                        <c:param name="categoryId" value="${categoryId}"/><c:param name="minPrice" value="${minPrice}"/>
                        <c:param name="maxPrice" value="${maxPrice}"/><c:param name="page" value="${products.number + 1}"/>
                    </c:url>
                    <a class="btn-ghost" style="padding:8px 16px;${products.last ? 'opacity:0.5;pointer-events:none;' : ''}" href="${nextUrl}">Sau</a>
                </li>
            </ul>
        </nav>
    </c:if>
</section>
<jsp:include page="components/footer.jsp" />
