<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- currentPage, pageTitle, users được truyền từ AdminUserController --%>

<jsp:include page="layout/admin-header.jsp"/>

    <!-- Page Heading -->
    <div class="d-flex align-items-center justify-content-between mb-4">
        <div>
            <h4 style="font-weight:800;font-size:1.4rem;margin-bottom:4px;letter-spacing:-0.3px;">Quản lý người dùng</h4>
            <p style="color:#9ca3af;font-size:0.88rem;margin-bottom:0;">Danh sách khách hàng đã đăng ký trong hệ thống</p>
        </div>
    </div>

    <!-- Flash messages -->
    <c:if test="${not empty successMsg}">
        <div style="background:rgba(16,185,129,0.08);border:1px solid rgba(16,185,129,0.15);border-radius:12px;padding:14px 18px;margin-bottom:20px;color:#10b981;font-size:0.88rem;display:flex;align-items:center;gap:8px;" role="alert">
            <i class="bi bi-check-circle"></i>${successMsg}
            <button type="button" class="btn-close" data-bs-dismiss="alert" style="margin-left:auto;"></button>
        </div>
    </c:if>
    <c:if test="${not empty errorMsg}">
        <div style="background:rgba(239,68,68,0.08);border:1px solid rgba(239,68,68,0.15);border-radius:12px;padding:14px 18px;margin-bottom:20px;color:#ef4444;font-size:0.88rem;display:flex;align-items:center;gap:8px;" role="alert">
            <i class="bi bi-exclamation-triangle"></i>${errorMsg}
            <button type="button" class="btn-close" data-bs-dismiss="alert" style="margin-left:auto;"></button>
        </div>
    </c:if>

    <!-- Bảng người dùng -->
    <div class="admin-card">
        <div style="padding:20px 24px 16px;display:flex;justify-content:space-between;align-items:center;">
            <div class="d-flex align-items-center gap-2">
                <div style="width:28px;height:28px;border-radius:8px;background:linear-gradient(135deg,rgba(102,126,234,0.1),rgba(118,75,162,0.1));display:flex;align-items:center;justify-content:center;">
                    <i class="bi bi-people" style="color:#667eea;font-size:0.85rem;"></i>
                </div>
                <h6 style="font-weight:700;font-size:0.95rem;margin:0;">Tất cả người dùng</h6>
            </div>
            <span style="background:linear-gradient(135deg,rgba(102,126,234,0.1),rgba(118,75,162,0.1));color:#667eea;padding:4px 14px;border-radius:999px;font-size:0.8rem;font-weight:600;">${users.size()} người dùng</span>
        </div>
        <div class="card-body p-0">
            <div class="table-responsive">
                <table class="table admin-table align-middle mb-0">
                    <thead>
                        <tr>
                            <th class="px-4">ID</th>
                            <th>Khách hàng</th>
                            <th>Số điện thoại</th>
                            <th>Địa chỉ</th>
                            <th class="text-center">Đơn hàng</th>
                            <th class="text-center">Trạng thái</th>
                            <th class="text-center">Hành động</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="user" items="${users}">
                            <tr>
                                <td class="px-4" style="color:#9ca3af;font-size:0.85rem;">${user[0]}</td>
                                <td>
                                    <div class="d-flex align-items-center gap-2">
                                        <div style="width:34px;height:34px;border-radius:10px;background:linear-gradient(135deg,rgba(102,126,234,0.1),rgba(118,75,162,0.1));display:flex;align-items:center;justify-content:center;flex-shrink:0;">
                                            <i class="bi bi-person" style="color:#667eea;font-size:0.85rem;"></i>
                                        </div>
                                        <div>
                                            <div style="font-weight:600;font-size:0.85rem;">${user[1]}</div>
                                            <div style="color:#9ca3af;font-size:0.72rem;">${user[2]}</div>
                                        </div>
                                    </div>
                                </td>
                                <td style="color:#9ca3af;font-size:0.85rem;">${user[3]}</td>
                                <td style="max-width:180px;">
                                    <span style="color:#9ca3af;font-size:0.85rem;display:block;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;max-width:160px;" title="${user[4]}">${user[4]}</span>
                                </td>
                                <td class="text-center">
                                    <c:choose>
                                        <c:when test="${user[6] > 0}">
                                            <span style="background:rgba(102,126,234,0.1);color:#667eea;padding:4px 12px;border-radius:999px;font-size:0.78rem;font-weight:600;">${user[6]} đơn</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span style="color:#d1d5db;font-size:0.85rem;">—</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td class="text-center">
                                    <c:choose>
                                        <c:when test="${user[5] == 1}">
                                            <span style="background:rgba(16,185,129,0.1);color:#10b981;padding:5px 14px;border-radius:999px;font-size:0.78rem;font-weight:600;display:inline-flex;align-items:center;gap:4px;">
                                                <i class="bi bi-check-circle"></i>Hoạt động
                                            </span>
                                        </c:when>
                                        <c:otherwise>
                                            <span style="background:rgba(156,163,175,0.1);color:#9ca3af;padding:5px 14px;border-radius:999px;font-size:0.78rem;font-weight:600;display:inline-flex;align-items:center;gap:4px;">
                                                <i class="bi bi-lock"></i>Đã khóa
                                            </span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td class="text-center">
                                    <div class="d-flex justify-content-center gap-2">
                                        <a href="${pageContext.request.contextPath}/admin/users/${user[0]}/edit" class="admin-btn-gradient" style="padding:6px 14px;font-size:0.8rem;">
                                            <i class="bi bi-pencil"></i>Sửa
                                        </a>
                                        <c:choose>
                                            <c:when test="${user[6] > 0}">
                                                <button disabled title="Không thể xóa vì đã có ${user[6]} đơn hàng"
                                                        style="padding:6px 14px;border-radius:10px;font-size:0.8rem;font-weight:600;color:#d1d5db;background:#f8f9fc;border:1px solid #e5e7eb;cursor:not-allowed;display:inline-flex;align-items:center;gap:4px;">
                                                    <i class="bi bi-trash"></i>Xóa
                                                </button>
                                            </c:when>
                                            <c:otherwise>
                                                <a href="${pageContext.request.contextPath}/admin/users/${user[0]}/delete"
                                                   onclick="return confirm('Xác nhận xóa người dùng \'${user[1]}\'?')"
                                                   style="padding:6px 14px;border-radius:10px;font-size:0.8rem;font-weight:600;color:#ef4444;background:rgba(239,68,68,0.08);text-decoration:none;display:inline-flex;align-items:center;gap:4px;transition:all 0.2s ease;"
                                                   onmouseover="this.style.background='rgba(239,68,68,0.15)'" onmouseout="this.style.background='rgba(239,68,68,0.08)'">
                                                    <i class="bi bi-trash"></i>Xóa
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
