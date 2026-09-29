<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%-- currentPage, pageTitle, orders, statuses được truyền từ AdminOrderController --%>

<jsp:include page="layout/admin-header.jsp"/>

    <!-- Page Heading -->
    <div class="d-flex align-items-center justify-content-between mb-4">
        <div>
            <h4 style="font-weight:800;font-size:1.4rem;margin-bottom:4px;letter-spacing:-0.3px;">Quản lý đơn hàng</h4>
            <p style="color:#9ca3af;font-size:0.88rem;margin-bottom:0;">Danh sách tất cả đơn hàng trong hệ thống</p>
        </div>
    </div>

    <!-- Flash message sau khi cập nhật trạng thái -->
    <c:if test="${not empty successMsg}">
        <div style="background:rgba(16,185,129,0.08);border:1px solid rgba(16,185,129,0.15);border-radius:12px;padding:14px 18px;margin-bottom:20px;color:#10b981;font-size:0.88rem;display:flex;align-items:center;gap:8px;" role="alert">
            <i class="bi bi-check-circle"></i>${successMsg}
            <button type="button" class="btn-close" data-bs-dismiss="alert" style="margin-left:auto;"></button>
        </div>
    </c:if>

    <!-- Bảng đơn hàng -->
    <div class="admin-card">
        <div style="padding:20px 24px 16px;display:flex;justify-content:space-between;align-items:center;">
            <div class="d-flex align-items-center gap-2">
                <div style="width:28px;height:28px;border-radius:8px;background:linear-gradient(135deg,rgba(102,126,234,0.1),rgba(118,75,162,0.1));display:flex;align-items:center;justify-content:center;">
                    <i class="bi bi-bag-check" style="color:#667eea;font-size:0.85rem;"></i>
                </div>
                <h6 style="font-weight:700;font-size:0.95rem;margin:0;">Tất cả đơn hàng</h6>
            </div>
            <span style="background:linear-gradient(135deg,rgba(102,126,234,0.1),rgba(118,75,162,0.1));color:#667eea;padding:4px 14px;border-radius:999px;font-size:0.8rem;font-weight:600;">${orders.size()} đơn</span>
        </div>
        <div class="card-body p-0">
            <div class="table-responsive">
                <table class="table admin-table align-middle mb-0">
                    <thead>
                        <tr>
                            <th class="px-4">Mã đơn</th>
                            <th>Khách hàng</th>
                            <th>Ngày đặt</th>
                            <th>Tổng tiền</th>
                            <th>Trạng thái</th>
                            <th class="text-center">Hành động</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="order" items="${orders}">
                            <tr>
                                <td class="px-4">
                                    <span style="font-weight:700;color:#667eea;">#${order[0]}</span>
                                </td>
                                <td>
                                    <div class="d-flex align-items-center gap-2">
                                        <div style="width:34px;height:34px;border-radius:10px;background:linear-gradient(135deg,rgba(102,126,234,0.1),rgba(118,75,162,0.1));display:flex;align-items:center;justify-content:center;flex-shrink:0;">
                                            <i class="bi bi-person" style="color:#667eea;font-size:0.85rem;"></i>
                                        </div>
                                        <div>
                                            <div style="font-weight:600;font-size:0.85rem;">${order[1]}</div>
                                            <div style="color:#9ca3af;font-size:0.72rem;">${order[2]}</div>
                                        </div>
                                    </div>
                                </td>
                                <td style="color:#9ca3af;font-size:0.85rem;">${order[4]}</td>
                                <td>
                                    <span style="font-weight:600;font-size:0.85rem;">
                                        <fmt:formatNumber value="${order[6]}" type="number" groupingUsed="true"/>₫
                                    </span>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${order[7] == 'Chờ xử lý'}">
                                            <span style="background:rgba(245,158,11,0.1);color:#f59e0b;padding:5px 14px;border-radius:999px;font-size:0.78rem;font-weight:600;display:inline-flex;align-items:center;gap:4px;">
                                                <i class="bi bi-hourglass-split"></i>${order[7]}
                                            </span>
                                        </c:when>
                                        <c:when test="${order[7] == 'Đang giao'}">
                                            <span style="background:rgba(102,126,234,0.1);color:#667eea;padding:5px 14px;border-radius:999px;font-size:0.78rem;font-weight:600;display:inline-flex;align-items:center;gap:4px;">
                                                <i class="bi bi-truck"></i>${order[7]}
                                            </span>
                                        </c:when>
                                        <c:when test="${order[7] == 'Đã giao'}">
                                            <span style="background:rgba(16,185,129,0.1);color:#10b981;padding:5px 14px;border-radius:999px;font-size:0.78rem;font-weight:600;display:inline-flex;align-items:center;gap:4px;">
                                                <i class="bi bi-check-circle"></i>${order[7]}
                                            </span>
                                        </c:when>
                                        <c:otherwise>
                                            <span style="background:rgba(239,68,68,0.1);color:#ef4444;padding:5px 14px;border-radius:999px;font-size:0.78rem;font-weight:600;display:inline-flex;align-items:center;gap:4px;">
                                                <i class="bi bi-x-circle"></i>${order[7]}
                                            </span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td class="text-center">
                                    <div class="d-flex justify-content-center gap-2">
                                        <a href="${pageContext.request.contextPath}/admin/orders/${order[0]}" class="admin-btn-gradient" style="padding:6px 14px;font-size:0.8rem;">
                                            <i class="bi bi-eye"></i>Chi tiết
                                        </a>
                                        <form action="${pageContext.request.contextPath}/admin/orders/${order[0]}/status"
                                              method="post" class="d-flex gap-1 align-items-center">
                                            <select name="status" style="border-radius:8px;border:1.5px solid #e5e7eb;padding:5px 10px;font-size:0.78rem;width:120px;outline:none;">
                                                <c:forEach var="st" items="${statuses}">
                                                    <option value="${st}" ${order[7] == st ? 'selected' : ''}>${st}</option>
                                                </c:forEach>
                                            </select>
                                            <button type="submit" style="background:rgba(16,185,129,0.1);color:#10b981;border:none;border-radius:8px;padding:5px 10px;cursor:pointer;font-size:0.85rem;transition:all 0.2s ease;" title="Lưu trạng thái" onmouseover="this.style.background='rgba(16,185,129,0.2)'" onmouseout="this.style.background='rgba(16,185,129,0.1)'">
                                                <i class="bi bi-check-lg"></i>
                                            </button>
                                        </form>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

<jsp:include page="layout/admin-footer.jsp"/>
