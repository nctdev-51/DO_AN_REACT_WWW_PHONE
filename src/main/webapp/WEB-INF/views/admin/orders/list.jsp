<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<jsp:include page="/WEB-INF/views/shared/admin/header.jsp" />
<section class="panel">
    <form method="get" action="${pageContext.request.contextPath}/admin/orders" class="row g-2 align-items-end mb-3">
    <div class="col-sm-6 col-xl">
      <label for="order-code" class="form-label">Mã đơn</label>
      <input id="order-code" class="form-control" name="code" type="search" placeholder="#123" value="<c:out value='${orderCode}'/>">
    </div>
    <div class="col-sm-6 col-xl">
      <label for="order-placed-on" class="form-label">Ngày đặt</label>
      <input id="order-placed-on" class="form-control" name="placedOn" type="date" value="${placedOn}">
    </div>
    <div class="col-sm-6 col-xl">
      <label for="order-state" class="form-label">Trạng thái</label>
      <select id="order-state" class="form-select" name="status">
        <option value="">Tất cả trạng thái</option>
        <c:forEach var="option" items="${statuses}">
          <option value="<c:out value='${option}'/>" ${orderStatus == option ? 'selected' : ''}><c:out value="${option}"/></option>
        </c:forEach>
      </select>
    </div>
    <div class="col-sm-6 col-xl">
      <label for="order-phone" class="form-label">Số điện thoại</label>
      <input id="order-phone" class="form-control" name="phone" type="tel" maxlength="24" value="<c:out value='${orderPhone}'/>">
    </div>
    <div class="col-auto">
      <button type="submit" class="btn btn-brand">Lọc</button>
      <c:if test="${hasFilters}"><a class="btn btn-light" href="${pageContext.request.contextPath}/admin/orders">Xóa bộ lọc</a></c:if>
    </div>
  </form>
  <c:if test="${not empty filterError}"><p class="alert alert-warning"><c:out value="${filterError}"/></p></c:if>
  <ui:adminBatchForm action="/admin/orders/batch">
    <option value="Đang giao">Chuyển sang đang giao</option>
    <option value="Đã giao">Xác nhận đã giao</option>
    <option value="Đã hủy" data-destructive>Hủy đơn</option>
  </ui:adminBatchForm>
  <div class="table-responsive">
    <table class="table align-middle">
      <thead>
        <tr>
          <th scope="col"><input type="checkbox" data-batch-all aria-label="Chọn tất cả kết quả"></th>
          <th>Mã đơn</th>
          <th>Khách hàng</th>
          <th>Số điện thoại</th>
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
            <td><input type="checkbox" name="ids" value="${order.id}" form="admin-batch" data-batch-label="<c:out value='#${order.id} · ${order.user.fullName}'/>"
                data-batch-actions="${order.status == 'Chờ xử lý' ? (order.paymentMethod != 'VNPAY' or order.paymentStatus == 'PAID' ? 'Đang giao|Đã hủy' : 'Đã hủy') : (order.status == 'Đang giao' and (order.paymentMethod != 'VNPAY' or order.paymentStatus == 'PAID') ? 'Đã giao' : '')}"
                aria-label="Chọn mục ${order.id}"></td>
            <td>#${order.id}</td>
            <td>
              <strong>
                <c:out value="${order.user.fullName}" default="Khách hàng"/>
              </strong>
              <small class="d-block muted">
                <c:out value="${order.user.email}"/>
              </small>
            </td>
            <td><c:out value="${empty order.phone ? order.user.phone : order.phone}" default="—"/></td>
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
              <ui:adminOrderStatus value="${order.status}"/>
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
    <p class="muted">${hasFilters ? 'Không tìm thấy đơn hàng phù hợp.' : 'Chưa có đơn hàng nào.'}</p>
  </c:if>
</section>
<jsp:include page="/WEB-INF/views/shared/admin/footer.jsp" />
