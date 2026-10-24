<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<jsp:include page="/WEB-INF/views/shared/store/header.jsp" />
<section class="success-panel panel">
  <h1>Kết quả thanh toán</h1>
  <p>Đơn hàng #${order.id}</p>
  <p class="fs-4">
    <ui:paymentStatus value="${order.paymentStatus}"/>
  </p>
  <c:if test="${order.paymentStatus == 'UNPAID'}">
    <p>Thanh toán đang được xác nhận. Bạn có thể theo dõi trạng thái trong đơn hàng.</p>
  </c:if>
  <a class="btn btn-brand" href="${pageContext.request.contextPath}/orders/${order.id}">Xem đơn hàng</a>
</section>
<jsp:include page="/WEB-INF/views/shared/store/footer.jsp" />
