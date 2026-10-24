<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<jsp:include page="/WEB-INF/views/shared/store/header.jsp" />
<jsp:include page="/WEB-INF/views/shared/store/account-nav.jsp" />
<div class="page-heading">
  <div>
    <span class="eyebrow">TÀI KHOẢN CỦA BẠN</span>
    <h1>Đơn hàng của tôi</h1>
  </div>
</div>
<c:if test="${empty orders}">
  <div class="empty-state">
    <h2>Bạn chưa có đơn hàng</h2>
    <a class="btn btn-brand" href="${pageContext.request.contextPath}/products">Mua sắm ngay</a>
  </div>
</c:if>
<div class="order-list">
  <c:forEach var="order" items="${orders}">
    <article class="panel order-card">
      <div>
        <span class="eyebrow">ĐƠN HÀNG #${order.id}</span>
        <h3>
          <c:out value="${order.status}"/>
        </h3>
        <p class="muted">
          <c:out value="${fn:replace(order.orderDate,'T',' ')}"/>
        </p>
      </div>
      <div>
        <strong>
          <ui:money value="${order.totalAmount}"/>
        </strong>
        <p>
          <ui:paymentStatus value="${order.paymentStatus}"/>
        </p>
        <a class="btn btn-light" href="${pageContext.request.contextPath}/orders/${order.id}">Chi tiết →</a>
      </div>
    </article>
  </c:forEach>
</div>
<jsp:include page="/WEB-INF/views/shared/store/footer.jsp" />
