<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- currentPage, pageTitle, totalOrders, totalRevenue, totalUsers, pendingOrders,
     recentOrders, chartLabels, chartData được truyền từ AdminDashboardController --%>

<jsp:include page="layout/admin-header.jsp"/>

    <!-- Page Heading -->
    <div class="d-flex align-items-center justify-content-between mb-4">
        <div>
            <h4 style="font-weight:800;font-size:1.4rem;margin-bottom:4px;letter-spacing:-0.3px;">Tổng quan hệ thống</h4>
            <p style="color:#9ca3af;font-size:0.88rem;margin-bottom:0;">Chào mừng trở lại, Admin!</p>
        </div>
    </div>

    <!-- ===== STAT CARDS ===== -->
    <div class="row g-4 mb-4">
        <div class="col-xl-3 col-md-6">
            <div class="admin-card h-100">
                <div class="card-body d-flex align-items-center gap-3 p-4">
                    <div class="stat-icon" style="background:linear-gradient(135deg,rgba(102,126,234,0.1),rgba(118,75,162,0.1));">
                        <i class="bi bi-bag-check fs-4" style="color:#667eea;"></i>
                    </div>
                    <div>
                        <div style="font-size:0.72rem;font-weight:700;text-transform:uppercase;letter-spacing:0.5px;color:#9ca3af;">Tổng đơn hàng</div>
                        <div style="font-size:1.8rem;font-weight:800;color:#1a1a2e;letter-spacing:-0.5px;line-height:1.2;">${totalOrders}</div>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-xl-3 col-md-6">
            <div class="admin-card h-100">
                <div class="card-body d-flex align-items-center gap-3 p-4">
                    <div class="stat-icon" style="background:linear-gradient(135deg,rgba(16,185,129,0.1),rgba(5,150,105,0.1));">
                        <i class="bi bi-cash-coin fs-4" style="color:#10b981;"></i>
                    </div>
                    <div>
                        <div style="font-size:0.72rem;font-weight:700;text-transform:uppercase;letter-spacing:0.5px;color:#9ca3af;">Doanh thu (VNĐ)</div>
                        <div style="font-size:1.2rem;font-weight:800;color:#1a1a2e;letter-spacing:-0.5px;line-height:1.4;">${totalRevenue}</div>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-xl-3 col-md-6">
            <div class="admin-card h-100">
                <div class="card-body d-flex align-items-center gap-3 p-4">
                    <div class="stat-icon" style="background:linear-gradient(135deg,rgba(239,68,68,0.1),rgba(220,38,38,0.1));">
                        <i class="bi bi-people fs-4" style="color:#ef4444;"></i>
                    </div>
                    <div>
                        <div style="font-size:0.72rem;font-weight:700;text-transform:uppercase;letter-spacing:0.5px;color:#9ca3af;">Khách hàng</div>
                        <div style="font-size:1.8rem;font-weight:800;color:#1a1a2e;letter-spacing:-0.5px;line-height:1.2;">${totalUsers}</div>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-xl-3 col-md-6">
            <div class="admin-card h-100">
                <div class="card-body d-flex align-items-center gap-3 p-4">
                    <div class="stat-icon" style="background:linear-gradient(135deg,rgba(245,158,11,0.1),rgba(234,88,12,0.1));">
                        <i class="bi bi-hourglass-split fs-4" style="color:#f59e0b;"></i>
                    </div>
                    <div>
                        <div style="font-size:0.72rem;font-weight:700;text-transform:uppercase;letter-spacing:0.5px;color:#9ca3af;">Chờ xử lý</div>
                        <div style="font-size:1.8rem;font-weight:800;color:#1a1a2e;letter-spacing:-0.5px;line-height:1.2;">${pendingOrders}</div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- ===== BIỂU ĐỒ + BẢNG ĐƠN HÀNG ===== -->
    <div class="row g-4 mb-4">

        <!-- Biểu đồ doanh thu theo tháng (Chart.js) -->
        <div class="col-xl-7">
            <div class="admin-card h-100">
                <div style="padding:20px 24px 0;">
                    <div class="d-flex align-items-center gap-2 mb-1">
                        <div style="width:28px;height:28px;border-radius:8px;background:linear-gradient(135deg,rgba(102,126,234,0.1),rgba(118,75,162,0.1));display:flex;align-items:center;justify-content:center;">
                            <i class="bi bi-bar-chart-line" style="color:#667eea;font-size:0.85rem;"></i>
                        </div>
                        <h6 style="font-weight:700;font-size:0.95rem;margin:0;">Doanh thu theo tháng</h6>
                    </div>
                    <p style="color:#9ca3af;font-size:0.82rem;margin:0;">6 tháng gần nhất</p>
                </div>
                <div class="card-body p-4">
                    <canvas id="revenueChart" height="110"></canvas>
                </div>
            </div>
        </div>

        <!-- Bảng đơn hàng gần đây -->
        <div class="col-xl-5">
            <div class="admin-card h-100">
                <div style="padding:20px 24px 0;display:flex;justify-content:space-between;align-items:start;">
                    <div>
                        <div class="d-flex align-items-center gap-2 mb-1">
                            <div style="width:28px;height:28px;border-radius:8px;background:linear-gradient(135deg,rgba(245,158,11,0.1),rgba(234,88,12,0.1));display:flex;align-items:center;justify-content:center;">
                                <i class="bi bi-clock-history" style="color:#f59e0b;font-size:0.85rem;"></i>
                            </div>
                            <h6 style="font-weight:700;font-size:0.95rem;margin:0;">Đơn hàng gần đây</h6>
                        </div>
                        <p style="color:#9ca3af;font-size:0.82rem;margin:0;">5 đơn mới nhất</p>
                    </div>
                    <a href="${pageContext.request.contextPath}/admin/orders" class="admin-btn-gradient" style="padding:6px 14px;font-size:0.8rem;">
                        Xem tất cả
                    </a>
                </div>
                <div class="card-body p-0" style="margin-top:16px;">
                    <div class="table-responsive">
                        <table class="table admin-table align-middle mb-0">
                            <thead>
                                <tr>
                                    <th class="px-4">#</th>
                                    <th>Khách hàng</th>
                                    <th>Tổng tiền</th>
                                    <th>Trạng thái</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="order" items="${recentOrders}">
                                    <tr>
                                        <td class="px-4" style="color:#9ca3af;">#${order[0]}</td>
                                        <td>
                                            <div class="d-flex align-items-center gap-2">
                                                <div style="width:32px;height:32px;border-radius:10px;background:linear-gradient(135deg,rgba(102,126,234,0.1),rgba(118,75,162,0.1));display:flex;align-items:center;justify-content:center;flex-shrink:0;">
                                                    <i class="bi bi-person" style="color:#667eea;font-size:0.85rem;"></i>
                                                </div>
                                                <div>
                                                    <div style="font-weight:600;font-size:0.85rem;">${order[1]}</div>
                                                    <div style="color:#9ca3af;font-size:0.72rem;">${order[2]}</div>
                                                </div>
                                            </div>
                                        </td>
                                        <td style="font-weight:600;font-size:0.85rem;">${order[3]}</td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${order[4] == 'Chờ xử lý'}">
                                                    <span style="background:rgba(245,158,11,0.1);color:#f59e0b;padding:4px 12px;border-radius:999px;font-size:0.78rem;font-weight:600;">${order[4]}</span>
                                                </c:when>
                                                <c:when test="${order[4] == 'Đang giao'}">
                                                    <span style="background:rgba(102,126,234,0.1);color:#667eea;padding:4px 12px;border-radius:999px;font-size:0.78rem;font-weight:600;">${order[4]}</span>
                                                </c:when>
                                                <c:when test="${order[4] == 'Đã giao'}">
                                                    <span style="background:rgba(16,185,129,0.1);color:#10b981;padding:4px 12px;border-radius:999px;font-size:0.78rem;font-weight:600;">${order[4]}</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span style="background:rgba(239,68,68,0.1);color:#ef4444;padding:4px 12px;border-radius:999px;font-size:0.78rem;font-weight:600;">${order[4]}</span>
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

            // Create gradient for chart
            const gradient = ctx.createLinearGradient(0, 0, 0, 300);
            gradient.addColorStop(0, 'rgba(102, 126, 234, 0.3)');
            gradient.addColorStop(1, 'rgba(102, 126, 234, 0.02)');

            new Chart(ctx, {
                type: 'bar',
                data: {
                    labels: labels,
                    datasets: [{
                        label: 'Doanh thu (VNĐ)',
                        data: data,
                        backgroundColor: gradient,
                        borderColor: '#667eea',
                        borderWidth: 2,
                        borderRadius: 8,
                        borderSkipped: false
                    }]
                },
                options: {
                    responsive: true,
                    plugins: {
                        legend: { display: false },
                        tooltip: {
                            backgroundColor: '#1a1a2e',
                            titleColor: '#fff',
                            bodyColor: '#fff',
                            cornerRadius: 10,
                            padding: 12,
                            titleFont: { weight: '600', size: 13 },
                            bodyFont: { size: 12 },
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
                                font: { size: 11, weight: '500' },
                                color: '#9ca3af',
                                callback: function(value) {
                                    if (value >= 1000000) return (value / 1000000) + 'tr';
                                    return value;
                                }
                            },
                            grid: { color: 'rgba(0,0,0,0.03)', drawBorder: false }
                        },
                        x: {
                            ticks: { font: { size: 11, weight: '500' }, color: '#9ca3af' },
                            grid: { display: false }
                        }
                    }
                }
            });
        });
    </script>

<jsp:include page="layout/admin-footer.jsp"/>
