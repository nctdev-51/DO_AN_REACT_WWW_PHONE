<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<!-- Include Header Component -->
<jsp:include page="components/header.jsp" />

<!-- Hero Section -->
<section style="background: linear-gradient(135deg, #FFF6D9 0%, #FFFDF7 100%); border-bottom: 1px solid var(--border-primary); position: relative; overflow: hidden;">
    <!-- Background decorative elements -->
    <div style="position:absolute;top:0;right:0;width:50vw;height:100%;background:radial-gradient(ellipse at right center, var(--brand-yellow-muted) 0%, transparent 60%); pointer-events: none;"></div>

    <div class="container" style="padding-top:80px;padding-bottom:80px;position:relative;z-index:2;">
        <div class="row align-items-center g-5">
            <div class="col-lg-7 animate-fadeInUp">
                <span class="section-badge section-badge-primary mb-4" style="background:var(--brand-yellow-muted);border:1px solid var(--brand-yellow-glow);">
                    <i class="bi bi-lightning-charge-fill me-1"></i> Ưu đãi mùa tựu trường & Ra mắt siêu phẩm mới
                </span>
                <!-- Hiển thị chuỗi truyền từ Controller qua Expression Language (EL) -->
                <h1 style="font-size:clamp(2.5rem,5vw,3.8rem);font-weight:900;color:var(--text-primary);line-height:1.15;margin-bottom:24px;letter-spacing:-1px;">
                    ${chuoi}
                </h1>
                <p style="font-size:1.1rem;color:var(--text-secondary);line-height:1.8;margin-bottom:40px;max-width:600px;">
                    Khám phá các mẫu Smartphone cao cấp nhất từ iPhone 16 Pro Max, Samsung Galaxy S25 Ultra đến Xiaomi 14 series với giá ưu đãi đặc quyền và bảo hành toàn diện.
                </p>
                <div class="d-flex flex-wrap gap-3">
                    <a href="${pageContext.request.contextPath}/products" class="btn-gradient" style="padding:16px 36px;font-size:1.05rem;">
                        <i class="bi bi-cart me-2"></i> Mua ngay
                    </a>
                    <a href="#featured" class="btn-ghost" style="padding:16px 36px;font-size:1.05rem;" onmouseover="this.style.background='var(--surface-secondary)'" onmouseout="this.style.background='transparent'">
                        <i class="bi bi-fire me-2" style="color:var(--brand-yellow);"></i> Sản phẩm bán chạy
                    </a>
                </div>
            </div>
            <div class="col-lg-5 animate-fadeInUp" style="animation-delay:0.2s;">
                <div id="heroCarousel" class="carousel slide carousel-fade" data-bs-ride="carousel" data-bs-interval="3000" style="position:relative; width: 100%;">
                    <!-- Glow effect -->
                    <div style="position:absolute;top:50%;left:50%;transform:translate(-50%,-50%);width:300px;height:300px;border-radius:50%;background:var(--brand-yellow-muted);filter:blur(40px);z-index:0;"></div>
                    
                    <div class="carousel-inner" style="position:relative;background:var(--glass-bg);border:1px solid var(--glass-border);border-radius:24px;backdrop-filter:blur(10px);-webkit-backdrop-filter:blur(10px);z-index:1;box-shadow:var(--shadow-md);">
                        <c:choose>
                            <c:when test="${not empty featuredProducts}">
                                <c:forEach items="${featuredProducts}" var="product" varStatus="loop">
                                    <div class="carousel-item ${loop.first ? 'active' : ''}">
                                        <a href="${pageContext.request.contextPath}/products/${product.id}" style="display:block;padding:40px 30px;text-align:center;text-decoration:none;">
                                            <c:choose>
                                                <c:when test="${not empty product.image}">
                                                    <img src="${pageContext.request.contextPath}${product.image}" alt="<c:out value='${product.name}'/>" style="height:220px;object-fit:contain;margin-bottom:20px;transition:transform 0.5s ease;" onmouseover="this.style.transform='scale(1.05)'" onmouseout="this.style.transform='scale(1)'">
                                                </c:when>
                                                <c:otherwise>
                                                    <i class="bi bi-phone" style="font-size:9rem;color:var(--text-tertiary);line-height:1;margin-bottom:20px;display:inline-block;"></i>
                                                </c:otherwise>
                                            </c:choose>
                                            <h4 style="font-weight:800;color:var(--text-primary);margin-bottom:8px;font-size:1.3rem;line-height:1.3;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;"><c:out value="${product.name}"/></h4>
                                            <div class="price-tag" style="font-size:1.2rem;"><fmt:formatNumber value="${product.price}" pattern="#,##0"/> ₫</div>
                                        </a>
                                    </div>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <!-- Fallback if no products -->
                                <div class="carousel-item active" style="padding:50px;text-align:center;">
                                    <i class="bi bi-phone-vibrate" style="font-size:7rem;color:var(--brand-yellow);filter:drop-shadow(0 0 15px rgba(252,213,53,0.4));"></i>
                                    <h4 style="font-weight:800;color:var(--text-primary);margin-top:24px;font-size:1.5rem;">Flagship Phone 2025</h4>
                                    <p style="color:var(--text-secondary);font-size:0.95rem;margin-bottom:0;">Thiết kế titan, camera 200MP</p>
                                </div>
                                <div class="carousel-item" style="padding:50px;text-align:center;">
                                    <i class="bi bi-phone-landscape" style="font-size:7rem;color:var(--brand-yellow);filter:drop-shadow(0 0 15px rgba(252,213,53,0.4));"></i>
                                    <h4 style="font-weight:800;color:var(--text-primary);margin-top:24px;font-size:1.5rem;">Cấu Hình Cực Khủng</h4>
                                    <p style="color:var(--text-secondary);font-size:0.95rem;margin-bottom:0;">Chiến mọi tựa game mượt mà</p>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <!-- Carousel Controls -->
                    <button class="carousel-control-prev" type="button" data-bs-target="#heroCarousel" data-bs-slide="prev" style="width:12%;z-index:2;opacity:0.8;">
                        <span class="carousel-control-prev-icon" aria-hidden="true" style="filter:invert(1) grayscale(100) brightness(10%);"></span>
                        <span class="visually-hidden">Previous</span>
                    </button>
                    <button class="carousel-control-next" type="button" data-bs-target="#heroCarousel" data-bs-slide="next" style="width:12%;z-index:2;opacity:0.8;">
                        <span class="carousel-control-next-icon" aria-hidden="true" style="filter:invert(1) grayscale(100) brightness(10%);"></span>
                        <span class="visually-hidden">Next</span>
                    </button>
                </div>
            </div>
        </div>
    </div>
