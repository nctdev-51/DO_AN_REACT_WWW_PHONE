<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<jsp:include page="/WEB-INF/views/shared/store/header.jsp" />
<jsp:include page="/WEB-INF/views/shared/store/account-nav.jsp" />
<section class="panel form-panel">
  <h1>Thông tin tài khoản</h1>
  <p class="muted">
    <c:out value="${email}"/>
  </p>
  <form:form action="${pageContext.request.contextPath}/account" modelAttribute="profileForm" method="post">
    <form:errors path="*" element="div" cssClass="alert alert-danger"/>
    <div class="field">
      <label for="fullName">Họ tên</label>
      <form:input id="fullName" path="fullName" cssClass="form-control" required="required" maxlength="100"/>
    </div>
    <div class="field">
      <label for="phone">Số điện thoại</label>
      <form:input id="phone" path="phone" cssClass="form-control" maxlength="10" pattern="0[0-9]{9}"/>
    </div>
    <button class="btn btn-brand">Lưu thay đổi</button>
  </form:form>
</section>
<jsp:include page="/WEB-INF/views/shared/store/footer.jsp" />
