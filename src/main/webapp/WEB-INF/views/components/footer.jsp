<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

    </main>
    <!-- Main Content Container End -->

    <!-- Footer Component -->
    <footer class="bg-dark text-white-50 pt-5 pb-3 mt-auto border-top border-secondary">
        <div class="container">
            <div class="row g-4 mb-4">
                <!-- Col 1: Store Intro -->
                <div class="col-lg-4 col-md-6">
                    <div class="d-flex align-items-center mb-3">
                        <i class="bi bi-phone text-primary fs-3 me-2"></i>
                        <span class="fs-4 fw-bold text-white">Phone<span class="text-primary">Store</span></span>
                    </div>
                    <p class="small text-secondary">
                        Hệ thống bán lẻ điện thoại di động chính hãng, cam kết chất lượng, bảo hành 12 tháng, đổi trả 30 ngày và giao hàng toàn quốc.
                    </p>
                    <div class="d-flex gap-3 text-secondary mt-3">
                        <a href="#" class="text-secondary hover-primary fs-5"><i class="bi bi-facebook"></i></a>
                        <a href="#" class="text-secondary hover-primary fs-5"><i class="bi bi-youtube"></i></a>
                        <a href="#" class="text-secondary hover-primary fs-5"><i class="bi bi-tiktok"></i></a>
                        <a href="#" class="text-secondary hover-primary fs-5"><i class="bi bi-instagram"></i></a>
                    </div>
                </div>

                <!-- Col 2: Quick Links -->
                <div class="col-lg-2 col-md-6 col-6">
                    <h6 class="text-white fw-semibold mb-3">Sản Phẩm</h6>
                    <ul class="list-unstyled small d-flex flex-column gap-2">
                        <li><a href="#" class="text-secondary text-decoration-none hover-link">Apple iPhone</a></li>
                        <li><a href="#" class="text-secondary text-decoration-none hover-link">Samsung Galaxy</a></li>
                        <li><a href="#" class="text-secondary text-decoration-none hover-link">Xiaomi Smartphone</a></li>
                        <li><a href="#" class="text-secondary text-decoration-none hover-link">Phụ kiện công nghệ</a></li>
                    </ul>
                </div>

                <!-- Col 3: Policy -->
                <div class="col-lg-3 col-md-6 col-6">
                    <h6 class="text-white fw-semibold mb-3">Chính Sách & Hỗ Trợ</h6>
                    <ul class="list-unstyled small d-flex flex-column gap-2">
                        <li><a href="#" class="text-secondary text-decoration-none hover-link">Chính sách bảo hành</a></li>
                        <li><a href="#" class="text-secondary text-decoration-none hover-link">Chính sách đổi trả 1-1</a></li>
                        <li><a href="#" class="text-secondary text-decoration-none hover-link">Hướng dẫn mua trả góp</a></li>
                        <li><a href="#" class="text-secondary text-decoration-none hover-link">Bảo mật thông tin KH</a></li>
                    </ul>
                </div>

                <!-- Col 4: Contact -->
                <div class="col-lg-3 col-md-6">
                    <h6 class="text-white fw-semibold mb-3">Liên Hệ Trực Tiếp</h6>
                    <ul class="list-unstyled small text-secondary d-flex flex-column gap-2">
                        <li><i class="bi bi-geo-alt me-2 text-primary"></i> 12 Nguyễn Văn Bảo, P.4, Q. Gò Vấp, TP.HCM</li>
                        <li><i class="bi bi-telephone me-2 text-primary"></i> Hotline: 1900 6868 (8:00 - 21:30)</li>
                        <li><i class="bi bi-envelope me-2 text-primary"></i> Email: support@phonestore.vn</li>
                    </ul>
                </div>
            </div>

            <hr class="border-secondary my-4">

            <!-- Copyright and Credits -->
            <div class="row align-items-center">
                <div class="col-md-6 text-center text-md-start small text-secondary">
                    &copy; 2025 <strong class="text-white">PhoneStore</strong>. Bản quyền thuộc về Đồ án Lập trình WWW - FIT IUH.
                </div>
                <div class="col-md-6 text-center text-md-end small text-secondary mt-2 mt-md-0">
                    Phát triển với Spring Boot 3 & Bootstrap 5
                </div>
            </div>
        </div>
    </footer>

    <!-- Bootstrap 5 JS Bundle CDN (Popper included) -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js" integrity="sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESaAA55NDzOxhy9GkcIdslK1eN7N6jIeHz" crossorigin="anonymous"></script>

    <style>
        .hover-link:hover {
            color: #0d6efd !important;
            transition: color 0.2s ease;
        }
        .hover-primary:hover {
            color: #0d6efd !important;
        }
    </style>
</body>
</html>