</section>

<!-- Trust & Feature Badges -->
<section style="padding:0;position:relative;z-index:5;margin-top:-50px;">
    <div class="container">
        <div class="row g-4">
            <div class="col-md-3 col-6">
                <div class="card-modern" style="padding:30px 20px;text-align:center;height:100%; border-radius: 20px;">
                    <div style="width:56px;height:56px;border-radius:14px;background:var(--brand-yellow-muted);display:flex;align-items:center;justify-content:center;margin:0 auto 16px;">
                        <i class="bi bi-shield-check" style="font-size:1.6rem;color:var(--brand-yellow);"></i>
                    </div>
                    <h6 style="font-weight:700;font-size:0.95rem;margin-bottom:6px;color:var(--text-primary);">Chính Hãng 100%</h6>
                    <small style="color:var(--text-secondary);font-size:0.8rem;">Bảo hành 12 tháng Apple & Samsung</small>
                </div>
            </div>
            <div class="col-md-3 col-6">
                <div class="card-modern" style="padding:30px 20px;text-align:center;height:100%; border-radius: 20px;">
                    <div style="width:56px;height:56px;border-radius:14px;background:rgba(14, 203, 129, 0.12);display:flex;align-items:center;justify-content:center;margin:0 auto 16px;">
                        <i class="bi bi-truck" style="font-size:1.6rem;color:var(--color-success);"></i>
                    </div>
                    <h6 style="font-weight:700;font-size:0.95rem;margin-bottom:6px;color:var(--text-primary);">Giao Nhanh 2 Giờ</h6>
                    <small style="color:var(--text-secondary);font-size:0.8rem;">Miễn phí vận chuyển đơn từ 2 triệu</small>
                </div>
            </div>
            <div class="col-md-3 col-6">
                <div class="card-modern" style="padding:30px 20px;text-align:center;height:100%; border-radius: 20px;">
                    <div style="width:56px;height:56px;border-radius:14px;background:rgba(240, 185, 11, 0.12);display:flex;align-items:center;justify-content:center;margin:0 auto 16px;">
                        <i class="bi bi-arrow-repeat" style="font-size:1.6rem;color:var(--color-warning);"></i>
                    </div>
                    <h6 style="font-weight:700;font-size:0.95rem;margin-bottom:6px;color:var(--text-primary);">1 Đổi 1 Trong 30 Ngày</h6>
                    <small style="color:var(--text-secondary);font-size:0.8rem;">Nếu phát sinh lỗi từ nhà sản xuất</small>
                </div>
            </div>
            <div class="col-md-3 col-6">
                <div class="card-modern" style="padding:30px 20px;text-align:center;height:100%; border-radius: 20px;">
                    <div style="width:56px;height:56px;border-radius:14px;background:rgba(246, 70, 93, 0.12);display:flex;align-items:center;justify-content:center;margin:0 auto 16px;">
                        <i class="bi bi-credit-card" style="font-size:1.6rem;color:var(--color-danger);"></i>
                    </div>
                    <h6 style="font-weight:700;font-size:0.95rem;margin-bottom:6px;color:var(--text-primary);">Trả Góp 0% Lãi Suất</h6>
                    <small style="color:var(--text-secondary);font-size:0.8rem;">Thủ tục đơn giản qua thẻ tín dụng</small>
                </div>
            </div>
        </div>
    </div>
