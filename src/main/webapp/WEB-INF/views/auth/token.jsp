<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<jsp:include page="/WEB-INF/views/shared/store/header.jsp" />
<section class="auth-panel panel">
  <h1>${verification ? 'Xác minh email' : 'Đặt lại mật khẩu'}</h1>
  <c:if test="${not empty error}"><div class="alert alert-danger"><c:out value="${error}"/></div></c:if>
  <c:if test="${valid}">
    <form method="post" action="${pageContext.request.contextPath}/${verification ? 'verify-email' : 'reset-password'}">
      <ui:csrf/>
      <input type="hidden" name="token" value="<c:out value='${token}'/>">
      <c:choose>
        <c:when test="${verification}"><p>Xác nhận để kích hoạt tài khoản và bắt đầu mua sắm.</p></c:when>
        <c:otherwise>
          <div class="field"><label for="password">Mật khẩu mới</label>
            <input id="password" name="password" type="password" class="form-control" minlength="8" maxlength="72" autocomplete="new-password" required>
          </div>
          <div class="field"><label for="confirmation">Nhập lại mật khẩu mới</label>
            <input id="confirmation" name="confirmation" type="password" class="form-control" minlength="8" maxlength="72" autocomplete="new-password" required>
          </div>
          <p>Tối thiểu 8 ký tự, tối đa 72 byte UTF-8. Sau khi đổi mật khẩu, bạn cần đăng nhập lại.</p>
        </c:otherwise>
      </c:choose>
      <button class="btn btn-brand w-100">${verification ? 'Xác minh tài khoản' : 'Lưu mật khẩu mới'}</button>
    </form>
  </c:if>
  <p class="mt-4"><a href="${pageContext.request.contextPath}/forgot-password">Yêu cầu đặt lại mật khẩu</a></p>
  <p><a href="${pageContext.request.contextPath}/resend-verification">Gửi lại email xác minh</a></p>
  <p><a href="${pageContext.request.contextPath}/login">Quay lại đăng nhập</a></p>
</section>
<jsp:include page="/WEB-INF/views/shared/store/footer.jsp" />
