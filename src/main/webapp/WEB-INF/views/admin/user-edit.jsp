<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- currentPage, pageTitle, user được truyền từ AdminUserController --%>

<jsp:include page="layout/admin-header.jsp"/>

    <!-- Page Heading -->
    <div class="d-flex align-items-center justify-content-between mb-4">
        <div>
            <a href="${pageContext.request.contextPath}/admin/users"
               class="btn btn-sm btn-outline-secondary rounded-pill px-3 mb-2">
                <i class="bi bi-arrow-left me-1"></i>Quay lại danh sách
            </a>
            <h4 class="fw-bold mb-1">Sửa thông tin người dùng</h4>
            <p class="text-muted small mb-0">Cập nhật thông tin cho <strong>${user[1]}</strong></p>
        </div>
    </div>

    <div class="row justify-content-center">
        <div class="col-xl-7 col-lg-9">
            <div class="card border-0 shadow-sm rounded-4">
                <div class="card-header bg-white border-0 pt-4 px-4 pb-0">
                    <div class="d-flex align-items-center gap-3">
                        <div class="rounded-circle bg-primary bg-opacity-10 d-flex align-items-center justify-content-center"
                             style="width:50px;height:50px;">
                            <i class="bi bi-person text-primary fs-4"></i>
                        </div>
                        <div>
                            <h6 class="fw-bold mb-0">${user[1]}</h6>
                            <small class="text-muted">${user[2]}</small>
                        </div>
                    </div>
                </div>
                <div class="card-body p-4">
                    <form action="${pageContext.request.contextPath}/admin/users/${user[0]}/edit"
                          method="post">

                        <!-- Họ tên -->
                        <div class="mb-3">
                            <label for="fullName" class="form-label fw-semibold small">
                                Họ và tên <span class="text-danger">*</span>
                            </label>
                            <input type="text" id="fullName" name="fullName"
                                   class="form-control rounded-3"
                                   value="${user[1]}" required maxlength="100"
                                   placeholder="Nhập họ và tên">
                        </div>

                        <!-- Email (readonly - không cho sửa) -->
                        <div class="mb-3">
                            <label for="email" class="form-label fw-semibold small">
                                Email <span class="text-muted fw-normal">(không thể thay đổi)</span>
                            </label>
                            <input type="email" id="email" name="email"
                                   class="form-control rounded-3 bg-light"
                                   value="${user[2]}" readonly>
                        </div>

                        <!-- Số điện thoại -->
                        <div class="mb-3">
                            <label for="phone" class="form-label fw-semibold small">Số điện thoại</label>
                            <input type="tel" id="phone" name="phone"
                                   class="form-control rounded-3"
                                   value="${user[3]}" maxlength="20"
                                   placeholder="Nhập số điện thoại">
                        </div>

                        <!-- Địa chỉ -->
                        <div class="mb-3">
                            <label for="address" class="form-label fw-semibold small">Địa chỉ</label>
                            <textarea id="address" name="address"
                                      class="form-control rounded-3" rows="2"
                                      maxlength="255"
                                      placeholder="Nhập địa chỉ">${user[4]}</textarea>
                        </div>

                        <!-- Trạng thái -->
                        <div class="mb-4">
                            <label for="status" class="form-label fw-semibold small">Trạng thái tài khoản</label>
                            <select id="status" name="status" class="form-select rounded-3">
                                <option value="1" ${user[5] == 1 ? 'selected' : ''}>Hoạt động</option>
                                <option value="0" ${user[5] == 0 ? 'selected' : ''}>Đã khóa</option>
                            </select>
                        </div>

                        <!-- Nút hành động -->
                        <div class="d-flex gap-3">
                            <button type="submit" class="btn btn-primary rounded-pill px-4 fw-semibold">
                                <i class="bi bi-check-lg me-2"></i>Lưu thay đổi
                            </button>
                            <a href="${pageContext.request.contextPath}/admin/users"
                               class="btn btn-outline-secondary rounded-pill px-4">
                                <i class="bi bi-x me-1"></i>Hủy
                            </a>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>

<jsp:include page="layout/admin-footer.jsp"/>
