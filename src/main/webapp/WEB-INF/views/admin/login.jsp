<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="../components/header.jsp" />

<section class="container py-5">
    <div class="row justify-content-center">
        <div class="col-md-6 col-lg-5">
            <div class="card border-0 shadow-sm">
                <div class="card-body p-4 p-md-5">
                    <div class="text-center mb-4">
                        <i class="bi bi-shield-lock text-primary" style="font-size: 3rem;"></i>
                        <h1 class="h3 fw-bold mt-2">Đăng nhập quản trị</h1>
                        <p class="text-muted mb-0">Đăng nhập để quản lý sản phẩm và danh mục.</p>
                    </div>

                    <c:if test="${param.error != null}">
                        <div class="alert alert-danger" role="alert">Tên đăng nhập hoặc mật khẩu không đúng.</div>
                    </c:if>
                    <c:if test="${param.logout != null}">
                        <div class="alert alert-success" role="alert">Bạn đã đăng xuất khỏi trang quản trị.</div>
                    </c:if>

                    <form action="${pageContext.request.contextPath}/admin/login" method="post">
                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                        <div class="mb-3">
                            <label class="form-label" for="username">Tên đăng nhập</label>
                            <input class="form-control form-control-lg" id="username" name="username" autocomplete="username" required autofocus>
                        </div>
                        <div class="mb-4">
                            <label class="form-label" for="password">Mật khẩu</label>
                            <input class="form-control form-control-lg" type="password" id="password" name="password" autocomplete="current-password" required>
                        </div>
                        <button class="btn btn-primary btn-lg w-100" type="submit">Đăng nhập</button>
                    </form>
                </div>
            </div>
        </div>
    </div>
</section>
<jsp:include page="../components/footer.jsp" />
