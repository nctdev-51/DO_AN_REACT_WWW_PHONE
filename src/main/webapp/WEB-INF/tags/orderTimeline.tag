<%@ tag pageEncoding="UTF-8" %>
<%@ attribute name="order" required="true" type="iuh.fit.www_phone_ver1.entity.Order" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<section class="panel mt-4">
  <h2>
    Lịch sử xử lý
  </h2>
  <ol class="order-timeline">
    <c:forEach var="event" items="${order.events}">
      <li>
        <time>
          <c:out value="${event.createdAt}"/>
        </time>
        <strong>
          <c:out value="${event.note}"/>
        </strong>
        <small>
          <c:out value="${event.actor}"/>
          ·
          <c:out value="${event.orderStatus}"/>
        </small>
      </li>
    </c:forEach>
  </ol>
  <c:if test="${empty order.events}">
    <p class="muted">
      Đơn được tạo trước khi hệ thống ghi nhận lịch sử. Trạng thái hiện tại:
      <c:out value="${order.status}"/>
      .
    </p>
  </c:if>
</section>
