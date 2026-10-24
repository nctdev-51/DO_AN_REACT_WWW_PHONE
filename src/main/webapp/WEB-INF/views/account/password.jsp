<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<jsp:include page="/WEB-INF/views/shared/store/header.jsp" />
<jsp:include page="/WEB-INF/views/shared/store/account-nav.jsp" />
<section class="panel form-panel">
  <h1>Đổi mật khẩu</h1>
  <form:form action="${pageContext.request.contextPath}/account/password" modelAttribute="passwordForm" method="post">
    <form:errors path="*" element="div" cssClass="alert alert-danger"/>
    <div class="field">
      <label for="currentPassword">Mật khẩu hiện tại</label>
      <form:password id="currentPassword" path="currentPassword" cssClass="form-control" required="required" autocomplete="current-password"/>
    </div>
    <div class="field">
      <label for="newPassword">Mật khẩu mới</label>
      <form:password id="newPassword" path="newPassword" cssClass="form-control" required="required" minlength="8" maxlength="72" autocomplete="new-password"/>
    </div>
    <div class="field">
      <label for="confirmPassword">Xác nhận mật khẩu mới</label>
      <form:password id="confirmPassword" path="confirmPassword" cssClass="form-control" required="required" autocomplete="new-password"/>
    </div>
    <button class="btn btn-brand">Đổi mật khẩu</button>
  </form:form>
</section>
<jsp:include page="/WEB-INF/views/shared/store/footer.jsp" />
