<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<jsp:include page="/WEB-INF/views/shared/admin/header.jsp" />
<section class="panel">
  <div class="table-responsive">
    <table class="table align-middle">
      <thead>
        <tr>
          <th>Mã đơn</th>
          <th>Khách hàng</th>
          <th>Ngày đặt</th>
          <th>Tổng tiền</th>
          <th>Thanh toán</th>
          <th>Trạng thái</th>
          <th>
          </th>
        </tr>
      </thead>
      <tbody>
        <c:forEach var="order" items="${orders}">
          <tr>
            <td>#${order.id}</td>
            <td>
              <strong>
                <c:out value="${order.user.fullName}" default="Khách hàng"/>
              </strong>
              <small class="d-block muted">
                <c:out value="${order.user.email}"/>
              </small>
            </td>
            <td>
              <c:out value="${fn:replace(order.orderDate,'T',' ')}"/>
            </td>
            <td>
              <ui:money value="${order.totalAmount}"/>
            </td>
            <td>
              <c:out value="${order.paymentMethod}"/>
              <small class="d-block">
                <ui:paymentStatus value="${order.paymentStatus}"/>
              </small>
            </td>
            <td>
              <c:out value="${order.status}"/>
            </td>
            <td>
              <a class="btn btn-light btn-sm" href="${pageContext.request.contextPath}/admin/orders/${order.id}">Chi tiết</a>
            </td>
          </tr>
        </c:forEach>
      </tbody>
    </table>
  </div>
  <c:if test="${empty orders}">
    <p class="muted">Chưa có đơn hàng nào.</p>
  </c:if>
</section>
<jsp:include page="/WEB-INF/views/shared/admin/footer.jsp" />
