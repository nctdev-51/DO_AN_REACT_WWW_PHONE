<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<!doctype html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Tài khoản PTV Mobile — Đăng nhập & Đăng ký</title>
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
=======
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
>>>>>>> origin/taidev
=======
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
>>>>>>> origin/taidev
=======
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
>>>>>>> origin/taidev
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
          <p>Đăng nhập tài khoản PTV Mobile để theo dõi đơn hàng, quản lý thiết bị bảo hành và nhận điểm thưởng tích lũy sau mỗi lần mua sắm.</p>
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
          <button type="button" class="auth-tab-btn active" id="tab-login-btn" onclick="switchAuthTab('login')">
            Đăng nhập
          </button>
          <button type="button" class="auth-tab-btn" id="tab-register-btn" onclick="switchAuthTab('register')">
            Đăng ký
          </button>
        </div>

        <!-- Alerts thông báo -->
        <c:if test="${param.verified != null}"><div class="alert alert-success">Email đã được xác minh. Bạn có thể đăng nhập ngay.</div></c:if>
        <c:if test="${param.reset != null}"><div class="alert alert-success">Đã đặt lại mật khẩu thành công. Hãy đăng nhập bằng mật khẩu mới.</div></c:if>
        <c:if test="${param.expired != null}"><div class="alert alert-info">Phiên đăng nhập đã kết thúc. Vui lòng đăng nhập lại.</div></c:if>
        <c:if test="${param.registered != null or not empty successMsg}">
          <div class="alert alert-success">
            🎉 <strong>Đăng ký thành công!</strong> Bạn có thể đăng nhập ngay bằng email hoặc số điện thoại vừa tạo.
          </div>
        </c:if>
        <c:if test="${param.logout != null}">
          <div class="alert alert-success">Bạn đã đăng xuất an toàn.</div>
        </c:if>
        <c:if test="${param.success != null}">
          <div class="alert alert-success">Đăng ký thành công! Hãy kiểm tra hộp thư email để xác minh tài khoản trước khi đăng nhập.</div>
        </c:if>

        <!-- FORM ĐĂNG NHẬP -->
        <div class="auth-form-section" id="section-login">
          <div class="form-heading">
            <h2>Chào mừng trở lại</h2>
            <p>Nhập thông tin tài khoản để tiếp tục mua sắm.</p>
          </div>
          <form action="${pageContext.request.contextPath}/login" method="post"
                data-account-memory data-context="${pageContext.request.contextPath}">
            <ui:csrf/>
            <c:if test="${not empty redirectTarget}">
              <input type="hidden" name="redirect" value="<c:out value='${redirectTarget}'/>">
            </c:if>
            <div class="field">
              <label for="username">Email hoặc số điện thoại</label>
              <input id="username" name="username" class="form-control" required autocomplete="username" placeholder="name@example.com hoặc 09xxxxxxxx">
            </div>
            <div class="field">
              <label for="password">Mật khẩu</label>
              <div class="login-password-control">
                <input id="password" name="password" type="password" class="form-control" required autocomplete="current-password" placeholder="••••••••">
                <button class="login-password-toggle" type="button" data-password-toggle aria-controls="password" aria-pressed="false" hidden>Hiện</button>
              </div>
              <c:if test="${param.error != null}">
                <div style="color: #dc2626; font-size: 0.875rem; margin-top: 8px; font-weight: 500;">
                  Email, số điện thoại hoặc mật khẩu không đúng, tài khoản bị khóa hoặc email chưa được xác minh.
                </div>
              </c:if>
            </div>
            <label class="login-remember"><input type="checkbox" data-remember-account> Nhớ tên tài khoản trên thiết bị này</label>
            <button class="btn btn-brand">Đăng nhập</button>
          </form>

          <div class="auth-links-row">
            <a href="${pageContext.request.contextPath}/forgot-password">Quên mật khẩu?</a>
            <a href="${pageContext.request.contextPath}/resend-verification">Gửi lại email xác minh</a>
          </div>

          <div class="auth-switch-text">
            Chưa có tài khoản? <a href="javascript:void(0)" onclick="switchAuthTab('register')">Đăng ký ngay</a>
          </div>
        </div>

        <!-- FORM ĐĂNG KÝ -->
        <div class="auth-form-section d-none" id="section-register">
          <div class="form-heading">
            <h2>Tạo tài khoản mới</h2>
            <p>Trở thành thành viên PTV Mobile để nhận nhiều ưu đãi.</p>
          </div>
          <form action="${pageContext.request.contextPath}/register" method="post">
            <ui:csrf/>
            <div class="field">
              <label for="fullName">Họ và tên</label>
              <input id="fullName" name="fullName" class="form-control" required maxlength="100" placeholder="Nguyễn Văn A">
            </div>
            <div class="field">
              <label for="reg-email">Địa chỉ Email</label>
              <input id="reg-email" name="email" type="email" class="form-control" required maxlength="100" placeholder="name@example.com">
            </div>
            <div class="field">
              <label for="phone">Số điện thoại</label>
              <input id="phone" name="phone" class="form-control" pattern="0[0-9]{9}" maxlength="10" placeholder="0912345678">
            </div>
            <div class="field">
              <label for="reg-password">Mật khẩu (tối thiểu 8 ký tự)</label>
              <input id="reg-password" name="password" type="password" class="form-control" required minlength="8" maxlength="72" autocomplete="new-password" placeholder="••••••••">
            </div>
            <div class="field">
              <label for="confirmPassword">Nhập lại mật khẩu</label>
              <input id="confirmPassword" name="confirmPassword" type="password" class="form-control" required autocomplete="new-password" placeholder="••••••••">
            </div>
            <button class="btn btn-brand">Tạo tài khoản</button>
          </form>

          <div class="auth-switch-text">
            Đã có tài khoản? <a href="javascript:void(0)" onclick="switchAuthTab('login')">Đăng nhập ngay</a>
          </div>
        </div>
      </div>
    </div>
  </div>

  <script src="${pageContext.request.contextPath}/js/login-account.js" defer></script>
  <script>
    function switchAuthTab(tab) {
      var loginSec = document.getElementById('section-login');
      var regSec = document.getElementById('section-register');
      var loginBtn = document.getElementById('tab-login-btn');
      var regBtn = document.getElementById('tab-register-btn');

      if (tab === 'register') {
        loginSec.classList.add('d-none');
        regSec.classList.remove('d-none');
        loginBtn.classList.remove('active');
        regBtn.classList.add('active');
        history.replaceState(null, '', '${pageContext.request.contextPath}/login?tab=register');
      } else {
        regSec.classList.add('d-none');
        loginSec.classList.remove('d-none');
        regBtn.classList.remove('active');
        loginBtn.classList.add('active');
        history.replaceState(null, '', '${pageContext.request.contextPath}/login');
      }
    }

    // Tự động chuyển tab nếu url có tab=register hoặc đăng ký lỗi
    (function() {
      var params = new URLSearchParams(window.location.search);
      if (params.get('tab') === 'register' || window.location.pathname.indexOf('/register') !== -1) {
        switchAuthTab('register');
      }

      // Giữ lại email/sdt khi nhập sai để không phải gõ lại
      var userInput = document.getElementById('username');
      var loginForm = document.querySelector('#section-login form');
      if (userInput && loginForm) {
        var saved = sessionStorage.getItem('last_login_id');
        if (saved && !userInput.value) {
          userInput.value = saved;
        }
        loginForm.addEventListener('submit', function() {
          if (userInput.value) {
            sessionStorage.setItem('last_login_id', userInput.value.trim());
          }
        });
      }
    })();
  </script>
</body>
</html>
