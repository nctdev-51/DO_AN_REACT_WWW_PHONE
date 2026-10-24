<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<jsp:include page="/WEB-INF/views/shared/store/header.jsp" />
<section class="auth-panel panel">
  <span class="eyebrow">TÀI KHOẢN PTV MOBILE</span>
  <h1>${verification ? 'Gửi lại email xác minh' : 'Quên mật khẩu'}</h1>
  <p>Nhập email đã đăng ký để nhận liên kết ${verification ? 'xác minh tài khoản' : 'đặt lại mật khẩu'}.</p>
  <c:if test="${param.sent != null}">
    <div class="alert alert-success">Nếu email phù hợp với tài khoản đang hoạt động, liên kết sẽ được gửi đến hộp thư. Hãy kiểm tra cả thư rác. Chờ ít nhất 60 giây trước khi yêu cầu lại.</div>
  </c:if>
  <form method="post" action="${pageContext.request.contextPath}/${verification ? 'resend-verification' : 'forgot-password'}">
    <ui:csrf/>
    <div class="field"><label for="email">Email đã đăng ký</label>
      <input id="email" name="email" type="email" class="form-control" maxlength="100" required autocomplete="email">
    </div>
    <button class="btn btn-brand w-100">Gửi liên kết</button>
  </form>
  <p class="mt-4"><a href="${pageContext.request.contextPath}/login">Quay lại đăng nhập</a></p>
</section>
<jsp:include page="/WEB-INF/views/shared/store/footer.jsp" />