</section>

<!-- Featured Products Showcase -->
<section class="py-5" id="featured" style="padding-top:80px !important;">
    <div class="container">
        <div class="d-flex justify-content-between align-items-end mb-5">
            <div>
                <span class="section-badge section-badge-primary mb-3">
                    <i class="bi bi-stars me-1"></i> Danh mục nổi bật
                </span>
                <h2 style="font-weight:800;font-size:2rem;letter-spacing:-0.5px;margin-bottom:0;color:var(--text-primary);">Điện Thoại Nổi Bật</h2>
            </div>
            <a href="${pageContext.request.contextPath}/products" class="btn-ghost" style="padding:12px 24px;font-size:0.95rem;">
                Xem tất cả <i class="bi bi-arrow-right ms-1"></i>
            </a>
        </div>

        <div class="row g-4">
            <c:forEach items="${featuredProducts}" var="product" varStatus="loop">
                <div class="col-lg-3 col-md-6 animate-fadeInUp" style="animation-delay:${loop.index * 0.1}s;">
                    <div class="card-modern h-100" style="border-radius: 20px;">
                        <a class="d-block position-relative" href="${pageContext.request.contextPath}/products/${product.id}" style="padding:30px;background:var(--surface-secondary);text-align:center;text-decoration:none;border-bottom:1px solid var(--border-primary);">
                            <c:choose>
                                <c:when test="${not empty product.image}">
                                    <img src="${pageContext.request.contextPath}${product.image}" alt="<c:out value='${product.name}'/>" class="img-fluid" style="height:180px;object-fit:contain;transition:transform 0.4s ease;" onmouseover="this.style.transform='scale(1.08)'" onmouseout="this.style.transform='scale(1)'">
                                </c:when>
                                <c:otherwise>
                                    <i class="bi bi-phone" style="font-size:5rem;color:var(--text-tertiary);"></i>
                                </c:otherwise>
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
                            <a class="btn-gradient w-100 d-block text-center" style="padding:12px;font-size:0.95rem; border-radius:12px;" href="${pageContext.request.contextPath}/products/${product.id}">
                                Xem chi tiết
                            </a>
                        </div>
                    </div>
                </div>
            </c:forEach>
            <c:if test="${empty featuredProducts}">
                <div class="col-12 text-center py-5" style="color:var(--text-tertiary);">
                    <i class="bi bi-inbox" style="font-size:3rem;display:block;margin-bottom:12px;"></i>
                    Chưa có sản phẩm. Quản trị viên có thể thêm sản phẩm trong mục Quản trị.
                </div>
            </c:if>
        </div>
    </div>
</section>

<!-- Include Footer Component -->
<jsp:include page="components/footer.jsp" />
