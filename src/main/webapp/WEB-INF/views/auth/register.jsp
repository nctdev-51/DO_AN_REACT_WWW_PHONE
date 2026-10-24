<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<jsp:include page="/WEB-INF/views/shared/store/header.jsp" />
<section class="auth-panel panel">
  <span class="eyebrow">BẮT ĐẦU VỚI PTV MOBILE</span>
  <h1>Tạo tài khoản</h1>
  <c:if test="${param.success != null}">
    <div class="alert alert-success">Đăng ký thành công. Hãy mở email và xác minh tài khoản trước khi đăng nhập. Liên kết có hiệu lực 24 giờ. <a href="${pageContext.request.contextPath}/resend-verification">Gửi lại email xác minh</a>.</div>
  </c:if>
  <form:form modelAttribute="user" action="${pageContext.request.contextPath}/register" method="post">
    <form:errors path="*" element="div" cssClass="alert alert-danger"/>
    <div class="field">
      <label for="fullName">Họ và tên</label>
      <form:input id="fullName" path="fullName" cssClass="form-control" required="required" maxlength="100"/>
    </div>
    <div class="field">
      <label for="email">Email</label>
      <form:input id="email" path="email" type="email" cssClass="form-control" required="required" maxlength="100"/>
    </div>
    <div class="field">
      <label for="phone">Số điện thoại</label>
      <form:input id="phone" path="phone" cssClass="form-control" pattern="0[0-9]{9}" maxlength="10"/>
    </div>
    <div class="field">
      <label for="password">Mật khẩu</label>
      <form:password id="password" path="password" cssClass="form-control" required="required" minlength="8" maxlength="72" autocomplete="new-password"/>
    </div>
    <div class="field">
      <label for="confirmPassword">Nhập lại mật khẩu</label>
      <form:password id="confirmPassword" path="confirmPassword" cssClass="form-control" required="required" autocomplete="new-password"/>
    </div>
    <button class="btn btn-brand w-100">Đăng ký</button>
  </form:form>
  <p class="mt-4">Đã có tài khoản? <a href="${pageContext.request.contextPath}/login">Đăng nhập</a>
  </p>
</section>
<jsp:include page="/WEB-INF/views/shared/store/footer.jsp" />
