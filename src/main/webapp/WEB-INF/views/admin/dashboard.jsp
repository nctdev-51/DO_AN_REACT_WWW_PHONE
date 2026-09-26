<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- currentPage và pageTitle được truyền từ AdminDashboardController --%>

<jsp:include page="layout/admin-header.jsp"/>

    <!-- Page Heading -->
    <div class="d-flex align-items-center justify-content-between mb-4">
        <div>
            <h4 class="fw-bold mb-1">Tổng quan hệ thống</h4>
            <p class="text-muted small mb-0">Chào mừng trở lại, Admin!</p>
        </div>
    </div>

    <!-- Stat Cards (data giả - sẽ nối DB ở Task 7) -->
    <div class="row g-4 mb-4">
        <div class="col-xl-3 col-md-6">
            <div class="card border-0 shadow-sm rounded-4 h-100">
                <div class="card-body d-flex align-items-center gap-3 p-4">
                    <div class="rounded-3 p-3" style="background:#e8f0fe;">
                        <i class="bi bi-bag-check fs-3" style="color:#1a73e8;"></i>
                    </div>
                    <div>
                        <div class="text-muted small fw-semibold text-uppercase">Tổng đơn hàng</div>
                        <div class="fs-3 fw-bold">120</div>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-xl-3 col-md-6">
            <div class="card border-0 shadow-sm rounded-4 h-100">
                <div class="card-body d-flex align-items-center gap-3 p-4">
                    <div class="rounded-3 p-3" style="background:#e6f4ea;">
                        <i class="bi bi-currency-dollar fs-3" style="color:#1e8e3e;"></i>
                    </div>
                    <div>
                        <div class="text-muted small fw-semibold text-uppercase">Doanh thu</div>
                        <div class="fs-3 fw-bold">1,5 tỷ</div>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-xl-3 col-md-6">
            <div class="card border-0 shadow-sm rounded-4 h-100">
                <div class="card-body d-flex align-items-center gap-3 p-4">
                    <div class="rounded-3 p-3" style="background:#fce8e6;">
                        <i class="bi bi-people fs-3" style="color:#d93025;"></i>
                    </div>
                    <div>
                        <div class="text-muted small fw-semibold text-uppercase">Khách hàng</div>
                        <div class="fs-3 fw-bold">85</div>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-xl-3 col-md-6">
            <div class="card border-0 shadow-sm rounded-4 h-100">
                <div class="card-body d-flex align-items-center gap-3 p-4">
                    <div class="rounded-3 p-3" style="background:#fef7e0;">
                        <i class="bi bi-hourglass-split fs-3" style="color:#f9ab00;"></i>
                    </div>
                    <div>
                        <div class="text-muted small fw-semibold text-uppercase">Chờ xử lý</div>
                        <div class="fs-3 fw-bold">12</div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <p class="text-muted text-center fst-italic small">— Dashboard đang dùng dữ liệu mẫu, sẽ nối DB ở Task 7 —</p>

<jsp:include page="layout/admin-footer.jsp"/>
