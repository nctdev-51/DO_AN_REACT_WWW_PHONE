<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!-- Include Header Component -->
<jsp:include page="components/header.jsp" />

<!-- Hero Section -->
<section class="py-5 bg-white border-bottom shadow-sm">
    <div class="container">
        <div class="row align-items-center g-4">
            <div class="col-lg-7">
                <span class="badge bg-primary-subtle text-primary fw-semibold px-3 py-2 rounded-pill mb-3">
                    <i class="bi bi-stars me-1"></i> Ưu đãi mùa tựu trường & Ra mắt siêu phẩm mới
                </span>
                <!-- Hiển thị chuỗi truyền từ Controller qua Expression Language (EL) -->
                <h1 class="display-4 fw-bold text-dark lh-sm mb-3">
                    ${chuoi}
                </h1>
                <p class="lead text-muted mb-4">
                    Khám phá các mẫu Smartphone cao cấp nhất từ iPhone 16 Pro Max, Samsung Galaxy S25 Ultra đến Xiaomi 14 series với giá ưu đãi đặc quyền và bảo hành toàn diện.
                </p>
                <div class="d-flex flex-wrap gap-3">
                    <a href="${pageContext.request.contextPath}/products" class="btn btn-primary btn-lg px-4 rounded-pill shadow-sm">
                        <i class="bi bi-bag-check me-2"></i> Mua ngay
                    </a>
                    <a href="#featured" class="btn btn-outline-secondary btn-lg px-4 rounded-pill">
                        <i class="bi bi-fire me-2"></i> Sản phẩm bán chạy
                    </a>
                </div>
            </div>
            <div class="col-lg-5 text-center">
                <div class="position-relative d-inline-block">
                    <div class="p-4 bg-light rounded-4 border shadow-sm text-center">
                        <i class="bi bi-phone-vibrate text-primary" style="font-size: 8rem;"></i>
                        <h4 class="fw-bold mt-2">Flagship Phone 2025</h4>
                        <p class="text-muted small mb-0">Thiết kế titan, camera 200MP, AI thông minh</p>
                    </div>
                </div>
            </div>
        </div>
    </div>
</section>

<!-- Trust & Feature Badges -->
<section class="py-4 bg-light border-bottom">
    <div class="container">
        <div class="row g-3 text-center">
            <div class="col-md-3 col-6">
                <div class="p-3 bg-white rounded-3 border shadow-sm h-100">
                    <i class="bi bi-shield-check text-primary fs-2 mb-2"></i>
                    <h6 class="fw-bold mb-1">Chính Hãng 100%</h6>
                    <small class="text-muted">Bảo hành 12 tháng Apple & Samsung</small>
                </div>
            </div>
            <div class="col-md-3 col-6">
                <div class="p-3 bg-white rounded-3 border shadow-sm h-100">
                    <i class="bi bi-truck text-success fs-2 mb-2"></i>
                    <h6 class="fw-bold mb-1">Giao Nhanh 2 Giờ</h6>
                    <small class="text-muted">Miễn phí vận chuyển đơn từ 2 triệu</small>
                </div>
            </div>
            <div class="col-md-3 col-6">
                <div class="p-3 bg-white rounded-3 border shadow-sm h-100">
                    <i class="bi bi-arrow-repeat text-warning fs-2 mb-2"></i>
                    <h6 class="fw-bold mb-1">1 Đổi 1 Trong 30 Ngày</h6>
                    <small class="text-muted">Nếu phát sinh lỗi từ nhà sản xuất</small>
                </div>
            </div>
            <div class="col-md-3 col-6">
                <div class="p-3 bg-white rounded-3 border shadow-sm h-100">
                    <i class="bi bi-credit-card text-danger fs-2 mb-2"></i>
                    <h6 class="fw-bold mb-1">Trả Góp 0% Lãi Suất</h6>
                    <small class="text-muted">Thủ tục đơn giản qua thẻ tín dụng</small>
                </div>
            </div>
        </div>
    </div>
</section>

