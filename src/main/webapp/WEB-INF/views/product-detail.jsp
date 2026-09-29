<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<jsp:include page="components/header.jsp" />

<section class="container" style="padding-top:60px;padding-bottom:80px;">
    <div class="row g-5">
        <!-- Product Image -->
        <div class="col-md-6 animate-fadeInUp">
            <div class="card-modern" style="border-radius:24px;padding:50px;text-align:center;position:relative;overflow:hidden;height:100%;display:flex;align-items:center;justify-content:center;">
                <!-- Decorative accent -->
                <div style="position:absolute;top:-50px;right:-50px;width:200px;height:200px;border-radius:50%;background:radial-gradient(circle,var(--brand-yellow-muted) 0%,transparent 70%);"></div>
                <c:choose>
                    <c:when test="${not empty product.image}">
                        <img class="img-fluid" src="${pageContext.request.contextPath}${product.image}" alt="<c:out value='${product.name}'/>" style="max-height:480px;object-fit:contain;position:relative;z-index:2;transition:transform 0.5s ease;" onmouseover="this.style.transform='scale(1.1)'" onmouseout="this.style.transform='scale(1)'">
                    </c:when>
                    <c:otherwise>
                        <i class="bi bi-phone" style="font-size:12rem;color:var(--text-tertiary);"></i>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

        <!-- Product Info -->
        <div class="col-md-6 animate-fadeInUp" style="animation-delay:0.15s;">
            <a class="btn-ghost" style="display:inline-flex;align-items:center;gap:8px;padding:8px 16px;border-radius:999px;margin-bottom:30px;font-size:0.9rem;" href="${pageContext.request.contextPath}/products">
                <i class="bi bi-arrow-left"></i> Tất cả sản phẩm
            </a>

            <div style="font-size:0.85rem;font-weight:700;text-transform:uppercase;letter-spacing:1.5px;color:var(--text-secondary);margin-bottom:12px;">
                <c:out value="${product.brand}"/>
            </div>

            <h1 style="font-weight:900;font-size:2.4rem;letter-spacing:-0.5px;color:var(--text-primary);margin-bottom:20px;line-height:1.2;">
                <c:out value="${product.name}"/>
            </h1>

            <div class="price-tag" style="font-size:2.5rem;margin-bottom:24px;">
                <fmt:formatNumber value="${product.price}" pattern="#,##0"/> ₫
            </div>

            <div style="display:inline-flex;align-items:center;gap:10px;padding:12px 20px;background:var(--surface-secondary);border:1px solid var(--border-primary);border-radius:12px;margin-bottom:30px;font-size:0.95rem;color:var(--text-primary);">
                <i class="bi bi-tag-fill" style="color:var(--brand-yellow);"></i>
                <strong style="color:var(--text-secondary);">Danh mục:</strong>
                <c:out value="${product.category.name}" default="Chưa phân loại"/>
            </div>

            <h5 style="font-weight:800;font-size:1.2rem;color:var(--text-primary);margin-bottom:16px;">Đặc điểm nổi bật</h5>
            <div style="color:var(--text-secondary);line-height:1.8;font-size:1rem;margin-bottom:40px;background:var(--surface-secondary);padding:24px;border-radius:16px;border:1px solid var(--border-primary);">
                <p style="white-space:pre-line;margin:0;">
                    <c:out value="${product.description}" default="Chưa có thông tin chi tiết."/>
                </p>
            </div>

            <!-- Add to Cart form -->
            <form class="d-flex gap-3 align-items-center" action="${pageContext.request.contextPath}/cart/add" method="post" style="flex-wrap:wrap;padding:24px;background:var(--surface-secondary);border-radius:20px;border:1px solid var(--border-primary);">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                <input type="hidden" name="productId" value="${product.id}">
                <div style="display:flex;align-items:center;gap:16px;width:100%;">
                    <div style="background:var(--surface-primary);border:1px solid var(--border-primary);border-radius:12px;padding:4px;display:flex;">
                        <input style="width:70px;background:transparent;border:none;color:var(--text-primary);padding:10px;font-size:1.1rem;text-align:center;font-weight:700;outline:none;" type="number" name="quantity" value="1" min="1" max="99" aria-label="Số lượng">
                    </div>
                    <button class="btn-gradient flex-grow-1" type="submit" style="padding:16px;font-size:1.1rem;border-radius:12px;">
                        <i class="bi bi-cart-plus-fill me-2"></i>Thêm vào giỏ hàng
                    </button>
                </div>
            </form>

            <!-- Trust badges -->
            <div class="d-flex flex-wrap gap-4 mt-5">
                <div style="display:flex;align-items:center;gap:8px;font-size:0.9rem;color:var(--text-secondary);font-weight:600;">
                    <i class="bi bi-shield-check" style="color:var(--color-success);font-size:1.2rem;"></i> Chính hãng 100%
                </div>
                <div style="display:flex;align-items:center;gap:8px;font-size:0.9rem;color:var(--text-secondary);font-weight:600;">
                    <i class="bi bi-truck" style="color:var(--brand-yellow);font-size:1.2rem;"></i> Giao toàn quốc
                </div>
                <div style="display:flex;align-items:center;gap:8px;font-size:0.9rem;color:var(--text-secondary);font-weight:600;">
                    <i class="bi bi-arrow-repeat" style="color:var(--color-info);font-size:1.2rem;"></i> Đổi trả 30 ngày
                </div>
            </div>
        </div>
    </div>
</section>
<jsp:include page="components/footer.jsp" />
