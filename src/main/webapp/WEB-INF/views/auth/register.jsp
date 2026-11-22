<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<!doctype html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Tạo tài khoản — PTV Mobile</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/vendor/bootstrap/bootstrap.min.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tokens.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/auth-split.css">
</head>
<body class="auth-split-body">
  <div class="auth-split-container">
    <!-- CỘT 1: VISUAL MEDIA & BRANDING (Ảnh điện thoại nghệ thuật) -->
    <div class="auth-split-visual">
      <div class="visual-content">
        <a href="${pageContext.request.contextPath}/" class="visual-brand">
          <span class="brand-logo-icon">P</span> PTV <span>Mobile</span>
        </a>
        <div class="visual-caption">
          <span class="visual-badge">CÔNG NGHỆ CHÍNH HÃNG</span>
          <h2>Chọn công nghệ.<br>Theo cách của bạn.</h2>
          <p>Tạo tài khoản PTV Mobile ngay hôm nay để nhận thông báo ưu đãi độc quyền, tích điểm thưởng đổi quà và bảo hành chính hãng trọn đời.</p>
        </div>
        <a href="${pageContext.request.contextPath}/" class="back-home-link">
          ← Quay về trang chủ cửa hàng
        </a>
      </div>
    </div>

    <!-- CỘT 2: FORM PANEL (Đăng nhập / Đăng ký) -->
    <div class="auth-split-form-panel">
      <div class="auth-form-inner">
        <!-- Top header trên mobile -->
        <div class="auth-mobile-header d-lg-none">
          <a href="${pageContext.request.contextPath}/" class="visual-brand" style="color:#0f172a;">
            <span class="brand-logo-icon">P</span> PTV <span style="color:#64748b;">Mobile</span>
          </a>
          <a href="${pageContext.request.contextPath}/" class="mobile-back-link">
            ← Về trang chủ
          </a>
        </div>

        <!-- Tab Switcher: Đăng nhập / Đăng ký -->
        <div class="auth-tabs-pill" role="tablist">
          <a class="auth-tab-btn text-decoration-none" href="${pageContext.request.contextPath}/login">
            Đăng nhập
          </a>
          <button type="button" class="auth-tab-btn active" id="tab-register-btn">
            Đăng ký
          </button>
        </div>

        <!-- Alerts thông báo -->
        <c:if test="${param.success != null}">
          <div class="alert alert-success">Đăng ký thành công! Hãy kiểm tra hộp thư email và xác minh tài khoản trước khi đăng nhập. Liên kết có hiệu lực 24 giờ. <a href="${pageContext.request.contextPath}/resend-verification">Gửi lại email xác minh</a>.</div>
        </c:if>

        <!-- FORM ĐĂNG KÝ -->
        <div class="auth-form-section" id="section-register">
          <div class="form-heading">
            <h2>Tạo tài khoản mới</h2>
            <p>Trở thành thành viên PTV Mobile để nhận nhiều ưu đãi.</p>
          </div>
          <form:form modelAttribute="user" action="${pageContext.request.contextPath}/register" method="post">
            <form:errors path="*" element="div" cssClass="alert alert-danger"/>
            <div class="field">
              <label for="fullName">Họ và tên</label>
              <form:input id="fullName" path="fullName" cssClass="form-control" required="required" maxlength="100" placeholder="Nguyễn Văn A"/>
            </div>
            <div class="field">
              <label for="email">Địa chỉ Email</label>
              <form:input id="email" path="email" type="email" cssClass="form-control" required="required" maxlength="100" placeholder="name@example.com"/>
            </div>
            <div class="field">
              <label for="phone">Số điện thoại</label>
              <form:input id="phone" path="phone" cssClass="form-control" pattern="0[0-9]{9}" maxlength="10" placeholder="0912345678"/>
            </div>
            <div class="field">
              <label for="password">Mật khẩu (tối thiểu 8 ký tự)</label>
              <form:password id="password" path="password" cssClass="form-control" required="required" minlength="8" maxlength="72" autocomplete="new-password" placeholder="••••••••"/>
            </div>
            <div class="field">
              <label for="confirmPassword">Nhập lại mật khẩu</label>
              <form:password id="confirmPassword" path="confirmPassword" cssClass="form-control" required="required" autocomplete="new-password" placeholder="••••••••"/>
            </div>
            <button class="btn btn-brand">Tạo tài khoản</button>
          </form:form>

          <div class="auth-switch-text">
            Đã có tài khoản? <a href="${pageContext.request.contextPath}/login">Đăng nhập ngay</a>
          </div>
        </div>
      </div>
    </div>
  </div>
</body>
</html>
