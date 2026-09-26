<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%-- currentPage, pageTitle, orders, statuses được truyền từ AdminOrderController --%>

<jsp:include page="layout/admin-header.jsp"/>

    <!-- Page Heading -->
    <div class="d-flex align-items-center justify-content-between mb-4">
        <div>
            <h4 class="fw-bold mb-1">Quản lý đơn hàng</h4>
            <p class="text-muted small mb-0">Danh sách tất cả đơn hàng trong hệ thống</p>
        </div>
    </div>

    <!-- Flash message sau khi cập nhật trạng thái -->
    <c:if test="${not empty successMsg}">
        <div class="alert alert-success alert-dismissible fade show rounded-3 mb-4" role="alert">
            <i class="bi bi-check-circle me-2"></i>${successMsg}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>

    <!-- Bảng đơn hàng -->
    <div class="card border-0 shadow-sm rounded-4">
        <div class="card-header bg-white border-0 pt-4 px-4 pb-3 d-flex justify-content-between align-items-center">
            <h6 class="fw-bold mb-0">
                <i class="bi bi-bag-check text-primary me-2"></i>Tất cả đơn hàng
            </h6>
            <span class="badge bg-primary rounded-pill">${orders.size()} đơn</span>
        </div>
        <div class="card-body p-0">
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0">
                    <thead class="table-light">
                        <tr>
                            <th class="px-4 py-3 small fw-semibold text-muted">Mã đơn</th>
                            <th class="py-3 small fw-semibold text-muted">Khách hàng</th>
                            <th class="py-3 small fw-semibold text-muted">Ngày đặt</th>
                            <th class="py-3 small fw-semibold text-muted">Tổng tiền</th>
                            <th class="py-3 small fw-semibold text-muted">Trạng thái</th>
                            <th class="py-3 small fw-semibold text-muted text-center">Hành động</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="order" items="${orders}">
                            <tr>
                                <!-- Mã đơn -->
                                <td class="px-4">
                                    <span class="fw-bold text-primary">#${order[0]}</span>
                                </td>

                                <!-- Khách hàng -->
                                <td>
                                    <div class="d-flex align-items-center gap-2">
                                        <div class="rounded-circle bg-primary bg-opacity-10 d-flex align-items-center justify-content-center"
                                             style="width:36px;height:36px;min-width:36px;">
                                            <i class="bi bi-person text-primary"></i>
                                        </div>
                                        <div>
                                            <div class="fw-semibold small">${order[1]}</div>
                                            <div class="text-muted" style="font-size:0.75rem;">${order[2]}</div>
                                        </div>
                                    </div>
                                </td>

                                <!-- Ngày đặt -->
                                <td class="small text-muted">${order[4]}</td>

                                <!-- Tổng tiền -->
                                <td>
                                    <span class="fw-semibold small">
                                        <fmt:formatNumber value="${order[6]}" type="number" groupingUsed="true"/>₫
                                    </span>
                                </td>

                                <!-- Trạng thái badge -->
                                <td>
                                    <c:choose>
                                        <c:when test="${order[7] == 'Chờ xử lý'}">
                                            <span class="badge rounded-pill bg-warning text-dark px-3 py-2">
                                                <i class="bi bi-hourglass-split me-1"></i>${order[7]}
                                            </span>
                                        </c:when>
                                        <c:when test="${order[7] == 'Đang giao'}">
                                            <span class="badge rounded-pill bg-primary px-3 py-2">
                                                <i class="bi bi-truck me-1"></i>${order[7]}
                                            </span>
                                        </c:when>
                                        <c:when test="${order[7] == 'Đã giao'}">
                                            <span class="badge rounded-pill bg-success px-3 py-2">
                                                <i class="bi bi-check-circle me-1"></i>${order[7]}
                                            </span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge rounded-pill bg-danger px-3 py-2">
                                                <i class="bi bi-x-circle me-1"></i>${order[7]}
                                            </span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>

                                <!-- Hành động -->
                                <td class="text-center">
                                    <div class="d-flex justify-content-center gap-2">
                                        <!-- Nút xem chi tiết -->
                                        <a href="${pageContext.request.contextPath}/admin/orders/${order[0]}"
                                           class="btn btn-sm btn-outline-primary rounded-pill px-3">
                                            <i class="bi bi-eye me-1"></i>Chi tiết
                                        </a>
                                        <!-- Form cập nhật nhanh trạng thái -->
                                        <form action="${pageContext.request.contextPath}/admin/orders/${order[0]}/status"
                                              method="post" class="d-flex gap-1 align-items-center">
                                            <select name="status" class="form-select form-select-sm rounded-pill"
                                                    style="width:130px;font-size:0.8rem;">
                                                <c:forEach var="st" items="${statuses}">
                                                    <option value="${st}" ${order[7] == st ? 'selected' : ''}>${st}</option>
                                                </c:forEach>
                                            </select>
                                            <button type="submit" class="btn btn-sm btn-success rounded-pill px-2"
                                                    title="Lưu trạng thái">
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
