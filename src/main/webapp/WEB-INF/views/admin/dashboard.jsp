<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- currentPage, pageTitle, totalOrders, totalRevenue, totalUsers, pendingOrders,
     recentOrders, chartLabels, chartData được truyền từ AdminDashboardController --%>

<jsp:include page="layout/admin-header.jsp"/>

    <!-- Page Heading -->
    <div class="d-flex align-items-center justify-content-between mb-4">
        <div>
            <h4 class="fw-bold mb-1">Tổng quan hệ thống</h4>
            <p class="text-muted small mb-0">Chào mừng trở lại, Admin!</p>
        </div>
    </div>

    <!-- ===== STAT CARDS ===== -->
    <div class="row g-4 mb-4">
        <div class="col-xl-3 col-md-6">
            <div class="card border-0 shadow-sm rounded-4 h-100">
                <div class="card-body d-flex align-items-center gap-3 p-4">
                    <div class="rounded-3 p-3" style="background:#e8f0fe;">
                        <i class="bi bi-bag-check fs-3" style="color:#1a73e8;"></i>
                    </div>
                    <div>
                        <div class="text-muted small fw-semibold text-uppercase">Tổng đơn hàng</div>
                        <div class="fs-3 fw-bold">${totalOrders}</div>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-xl-3 col-md-6">
            <div class="card border-0 shadow-sm rounded-4 h-100">
                <div class="card-body d-flex align-items-center gap-3 p-4">
                    <div class="rounded-3 p-3" style="background:#e6f4ea;">
                        <i class="bi bi-cash-coin fs-3" style="color:#1e8e3e;"></i>
                    </div>
                    <div>
                        <div class="text-muted small fw-semibold text-uppercase">Doanh thu (VNĐ)</div>
                        <div class="fs-5 fw-bold">${totalRevenue}</div>
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
                        <div class="fs-3 fw-bold">${totalUsers}</div>
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
                        <div class="fs-3 fw-bold">${pendingOrders}</div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- ===== BIỂU ĐỒ + BẢNG ĐƠN HÀNG ===== -->
    <div class="row g-4 mb-4">

        <!-- Biểu đồ doanh thu theo tháng (Chart.js) -->
        <div class="col-xl-7">
            <div class="card border-0 shadow-sm rounded-4 h-100">
                <div class="card-header bg-white border-0 pt-4 px-4 pb-0">
                    <h6 class="fw-bold mb-0">
                        <i class="bi bi-bar-chart-line text-primary me-2"></i>Doanh thu theo tháng
                    </h6>
                    <p class="text-muted small mb-0">6 tháng gần nhất</p>
                </div>
                <div class="card-body p-4">
                    <canvas id="revenueChart" height="110"></canvas>
                </div>
            </div>
        </div>

        <!-- Bảng đơn hàng gần đây -->
        <div class="col-xl-5">
            <div class="card border-0 shadow-sm rounded-4 h-100">
                <div class="card-header bg-white border-0 pt-4 px-4 pb-0 d-flex justify-content-between align-items-center">
                    <div>
                        <h6 class="fw-bold mb-0">
                            <i class="bi bi-clock-history text-warning me-2"></i>Đơn hàng gần đây
                        </h6>
                        <p class="text-muted small mb-0">5 đơn mới nhất</p>
                    </div>
                    <a href="${pageContext.request.contextPath}/admin/orders"
                       class="btn btn-sm btn-outline-primary rounded-pill px-3">
                        Xem tất cả
                    </a>
                </div>
                <div class="card-body p-0">
                    <div class="table-responsive">
                        <table class="table table-hover align-middle mb-0">
                            <thead class="table-light">
                                <tr>
                                    <th class="px-4 py-3 small fw-semibold text-muted">#</th>
                                    <th class="py-3 small fw-semibold text-muted">Khách hàng</th>
                                    <th class="py-3 small fw-semibold text-muted">Tổng tiền</th>
                                    <th class="py-3 small fw-semibold text-muted">Trạng thái</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="order" items="${recentOrders}">
                                    <tr>
                                        <td class="px-4 text-muted small">#${order[0]}</td>
                                        <td>
                                            <div class="d-flex align-items-center gap-2">
                                                <div class="rounded-circle bg-primary bg-opacity-10 d-flex align-items-center justify-content-center"
                                                     style="width:32px;height:32px;min-width:32px;">
                                                    <i class="bi bi-person text-primary small"></i>
                                                </div>
                                                <div>
                                                    <div class="small fw-semibold">${order[1]}</div>
                                                    <div class="text-muted" style="font-size:0.75rem;">${order[2]}</div>
                                                </div>
                                            </div>
                                        </td>
                                        <td class="small fw-semibold">${order[3]}</td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${order[4] == 'Chờ xử lý'}">
                                                    <span class="badge rounded-pill bg-warning text-dark">${order[4]}</span>
                                                </c:when>
                                                <c:when test="${order[4] == 'Đang giao'}">
                                                    <span class="badge rounded-pill bg-primary">${order[4]}</span>
                                                </c:when>
                                                <c:when test="${order[4] == 'Đã giao'}">
                                                    <span class="badge rounded-pill bg-success">${order[4]}</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge rounded-pill bg-danger">${order[4]}</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- ===== CHART.JS SCRIPT ===== -->
    <%-- Dùng hidden span để tránh JSP escape dấu nháy kép trong EL --%>
    <span id="chartLabelsData" style="display:none;">${chartLabels}</span>
    <span id="chartDataValues" style="display:none;">${chartData}</span>

    <%-- Chart.js đã được load trong admin-footer.jsp --%>
    <script>
        document.addEventListener('DOMContentLoaded', function () {
            const ctx = document.getElementById('revenueChart').getContext('2d');

            // Đọc data từ hidden span thay vì inject thẳng vào JS (tránh lỗi JSP escape)
            const labels = document.getElementById('chartLabelsData').textContent.split(',').map(s => s.trim().replace(/"/g, ''));
            const data   = document.getElementById('chartDataValues').textContent.split(',').map(s => Number(s.trim()));

            new Chart(ctx, {
                type: 'bar',
                data: {
                    labels: labels,
                    datasets: [{
                        label: 'Doanh thu (VNĐ)',
                        data: data,
                        backgroundColor: 'rgba(13, 110, 253, 0.15)',
                        borderColor: 'rgba(13, 110, 253, 0.9)',
                        borderWidth: 2,
                        borderRadius: 6,
                        borderSkipped: false
                    }]
                },
                options: {
                    responsive: true,
                    plugins: {
                        legend: { display: false },
                        tooltip: {
                            callbacks: {
                                label: function(context) {
                                    // Format số tiền theo kiểu VNĐ
                                    return ' ' + context.parsed.y.toLocaleString('vi-VN') + ' đ';
                                }
                            }
                        }
                    },
                    scales: {
                        y: {
                            beginAtZero: true,
                            ticks: {
                                callback: function(value) {
                                    if (value >= 1000000) return (value / 1000000) + 'tr';
                                    return value;
                                }
                            },
                            grid: { color: 'rgba(0,0,0,0.04)' }
                        },
                        x: {
                            grid: { display: false }
                        }
                    }
                }
            });
        });
    </script>

<jsp:include page="layout/admin-footer.jsp"/>
