<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<jsp:include page="/WEB-INF/views/shared/store/header.jsp" />
<section class="success-panel panel">
  <span class="success-icon">✓</span>
  <span class="eyebrow">CẢM ƠN BẠN</span>
  <h1>Đã nhận đơn hàng #${order.id}</h1>
  <p>Đơn hàng của bạn đang chờ xử lý. Bạn có thể theo dõi tiến trình giao hàng trong tài khoản.</p>
  <p class="price">
    <ui:money value="${order.totalAmount}"/>
  </p>
  <p>Giao đến: <c:out value="${order.shippingAddress}"/>
</p>
<div class="d-flex gap-3 justify-content-center">
  <a class="btn btn-brand" href="${pageContext.request.contextPath}/orders/${order.id}">Xem đơn hàng</a>
  <a class="btn btn-light" href="${pageContext.request.contextPath}/products">Tiếp tục mua sắm</a>
</div>
</section>
<jsp:include page="/WEB-INF/views/shared/store/footer.jsp" />
