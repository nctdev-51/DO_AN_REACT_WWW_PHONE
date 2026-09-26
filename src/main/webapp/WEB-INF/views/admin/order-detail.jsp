<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%-- currentPage, pageTitle, order, orderItems, statuses được truyền từ AdminOrderController --%>

<jsp:include page="layout/admin-header.jsp"/>

    <!-- Page Heading -->
    <div class="d-flex align-items-center justify-content-between mb-4">
        <div>
            <a href="${pageContext.request.contextPath}/admin/orders"
               class="btn btn-sm btn-outline-secondary rounded-pill px-3 mb-2">
                <i class="bi bi-arrow-left me-1"></i>Quay lại danh sách
            </a>
            <h4 class="fw-bold mb-1">Chi tiết đơn hàng <span class="text-primary">#${order[0]}</span></h4>
            <p class="text-muted small mb-0">Thông tin chi tiết và trạng thái đơn hàng</p>
        </div>
    </div>

    <div class="row g-4">

        <!-- ===== CỘT TRÁI: Thông tin đơn + Sản phẩm ===== -->
        <div class="col-xl-8">

            <!-- Thông tin khách hàng & giao hàng -->
            <div class="card border-0 shadow-sm rounded-4 mb-4">
                <div class="card-header bg-white border-0 pt-4 px-4 pb-0">
                    <h6 class="fw-bold mb-0">
                        <i class="bi bi-person-lines-fill text-primary me-2"></i>Thông tin khách hàng
                    </h6>
                </div>
                <div class="card-body p-4">
                    <div class="row g-3">
                        <div class="col-md-6">
                            <div class="d-flex align-items-center gap-3 p-3 bg-light rounded-3">
                                <div class="rounded-circle bg-primary bg-opacity-10 d-flex align-items-center justify-content-center"
                                     style="width:44px;height:44px;min-width:44px;">
                                    <i class="bi bi-person text-primary fs-5"></i>
                                </div>
                                <div>
                                    <div class="text-muted small">Họ tên</div>
                                    <div class="fw-semibold">${order[1]}</div>
                                </div>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="d-flex align-items-center gap-3 p-3 bg-light rounded-3">
                                <div class="rounded-circle bg-success bg-opacity-10 d-flex align-items-center justify-content-center"
                                     style="width:44px;height:44px;min-width:44px;">
                                    <i class="bi bi-envelope text-success fs-5"></i>
                                </div>
                                <div>
                                    <div class="text-muted small">Email</div>
                                    <div class="fw-semibold">${order[2]}</div>
                                </div>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="d-flex align-items-center gap-3 p-3 bg-light rounded-3">
                                <div class="rounded-circle bg-warning bg-opacity-10 d-flex align-items-center justify-content-center"
                                     style="width:44px;height:44px;min-width:44px;">
                                    <i class="bi bi-telephone text-warning fs-5"></i>
                                </div>
                                <div>
                                    <div class="text-muted small">Số điện thoại</div>
                                    <div class="fw-semibold">${order[3]}</div>
                                </div>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="d-flex align-items-center gap-3 p-3 bg-light rounded-3">
                                <div class="rounded-circle bg-danger bg-opacity-10 d-flex align-items-center justify-content-center"
                                     style="width:44px;height:44px;min-width:44px;">
                                    <i class="bi bi-geo-alt text-danger fs-5"></i>
                                </div>
                                <div>
                                    <div class="text-muted small">Địa chỉ giao hàng</div>
                                    <div class="fw-semibold">${order[5]}</div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Bảng sản phẩm trong đơn -->
            <div class="card border-0 shadow-sm rounded-4">
                <div class="card-header bg-white border-0 pt-4 px-4 pb-0">
                    <h6 class="fw-bold mb-0">
                        <i class="bi bi-phone text-primary me-2"></i>Sản phẩm trong đơn
                    </h6>
                </div>
                <div class="card-body p-0">
                    <div class="table-responsive">
                        <table class="table align-middle mb-0">
                            <thead class="table-light">
                                <tr>
                                    <th class="px-4 py-3 small fw-semibold text-muted">Tên sản phẩm</th>
                                    <th class="py-3 small fw-semibold text-muted text-center">Số lượng</th>
                                    <th class="py-3 small fw-semibold text-muted text-end">Đơn giá</th>
                                    <th class="py-3 small fw-semibold text-muted text-end px-4">Thành tiền</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="item" items="${orderItems}">
                                    <tr>
                                        <td class="px-4">
                                            <div class="d-flex align-items-center gap-2">
                                                <div class="rounded-2 bg-light d-flex align-items-center justify-content-center"
                                                     style="width:40px;height:40px;min-width:40px;">
                                                    <i class="bi bi-phone text-muted"></i>
                                                </div>
                                                <span class="fw-semibold small">${item[0]}</span>
                                            </div>
                                        </td>
                                        <td class="text-center small">${item[1]}</td>
                                        <td class="text-end small">
                                            <fmt:formatNumber value="${item[2]}" type="number" groupingUsed="true"/>₫
                                        </td>
                                        <td class="text-end px-4">
                                            <span class="fw-semibold text-danger small">
                                                <fmt:formatNumber value="${item[3]}" type="number" groupingUsed="true"/>₫
                                            </span>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                            <tfoot class="table-light">
                                <tr>
                                    <td colspan="3" class="px-4 py-3 text-end fw-bold">Tổng thanh toán:</td>
                                    <td class="px-4 py-3 text-end">
                                        <span class="fs-5 fw-bold text-danger">
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
            <div class="card border-0 shadow-sm rounded-4 mb-4">
                <div class="card-header bg-white border-0 pt-4 px-4 pb-0">
                    <h6 class="fw-bold mb-0">
                        <i class="bi bi-info-circle text-primary me-2"></i>Thông tin đơn
                    </h6>
                </div>
                <div class="card-body p-4">
                    <ul class="list-unstyled d-flex flex-column gap-3 mb-0">
                        <li class="d-flex justify-content-between align-items-center">
                            <span class="text-muted small">Mã đơn hàng</span>
                            <span class="fw-bold text-primary">#${order[0]}</span>
                        </li>
                        <li class="d-flex justify-content-between align-items-center">
                            <span class="text-muted small">Ngày đặt</span>
                            <span class="fw-semibold small">${order[4]}</span>
                        </li>
                        <li class="d-flex justify-content-between align-items-center">
                            <span class="text-muted small">Trạng thái hiện tại</span>
                            <c:choose>
                                <c:when test="${order[7] == 'Chờ xử lý'}">
                                    <span class="badge rounded-pill bg-warning text-dark">${order[7]}</span>
                                </c:when>
                                <c:when test="${order[7] == 'Đang giao'}">
                                    <span class="badge rounded-pill bg-primary">${order[7]}</span>
                                </c:when>
                                <c:when test="${order[7] == 'Đã giao'}">
                                    <span class="badge rounded-pill bg-success">${order[7]}</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge rounded-pill bg-danger">${order[7]}</span>
                                </c:otherwise>
                            </c:choose>
                        </li>
                        <li class="d-flex justify-content-between align-items-center border-top pt-3">
                            <span class="text-muted small">Tổng tiền</span>
                            <span class="fw-bold text-danger">
                                <fmt:formatNumber value="${order[6]}" type="number" groupingUsed="true"/>₫
                            </span>
                        </li>
                    </ul>
                </div>
            </div>

            <!-- Form cập nhật trạng thái -->
            <div class="card border-0 shadow-sm rounded-4">
                <div class="card-header bg-white border-0 pt-4 px-4 pb-0">
                    <h6 class="fw-bold mb-0">
                        <i class="bi bi-pencil-square text-warning me-2"></i>Cập nhật trạng thái
                    </h6>
                </div>
                <div class="card-body p-4">
                    <form action="${pageContext.request.contextPath}/admin/orders/${order[0]}/status"
                          method="post">
                        <div class="mb-3">
                            <label for="statusSelect" class="form-label small fw-semibold text-muted">
                                Chọn trạng thái mới
                            </label>
                            <select id="statusSelect" name="status" class="form-select rounded-3">
                                <c:forEach var="st" items="${statuses}">
                                    <option value="${st}" ${order[7] == st ? 'selected' : ''}>${st}</option>
                                </c:forEach>
                            </select>
                        </div>
                        <button type="submit" class="btn btn-primary w-100 rounded-pill fw-semibold">
                            <i class="bi bi-check-lg me-2"></i>Lưu thay đổi
                        </button>
                    </form>
                </div>
            </div>

        </div>
    </div>

<jsp:include page="layout/admin-footer.jsp"/>
