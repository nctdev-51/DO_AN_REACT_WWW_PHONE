<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- currentPage, pageTitle, users được truyền từ AdminUserController --%>

<jsp:include page="layout/admin-header.jsp"/>

    <!-- Page Heading -->
    <div class="d-flex align-items-center justify-content-between mb-4">
        <div>
            <h4 class="fw-bold mb-1">Quản lý người dùng</h4>
            <p class="text-muted small mb-0">Danh sách khách hàng đã đăng ký trong hệ thống</p>
        </div>
    </div>

    <!-- Flash messages -->
    <c:if test="${not empty successMsg}">
        <div class="alert alert-success alert-dismissible fade show rounded-3 mb-4" role="alert">
            <i class="bi bi-check-circle me-2"></i>${successMsg}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>
    <c:if test="${not empty errorMsg}">
        <div class="alert alert-danger alert-dismissible fade show rounded-3 mb-4" role="alert">
            <i class="bi bi-exclamation-triangle me-2"></i>${errorMsg}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>

    <!-- Bảng người dùng -->
    <div class="card border-0 shadow-sm rounded-4">
        <div class="card-header bg-white border-0 pt-4 px-4 pb-3 d-flex justify-content-between align-items-center">
            <h6 class="fw-bold mb-0">
                <i class="bi bi-people text-primary me-2"></i>Tất cả người dùng
            </h6>
            <span class="badge bg-primary rounded-pill">${users.size()} người dùng</span>
        </div>
        <div class="card-body p-0">
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0">
                    <thead class="table-light">
                        <tr>
                            <th class="px-4 py-3 small fw-semibold text-muted">ID</th>
                            <th class="py-3 small fw-semibold text-muted">Khách hàng</th>
                            <th class="py-3 small fw-semibold text-muted">Số điện thoại</th>
                            <th class="py-3 small fw-semibold text-muted">Địa chỉ</th>
                            <th class="py-3 small fw-semibold text-muted text-center">Đơn hàng</th>
                            <th class="py-3 small fw-semibold text-muted text-center">Trạng thái</th>
                            <th class="py-3 small fw-semibold text-muted text-center">Hành động</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="user" items="${users}">
                            <tr>
                                <!-- ID -->
                                <td class="px-4 text-muted small">${user[0]}</td>

                                <!-- Khách hàng -->
                                <td>
                                    <div class="d-flex align-items-center gap-2">
                                        <div class="rounded-circle bg-primary bg-opacity-10 d-flex align-items-center justify-content-center"
                                             style="width:36px;height:36px;min-width:36px;">
                                            <i class="bi bi-person text-primary"></i>
                                        </div>
                                        <div>
                                            <div class="fw-semibold small">${user[1]}</div>
                                            <div class="text-muted" style="font-size:0.75rem;">${user[2]}</div>
                                        </div>
                                    </div>
                                </td>

                                <!-- Số điện thoại -->
                                <td class="small text-muted">${user[3]}</td>

                                <!-- Địa chỉ -->
                                <td class="small text-muted" style="max-width:200px;">
                                    <span class="text-truncate d-block" style="max-width:180px;" title="${user[4]}">${user[4]}</span>
                                </td>

                                <!-- Số lượng đơn hàng -->
                                <td class="text-center">
                                    <c:choose>
                                        <c:when test="${user[6] > 0}">
                                            <span class="badge bg-info rounded-pill">${user[6]} đơn</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="text-muted small">—</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>

                                <!-- Trạng thái -->
                                <td class="text-center">
                                    <c:choose>
                                        <c:when test="${user[5] == 1}">
                                            <span class="badge rounded-pill bg-success px-3 py-2">
                                                <i class="bi bi-check-circle me-1"></i>Hoạt động
                                            </span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge rounded-pill bg-secondary px-3 py-2">
                                                <i class="bi bi-lock me-1"></i>Đã khóa
                                            </span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>

                                <!-- Hành động -->
                                <td class="text-center">
                                    <div class="d-flex justify-content-center gap-2">
                                        <!-- Nút Sửa -->
                                        <a href="${pageContext.request.contextPath}/admin/users/${user[0]}/edit"
                                           class="btn btn-sm btn-outline-primary rounded-pill px-3">
                                            <i class="bi bi-pencil me-1"></i>Sửa
                                        </a>

                                        <!-- Nút Xóa - disable nếu có đơn hàng -->
                                        <c:choose>
                                            <c:when test="${user[6] > 0}">
                                                <%-- Có đơn hàng: disable nút, tooltip giải thích --%>
                                                <button class="btn btn-sm btn-outline-danger rounded-pill px-3"
                                                        disabled title="Không thể xóa vì đã có ${user[6]} đơn hàng">
                                                    <i class="bi bi-trash me-1"></i>Xóa
                                                </button>
                                            </c:when>
                                            <c:otherwise>
                                                <%-- Chưa có đơn hàng: cho phép xóa --%>
                                                <a href="${pageContext.request.contextPath}/admin/users/${user[0]}/delete"
                                                   class="btn btn-sm btn-outline-danger rounded-pill px-3"
                                                   onclick="return confirm('Xác nhận xóa người dùng \'${user[1]}\'?')">
                                                    <i class="bi bi-trash me-1"></i>Xóa
                                                </a>
                                            </c:otherwise>
                                        </c:choose>
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
