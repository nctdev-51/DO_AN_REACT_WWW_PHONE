<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%-- currentPage, pageTitle, order, orderItems, statuses được truyền từ AdminOrderController --%>

<jsp:include page="layout/admin-header.jsp"/>

    <!-- Page Heading -->
    <div class="d-flex align-items-center justify-content-between mb-4">
        <div>
            <a href="${pageContext.request.contextPath}/admin/orders"
               style="display:inline-flex;align-items:center;gap:6px;color:#667eea;text-decoration:none;font-weight:600;font-size:0.85rem;padding:6px 14px;border-radius:999px;background:rgba(102,126,234,0.08);transition:all 0.2s ease;margin-bottom:12px;"
               onmouseover="this.style.background='rgba(102,126,234,0.15)'" onmouseout="this.style.background='rgba(102,126,234,0.08)'">
                <i class="bi bi-arrow-left"></i>Quay lại danh sách
            </a>
            <h4 style="font-weight:800;font-size:1.4rem;margin-bottom:4px;letter-spacing:-0.3px;">Chi tiết đơn hàng <span style="color:#667eea;">#${order[0]}</span></h4>
            <p style="color:#9ca3af;font-size:0.88rem;margin-bottom:0;">Thông tin chi tiết và trạng thái đơn hàng</p>
        </div>
    </div>

    <div class="row g-4">

        <!-- ===== CỘT TRÁI: Thông tin đơn + Sản phẩm ===== -->
        <div class="col-xl-8">

            <!-- Thông tin khách hàng & giao hàng -->
            <div class="admin-card mb-4">
                <div style="padding:20px 24px 0;">
                    <div class="d-flex align-items-center gap-2">
                        <div style="width:28px;height:28px;border-radius:8px;background:linear-gradient(135deg,rgba(102,126,234,0.1),rgba(118,75,162,0.1));display:flex;align-items:center;justify-content:center;">
                            <i class="bi bi-person-lines-fill" style="color:#667eea;font-size:0.85rem;"></i>
                        </div>
                        <h6 style="font-weight:700;font-size:0.95rem;margin:0;">Thông tin khách hàng</h6>
                    </div>
                </div>
                <div class="card-body p-4">
                    <div class="row g-3">
                        <div class="col-md-6">
                            <div style="display:flex;align-items:center;gap:12px;padding:14px 16px;background:#f8f9fc;border-radius:12px;">
                                <div style="width:40px;height:40px;border-radius:10px;background:linear-gradient(135deg,rgba(102,126,234,0.1),rgba(118,75,162,0.1));display:flex;align-items:center;justify-content:center;flex-shrink:0;">
                                    <i class="bi bi-person" style="color:#667eea;"></i>
                                </div>
                                <div>
                                    <div style="font-size:0.78rem;color:#9ca3af;">Họ tên</div>
                                    <div style="font-weight:600;font-size:0.9rem;">${order[1]}</div>
                                </div>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div style="display:flex;align-items:center;gap:12px;padding:14px 16px;background:#f8f9fc;border-radius:12px;">
                                <div style="width:40px;height:40px;border-radius:10px;background:linear-gradient(135deg,rgba(16,185,129,0.1),rgba(5,150,105,0.1));display:flex;align-items:center;justify-content:center;flex-shrink:0;">
                                    <i class="bi bi-envelope" style="color:#10b981;"></i>
                                </div>
                                <div>
                                    <div style="font-size:0.78rem;color:#9ca3af;">Email</div>
                                    <div style="font-weight:600;font-size:0.9rem;">${order[2]}</div>
                                </div>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div style="display:flex;align-items:center;gap:12px;padding:14px 16px;background:#f8f9fc;border-radius:12px;">
                                <div style="width:40px;height:40px;border-radius:10px;background:linear-gradient(135deg,rgba(245,158,11,0.1),rgba(234,88,12,0.1));display:flex;align-items:center;justify-content:center;flex-shrink:0;">
                                    <i class="bi bi-telephone" style="color:#f59e0b;"></i>
                                </div>
                                <div>
                                    <div style="font-size:0.78rem;color:#9ca3af;">Số điện thoại</div>
                                    <div style="font-weight:600;font-size:0.9rem;">${order[3]}</div>
                                </div>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div style="display:flex;align-items:center;gap:12px;padding:14px 16px;background:#f8f9fc;border-radius:12px;">
                                <div style="width:40px;height:40px;border-radius:10px;background:linear-gradient(135deg,rgba(239,68,68,0.1),rgba(220,38,38,0.1));display:flex;align-items:center;justify-content:center;flex-shrink:0;">
                                    <i class="bi bi-geo-alt" style="color:#ef4444;"></i>
                                </div>
                                <div>
                                    <div style="font-size:0.78rem;color:#9ca3af;">Địa chỉ giao hàng</div>
                                    <div style="font-weight:600;font-size:0.9rem;">${order[5]}</div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Bảng sản phẩm trong đơn -->
            <div class="admin-card">
                <div style="padding:20px 24px 0;">
                    <div class="d-flex align-items-center gap-2">
                        <div style="width:28px;height:28px;border-radius:8px;background:linear-gradient(135deg,rgba(102,126,234,0.1),rgba(118,75,162,0.1));display:flex;align-items:center;justify-content:center;">
                            <i class="bi bi-phone" style="color:#667eea;font-size:0.85rem;"></i>
                        </div>
                        <h6 style="font-weight:700;font-size:0.95rem;margin:0;">Sản phẩm trong đơn</h6>
                    </div>
                </div>
                <div class="card-body p-0" style="margin-top:16px;">
                    <div class="table-responsive">
                        <table class="table admin-table align-middle mb-0">
                            <thead>
                                <tr>
                                    <th class="px-4">Tên sản phẩm</th>
                                    <th class="text-center">Số lượng</th>
                                    <th class="text-end">Đơn giá</th>
                                    <th class="text-end px-4">Thành tiền</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="item" items="${orderItems}">
                                    <tr>
                                        <td class="px-4">
                                            <div class="d-flex align-items-center gap-2">
                                                <div style="width:36px;height:36px;border-radius:8px;background:#f8f9fc;display:flex;align-items:center;justify-content:center;flex-shrink:0;">
                                                    <i class="bi bi-phone" style="color:#9ca3af;font-size:0.85rem;"></i>
                                                </div>
                                                <span style="font-weight:600;font-size:0.88rem;">${item[0]}</span>
                                            </div>
                                        </td>
                                        <td class="text-center" style="font-size:0.88rem;">${item[1]}</td>
                                        <td class="text-end" style="font-size:0.88rem;">
                                            <fmt:formatNumber value="${item[2]}" type="number" groupingUsed="true"/>₫
                                        </td>
                                        <td class="text-end px-4">
                                            <span style="font-weight:700;color:#ef4444;font-size:0.88rem;">
                                                <fmt:formatNumber value="${item[3]}" type="number" groupingUsed="true"/>₫
                                            </span>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                            <tfoot>
                                <tr style="background:#f8f9fc;">
                                    <td colspan="3" class="px-4 text-end" style="font-weight:700;padding:16px;">Tổng thanh toán:</td>
                                    <td class="px-4 text-end" style="padding:16px;">
                                        <span style="font-size:1.2rem;font-weight:800;background:linear-gradient(135deg,#f5576c,#ff6b6b);-webkit-background-clip:text;-webkit-text-fill-color:transparent;background-clip:text;">
                                            <fmt:formatNumber value="${order[6]}" type="number" groupingUsed="true"/>₫
                                        </span>
                                    </td>
                                </tr>
                            </tfoot>
                        </table>
                    </div>
                </div>
            </div>
        </div>

        <!-- ===== CỘT PHẢI: Trạng thái + Cập nhật ===== -->
        <div class="col-xl-4">

            <!-- Thông tin đơn hàng -->
            <div class="admin-card mb-4">
                <div style="padding:20px 24px 0;">
                    <div class="d-flex align-items-center gap-2">
                        <div style="width:28px;height:28px;border-radius:8px;background:linear-gradient(135deg,rgba(102,126,234,0.1),rgba(118,75,162,0.1));display:flex;align-items:center;justify-content:center;">
                            <i class="bi bi-info-circle" style="color:#667eea;font-size:0.85rem;"></i>
                        </div>
                        <h6 style="font-weight:700;font-size:0.95rem;margin:0;">Thông tin đơn</h6>
                    </div>
                </div>
                <div class="card-body p-4">
                    <ul class="list-unstyled d-flex flex-column gap-3 mb-0">
                        <li class="d-flex justify-content-between align-items-center">
                            <span style="color:#9ca3af;font-size:0.85rem;">Mã đơn hàng</span>
                            <span style="font-weight:700;color:#667eea;">#${order[0]}</span>
                        </li>
                        <li class="d-flex justify-content-between align-items-center">
                            <span style="color:#9ca3af;font-size:0.85rem;">Ngày đặt</span>
                            <span style="font-weight:600;font-size:0.85rem;">${order[4]}</span>
                        </li>
                        <li class="d-flex justify-content-between align-items-center">
                            <span style="color:#9ca3af;font-size:0.85rem;">Trạng thái hiện tại</span>
                            <c:choose>
                                <c:when test="${order[7] == 'Chờ xử lý'}">
                                    <span style="background:rgba(245,158,11,0.1);color:#f59e0b;padding:4px 12px;border-radius:999px;font-size:0.78rem;font-weight:600;">${order[7]}</span>
                                </c:when>
                                <c:when test="${order[7] == 'Đang giao'}">
                                    <span style="background:rgba(102,126,234,0.1);color:#667eea;padding:4px 12px;border-radius:999px;font-size:0.78rem;font-weight:600;">${order[7]}</span>
                                </c:when>
                                <c:when test="${order[7] == 'Đã giao'}">
                                    <span style="background:rgba(16,185,129,0.1);color:#10b981;padding:4px 12px;border-radius:999px;font-size:0.78rem;font-weight:600;">${order[7]}</span>
                                </c:when>
                                <c:otherwise>
                                    <span style="background:rgba(239,68,68,0.1);color:#ef4444;padding:4px 12px;border-radius:999px;font-size:0.78rem;font-weight:600;">${order[7]}</span>
                                </c:otherwise>
                            </c:choose>
                        </li>
                        <li style="border-top:1px solid #f3f4f6;padding-top:12px;" class="d-flex justify-content-between align-items-center">
                            <span style="color:#9ca3af;font-size:0.85rem;">Tổng tiền</span>
                            <span style="font-weight:800;font-size:1.1rem;background:linear-gradient(135deg,#f5576c,#ff6b6b);-webkit-background-clip:text;-webkit-text-fill-color:transparent;background-clip:text;">
                                <fmt:formatNumber value="${order[6]}" type="number" groupingUsed="true"/>₫
                            </span>
                        </li>
                    </ul>
                </div>
            </div>

            <!-- Form cập nhật trạng thái -->
            <div class="admin-card">
                <div style="padding:20px 24px 0;">
                    <div class="d-flex align-items-center gap-2">
                        <div style="width:28px;height:28px;border-radius:8px;background:linear-gradient(135deg,rgba(245,158,11,0.1),rgba(234,88,12,0.1));display:flex;align-items:center;justify-content:center;">
                            <i class="bi bi-pencil-square" style="color:#f59e0b;font-size:0.85rem;"></i>
                        </div>
                        <h6 style="font-weight:700;font-size:0.95rem;margin:0;">Cập nhật trạng thái</h6>
                    </div>
                </div>
                <div class="card-body p-4">
                    <form action="${pageContext.request.contextPath}/admin/orders/${order[0]}/status"
                          method="post">
                        <div style="margin-bottom:16px;">
                            <label for="statusSelect" style="font-size:0.82rem;font-weight:600;color:#9ca3af;margin-bottom:6px;display:block;">
                                Chọn trạng thái mới
                            </label>
                            <select id="statusSelect" name="status" style="width:100%;border-radius:12px;border:1.5px solid #e5e7eb;padding:10px 14px;font-size:0.9rem;outline:none;transition:all 0.2s ease;"
                                    onfocus="this.style.borderColor='#667eea'" onblur="this.style.borderColor='#e5e7eb'">
                                <c:forEach var="st" items="${statuses}">
                                    <option value="${st}" ${order[7] == st ? 'selected' : ''}>${st}</option>
                                </c:forEach>
                            </select>
                        </div>
                        <button type="submit" class="admin-btn-gradient w-100" style="padding:12px;font-size:0.92rem;justify-content:center;">
                            <i class="bi bi-check-lg"></i>Lưu thay đổi
                        </button>
                    </form>
                </div>
            </div>

        </div>
    </div>

<jsp:include page="layout/admin-footer.jsp"/>