<!-- Featured Products Showcase -->
<section class="py-5" id="featured">
    <div class="container">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <div>
                <span class="text-primary fw-semibold small text-uppercase">Danh mục nổi bật</span>
                <h2 class="fw-bold mb-0">Điện Thoại Nổi Bật</h2>
            </div>
            <a href="${pageContext.request.contextPath}/products" class="btn btn-outline-primary rounded-pill px-3">
                Xem tất cả <i class="bi bi-arrow-right ms-1"></i>
            </a>
        </div>

        <div class="row g-4">
            <!-- Sample Card 1 -->
            <div class="col-lg-3 col-md-6">
                <div class="card h-100 shadow-sm border-0 rounded-4 product-card overflow-hidden">
                    <div class="position-relative bg-light text-center p-4">
                        <span class="badge bg-danger position-absolute top-0 start-0 m-3 rounded-pill">-15%</span>
                        <i class="bi bi-phone text-muted" style="font-size: 5rem;"></i>
                    </div>
                    <div class="card-body d-flex flex-column p-4">
                        <small class="text-muted text-uppercase fw-semibold">Apple</small>
                        <h5 class="card-title fw-bold mt-1 mb-2">iPhone 16 Pro Max 256GB</h5>
                        <div class="mb-3">
                            <span class="text-danger fw-bold fs-5">32.990.000₫</span>
                            <span class="text-muted text-decoration-line-through small ms-2">34.990.000₫</span>
                        </div>
                        <div class="mt-auto">
                            <button class="btn btn-primary w-100 rounded-pill">
                                <i class="bi bi-cart-plus me-1"></i> Thêm vào giỏ
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Sample Card 2 -->
            <div class="col-lg-3 col-md-6">
                <div class="card h-100 shadow-sm border-0 rounded-4 product-card overflow-hidden">
                    <div class="position-relative bg-light text-center p-4">
                        <span class="badge bg-primary position-absolute top-0 start-0 m-3 rounded-pill">Mới</span>
                        <i class="bi bi-phone text-muted" style="font-size: 5rem;"></i>
                    </div>
                    <div class="card-body d-flex flex-column p-4">
                        <small class="text-muted text-uppercase fw-semibold">Samsung</small>
                        <h5 class="card-title fw-bold mt-1 mb-2">Samsung Galaxy S25 Ultra</h5>
                        <div class="mb-3">
                            <span class="text-danger fw-bold fs-5">31.490.000₫</span>
                            <span class="text-muted text-decoration-line-through small ms-2">33.990.000₫</span>
                        </div>
                        <div class="mt-auto">
                            <button class="btn btn-primary w-100 rounded-pill">
                                <i class="bi bi-cart-plus me-1"></i> Thêm vào giỏ
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Sample Card 3 -->
            <div class="col-lg-3 col-md-6">
                <div class="card h-100 shadow-sm border-0 rounded-4 product-card overflow-hidden">
                    <div class="position-relative bg-light text-center p-4">
                        <span class="badge bg-warning text-dark position-absolute top-0 start-0 m-3 rounded-pill">Hot</span>
                        <i class="bi bi-phone text-muted" style="font-size: 5rem;"></i>
                    </div>
                    <div class="card-body d-flex flex-column p-4">
                        <small class="text-muted text-uppercase fw-semibold">Xiaomi</small>
                        <h5 class="card-title fw-bold mt-1 mb-2">Xiaomi 14 Ultra 512GB</h5>
                        <div class="mb-3">
                            <span class="text-danger fw-bold fs-5">24.990.000₫</span>
                            <span class="text-muted text-decoration-line-through small ms-2">26.990.000₫</span>
                        </div>
                        <div class="mt-auto">
                            <button class="btn btn-primary w-100 rounded-pill">
                                <i class="bi bi-cart-plus me-1"></i> Thêm vào giỏ
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Sample Card 4 -->
            <div class="col-lg-3 col-md-6">
                <div class="card h-100 shadow-sm border-0 rounded-4 product-card overflow-hidden">
                    <div class="position-relative bg-light text-center p-4">
                        <span class="badge bg-success position-absolute top-0 start-0 m-3 rounded-pill">Bán chạy</span>
                        <i class="bi bi-phone text-muted" style="font-size: 5rem;"></i>
                    </div>
                    <div class="card-body d-flex flex-column p-4">
                        <small class="text-muted text-uppercase fw-semibold">Apple</small>
                        <h5 class="card-title fw-bold mt-1 mb-2">iPhone 15 128GB Chính Hãng</h5>
                        <div class="mb-3">
                            <span class="text-danger fw-bold fs-5">18.490.000₫</span>
                            <span class="text-muted text-decoration-line-through small ms-2">20.490.000₫</span>
                        </div>
                        <div class="mt-auto">
                            <button class="btn btn-primary w-100 rounded-pill">
                                <i class="bi bi-cart-plus me-1"></i> Thêm vào giỏ
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</section>

<style>
    .product-card {
        transition: transform 0.25s ease, box-shadow 0.25s ease;
    }
    .product-card:hover {
        transform: translateY(-5px);
        box-shadow: 0 12px 24px rgba(0, 0, 0, 0.1) !important;
    }
</style>

<!-- Include Footer Component -->
<jsp:include page="components/footer.jsp" />
