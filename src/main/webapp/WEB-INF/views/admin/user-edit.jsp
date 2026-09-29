<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- currentPage, pageTitle, user được truyền từ AdminUserController --%>

<jsp:include page="layout/admin-header.jsp"/>

    <!-- Page Heading -->
    <div class="d-flex align-items-center justify-content-between mb-4">
        <div>
            <a href="${pageContext.request.contextPath}/admin/users"
               style="display:inline-flex;align-items:center;gap:6px;color:#667eea;text-decoration:none;font-weight:600;font-size:0.85rem;padding:6px 14px;border-radius:999px;background:rgba(102,126,234,0.08);transition:all 0.2s ease;margin-bottom:12px;"
               onmouseover="this.style.background='rgba(102,126,234,0.15)'" onmouseout="this.style.background='rgba(102,126,234,0.08)'">
                <i class="bi bi-arrow-left"></i>Quay lại danh sách
            </a>
            <h4 style="font-weight:800;font-size:1.4rem;margin-bottom:4px;letter-spacing:-0.3px;">Sửa thông tin người dùng</h4>
            <p style="color:#9ca3af;font-size:0.88rem;margin-bottom:0;">Cập nhật thông tin cho <strong style="color:#1a1a2e;">${user[1]}</strong></p>
        </div>
    </div>

    <div class="row justify-content-center">
        <div class="col-xl-7 col-lg-9">
            <div class="admin-card">
                <div style="padding:24px 28px 0;">
                    <div class="d-flex align-items-center gap-3">
                        <div style="width:50px;height:50px;border-radius:14px;background:linear-gradient(135deg,rgba(102,126,234,0.1),rgba(118,75,162,0.1));display:flex;align-items:center;justify-content:center;">
                            <i class="bi bi-person" style="color:#667eea;font-size:1.3rem;"></i>
                        </div>
                        <div>
                            <h6 style="font-weight:700;margin:0;font-size:1rem;">${user[1]}</h6>
                            <span style="color:#9ca3af;font-size:0.82rem;">${user[2]}</span>
                        </div>
                    </div>
                </div>
                <div class="card-body" style="padding:24px 28px 28px;">
                    <form action="${pageContext.request.contextPath}/admin/users/${user[0]}/edit"
                          method="post">

                        <!-- Họ tên -->
                        <div style="margin-bottom:16px;">
                            <label for="fullName" style="font-size:0.82rem;font-weight:600;color:#6b7280;margin-bottom:6px;display:block;">
                                Họ và tên <span style="color:#ef4444;">*</span>
                            </label>
                            <input type="text" id="fullName" name="fullName"
                                   value="${user[1]}" required maxlength="100"
                                   placeholder="Nhập họ và tên"
                                   style="width:100%;border-radius:12px;border:1.5px solid #e5e7eb;padding:12px 16px;font-size:0.92rem;outline:none;transition:all 0.2s ease;"
                                   onfocus="this.style.borderColor='#667eea';this.style.boxShadow='0 0 0 3px rgba(102,126,234,0.1)'"
                                   onblur="this.style.borderColor='#e5e7eb';this.style.boxShadow='none'">
                        </div>

                        <!-- Email (readonly) -->
                        <div style="margin-bottom:16px;">
                            <label for="email" style="font-size:0.82rem;font-weight:600;color:#6b7280;margin-bottom:6px;display:block;">
                                Email <span style="color:#9ca3af;font-weight:400;">(không thể thay đổi)</span>
                            </label>
                            <input type="email" id="email" name="email"
                                   value="${user[2]}" readonly
                                   style="width:100%;border-radius:12px;border:1.5px solid #e5e7eb;padding:12px 16px;font-size:0.92rem;background:#f8f9fc;color:#9ca3af;">
                        </div>

                        <!-- Số điện thoại -->
                        <div style="margin-bottom:16px;">
                            <label for="phone" style="font-size:0.82rem;font-weight:600;color:#6b7280;margin-bottom:6px;display:block;">Số điện thoại</label>
                            <input type="tel" id="phone" name="phone"
                                   value="${user[3]}" maxlength="20"
                                   placeholder="Nhập số điện thoại"
                                   style="width:100%;border-radius:12px;border:1.5px solid #e5e7eb;padding:12px 16px;font-size:0.92rem;outline:none;transition:all 0.2s ease;"
                                   onfocus="this.style.borderColor='#667eea';this.style.boxShadow='0 0 0 3px rgba(102,126,234,0.1)'"
                                   onblur="this.style.borderColor='#e5e7eb';this.style.boxShadow='none'">
                        </div>

                        <!-- Địa chỉ -->
                        <div style="margin-bottom:16px;">
                            <label for="address" style="font-size:0.82rem;font-weight:600;color:#6b7280;margin-bottom:6px;display:block;">Địa chỉ</label>
                            <textarea id="address" name="address"
                                      rows="2" maxlength="255"
                                      placeholder="Nhập địa chỉ"
                                      style="width:100%;border-radius:12px;border:1.5px solid #e5e7eb;padding:12px 16px;font-size:0.92rem;outline:none;transition:all 0.2s ease;resize:vertical;"
                                      onfocus="this.style.borderColor='#667eea';this.style.boxShadow='0 0 0 3px rgba(102,126,234,0.1)'"
                                      onblur="this.style.borderColor='#e5e7eb';this.style.boxShadow='none'">${user[4]}</textarea>
                        </div>

                        <!-- Trạng thái -->
                        <div style="margin-bottom:24px;">
                            <label for="status" style="font-size:0.82rem;font-weight:600;color:#6b7280;margin-bottom:6px;display:block;">Trạng thái tài khoản</label>
                            <select id="status" name="status"
                                    style="width:100%;border-radius:12px;border:1.5px solid #e5e7eb;padding:12px 16px;font-size:0.92rem;outline:none;transition:all 0.2s ease;"
                                    onfocus="this.style.borderColor='#667eea'" onblur="this.style.borderColor='#e5e7eb'">
                                <option value="1" ${user[5] == 1 ? 'selected' : ''}>Hoạt động</option>
                                <option value="0" ${user[5] == 0 ? 'selected' : ''}>Đã khóa</option>
                            </select>
                        </div>

                        <!-- Nút hành động -->
                        <div class="d-flex gap-3">
                            <button type="submit" class="admin-btn-gradient" style="padding:12px 24px;font-size:0.92rem;">
                                <i class="bi bi-check-lg"></i>Lưu thay đổi
                            </button>
                            <a href="${pageContext.request.contextPath}/admin/users"
                               style="padding:12px 24px;border-radius:10px;font-size:0.92rem;font-weight:600;color:#6b7280;border:1.5px solid #e5e7eb;text-decoration:none;display:inline-flex;align-items:center;gap:6px;transition:all 0.2s ease;"
                               onmouseover="this.style.borderColor='#667eea';this.style.color='#667eea'" onmouseout="this.style.borderColor='#e5e7eb';this.style.color='#6b7280'">
                                <i class="bi bi-x"></i>Hủy
                            </a>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>

<jsp:include page="layout/admin-footer.jsp"/>
