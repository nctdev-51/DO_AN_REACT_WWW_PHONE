<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<jsp:include page="/WEB-INF/views/shared/store/header.jsp" />
<section class="auth-panel panel">
  <span class="eyebrow">CHÀO MỪNG TRỞ LẠI</span>
  <h1>Đăng nhập</h1>
  <c:if test="${param.verified != null}"><div class="alert alert-success">Email đã được xác minh. Bạn có thể đăng nhập.</div></c:if>
  <c:if test="${param.reset != null}"><div class="alert alert-success">Đã đặt lại mật khẩu. Hãy đăng nhập bằng mật khẩu mới.</div></c:if>
  <c:if test="${param.expired != null}"><div class="alert alert-info">Phiên đăng nhập đã kết thúc. Vui lòng đăng nhập lại.</div></c:if>
  <c:if test="${param.error != null}">
    <div class="alert alert-danger">Email hoặc mật khẩu không đúng, tài khoản bị khóa hoặc email chưa được xác minh.</div>
  </c:if>
  <c:if test="${param.logout != null}">
    <div class="alert alert-success">Bạn đã đăng xuất.</div>
  </c:if>
  <form action="${pageContext.request.contextPath}/login" method="post">
    <ui:csrf/>
    <div class="field">
      <label for="username">Email / tài khoản</label>
      <input id="username" name="username" class="form-control" required autocomplete="username">
    </div>
    <div class="field">
      <label for="password">Mật khẩu</label>
      <input id="password" name="password" type="password" class="form-control" required autocomplete="current-password">
    </div>
    <button class="btn btn-brand w-100">Đăng nhập</button>
  </form>
  <p class="mt-4"><a href="${pageContext.request.contextPath}/forgot-password">Quên mật khẩu?</a></p>
  <p><a href="${pageContext.request.contextPath}/resend-verification">Gửi lại email xác minh</a></p>
  <p class="mt-4">Chưa có tài khoản? <a href="${pageContext.request.contextPath}/register">Đăng ký</a>
  </p>
</section>
<jsp:include page="/WEB-INF/views/shared/store/footer.jsp" />
