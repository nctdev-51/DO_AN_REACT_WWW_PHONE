<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="/WEB-INF/views/shared/store/header.jsp"/>
<section class="success-panel panel">
  <span class="eyebrow">PTV MOBILE</span>
  <h1>${statusCode}</h1>
  <c:choose>
    <c:when test="${statusCode == 404}">
      <h2>Không tìm thấy trang</h2>
      <p>Trang bạn tìm có thể đã được chuyển hoặc không còn tồn tại.</p>
    </c:when>
    <c:when test="${statusCode == 403}">
      <h2>Bạn không có quyền truy cập</h2>
      <p>Vui lòng sử dụng tài khoản có quyền phù hợp.</p>
    </c:when>
    <c:otherwise>
      <h2>Chưa thể xử lý yêu cầu</h2>
      <p>Vui lòng thử lại sau ít phút.</p>
    </c:otherwise>
  </c:choose>
  <a class="btn btn-brand" href="${pageContext.request.contextPath}/">Về trang chủ</a>
</section>
<jsp:include page="/WEB-INF/views/shared/store/footer.jsp"/>
