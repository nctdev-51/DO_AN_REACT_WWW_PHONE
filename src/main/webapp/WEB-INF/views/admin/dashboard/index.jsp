<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<jsp:include page="/WEB-INF/views/shared/admin/header.jsp" />
<c:if test="${not empty successMsg}"><div class="alert alert-success alert-dismissible fade show" role="alert"><c:out value="${successMsg}"/><button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button></div></c:if>
<c:if test="${not empty errorMsg}"><div class="alert alert-danger alert-dismissible fade show" role="alert"><c:out value="${errorMsg}"/><button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button></div></c:if>
<section class="stat-grid">
  <article class="panel">
    <span>Tổng đơn hàng</span>
    <strong>${totalOrders}</strong>
  </article>
  <article class="panel">
    <span>Giá trị đơn giao sau hoàn tiền</span>
    <strong>${totalRevenue}</strong><small>Đã giao: ${grossRevenue}<br>Đã hoàn: ${refundedRevenue}</small>
  </article>
  <article class="panel">
    <span>Tài khoản</span>
    <strong>${totalUsers}</strong>
  </article>
  <article class="panel">
    <span>Đơn chờ xử lý</span>
    <strong>${pendingOrders}</strong>
  </article>
</section>
<section class="panel mb-4">
  <h2>Giá trị đơn giao 6 tháng</h2>
  <p class="muted small">Tính theo tháng giao hàng (đơn cũ dùng ngày đặt), gồm phí giao. Chỉ trừ khoản hoàn đã xác nhận và cập nhật về tháng của đơn; đây không phải biểu đồ dòng tiền theo ngày hoàn.</p>
  <div class="chart-box">
    <canvas id="revenue-chart" data-labels="<c:out value='${chartLabels}'/>" data-values="<c:out value='${chartData}'/>">
    </canvas>
  </div>
</section>
<section class="panel">
  <div class="section-heading">
    <h2>Đơn hàng gần đây</h2>
    <a href="${pageContext.request.contextPath}/admin/orders">Tất cả đơn hàng →</a>
  </div>
  <div class="table-responsive">
    <table class="table">
      <thead>
        <tr>
          <th>Mã đơn</th>
          <th>Khách hàng</th>
          <th>Tổng tiền</th>
          <th>Trạng thái</th>
        </tr>
      </thead>
      <tbody>
        <c:forEach var="order" items="${recentOrders}">
          <tr>
            <td>
              <a href="${pageContext.request.contextPath}/admin/orders/${order.id}">#${order.id}</a>
            </td>
            <td>
              <c:out value="${order.user.fullName}" default="Khách hàng"/>
            </td>
            <td>
              <ui:money value="${order.totalAmount}"/>
            </td>
            <td>
              <ui:adminOrderStatus value="${order.status}"/>
            </td>
          </tr>
        </c:forEach>
      </tbody>
    </table>
  </div>
</section>
<jsp:include page="/WEB-INF/views/shared/admin/footer.jsp" />
