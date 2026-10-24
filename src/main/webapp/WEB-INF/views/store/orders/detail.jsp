<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<jsp:include page="/WEB-INF/views/shared/store/header.jsp" />
<a href="${pageContext.request.contextPath}/orders">← Đơn hàng của tôi</a>
<div class="page-heading">
  <div>
    <span class="eyebrow">THEO DÕI ĐƠN HÀNG</span>
    <h1>Đơn hàng #${order.id}</h1>
    <p>
      <c:out value="${order.status}"/>
    </p>
  </div>
  <strong class="price">
    <ui:money value="${order.totalAmount}"/>
  </strong>
</div>
<div class="detail-sections">
  <section class="panel">
    <h2>Thông tin nhận hàng</h2>
    <p>
      <strong>
        <c:out value="${empty order.recipientName ? order.user.fullName : order.recipientName}"/>
      </strong>
    </p>
    <p>
      <c:out value="${order.phone}"/>
    </p>
    <p>
      <c:out value="${order.shippingAddress}"/>
    </p>
  </section>
  <section class="panel">
    <h2>Thanh toán</h2>
    <p>
      <c:out value="${order.paymentMethod}"/>
    </p>
    <ui:paymentStatus value="${order.paymentStatus}"/>
    <c:if test="${not empty order.cancellationReason}">
      <p class="mt-3">Lý do hủy: <c:out value="${order.cancellationReason}"/>
    </p>
  </c:if>
</section>
</div>
<section class="panel">
  <h2>Sản phẩm đã đặt</h2>
  <div class="table-responsive">
    <table class="table align-middle">
      <thead>
        <tr>
          <th>Sản phẩm</th>
          <th>Số lượng</th>
          <th>Đơn giá</th>
          <th>Thành tiền</th>
        </tr>
      </thead>
      <tbody>
        <c:forEach var="item" items="${order.orderDetails}">
          <tr>
            <td>
              <a href="${pageContext.request.contextPath}/products/${item.product.id}">
                <c:out value="${item.displayName}"/>
              </a>
              <small class="d-block muted">
                <c:out value="${item.variantLabel}"/>
              </small>
            </td>
            <td>${item.quantity}</td>
            <td>
              <ui:money value="${item.price}"/>
            </td>
            <td>
              <ui:money value="${item.subTotal}"/>
            </td>
          </tr>
        </c:forEach>
      </tbody>
    </table>
  </div>
</section>
<c:if test="${order.status == 'Chờ xử lý' and order.paymentStatus != 'PAID'}">
  <section class="panel mt-4">
    <h2>Hủy đơn hàng</h2>
    <form action="${pageContext.request.contextPath}/orders/${order.id}/cancel" method="post" data-confirm="Bạn muốn hủy đơn hàng này?">
      <ui:csrf/>
      <label for="cancel-reason">Lý do hủy</label>
      <input id="cancel-reason" class="form-control mb-3" name="reason" required maxlength="500" placeholder="Nhập lý do hủy đơn">
      <button class="btn btn-outline-danger">Hủy đơn hàng</button>
    </form>
  </section>
</c:if>
<section class="panel mt-4"><h2>Chi tiết thanh toán</h2><ui:orderAmounts order="${order}"/></section>
<section class="panel mt-4"><h2>Giao nhận</h2><c:if test="${not empty order.carrierStatus}"><p><strong><c:out value="${order.carrierStatusLabel}"/></strong> · ${order.ghnEnvironment == 'STAGING' ? 'Vận đơn thử nghiệm' : 'Vận đơn GHN'}</p></c:if><c:if test="${not empty order.province or not empty order.ward}"><p><c:out value="${order.ward}"/> · <c:out value="${order.province}"/></p></c:if><c:if test="${not empty order.estimatedDeliveryFrom}"><p>Dự kiến: <c:out value="${order.estimatedDeliveryLabel}"/>. Cửa hàng sẽ xác nhận khi tiếp nhận.</p></c:if><c:choose><c:when test="${not empty order.trackingCode}"><p>Đơn vị: <strong><c:out value="${order.carrier}"/></strong> · Mã vận đơn: <strong><c:out value="${order.trackingCode}"/></strong></p><p class="muted small">Thông tin do cửa hàng cập nhật. Dùng mã này tại trang tra cứu chính thức của đơn vị vận chuyển.</p></c:when><c:otherwise><p class="muted">Chưa có mã vận đơn. Tiến trình xử lý được cập nhật bên dưới.</p></c:otherwise></c:choose></section><c:if test="${not empty refunds}"><section class="panel mt-4"><h2>Theo dõi hoàn tiền</h2><c:forEach var="r" items="${refunds}"><article class="review"><strong>Khoản hoàn #${r.id}: <ui:money value="${r.amount}"/></strong><span class="status-badge">${r.status == 'PENDING' ? 'Chờ đối soát' : r.status == 'COMPLETED' ? 'Đã xác nhận hoàn' : 'Đã từ chối'}</span><p><c:out value="${r.reason}"/></p><c:if test="${not empty r.settlementReference}"><p>Tham chiếu / phản hồi: <c:out value="${r.settlementReference}"/></p></c:if></article></c:forEach><p class="muted small">Thời gian tiền về phụ thuộc phương thức và đơn vị xử lý thanh toán. Điểm được điều chỉnh khi hoàn tiền được xác nhận.</p></section></c:if><ui:orderTimeline order="${order}"/>
<c:if test="${order.status == 'Đã giao' and (order.paymentStatus == 'PAID' or order.paymentStatus == 'PARTIALLY_REFUNDED')}">
  <section class="panel mt-4"><h2>Yêu cầu đổi trả / bảo hành</h2><p>Cửa hàng xét duyệt dựa trên thời hạn và tình trạng thiết bị. Theo dõi phản hồi tại <a href="${pageContext.request.contextPath}/account/support">Đổi trả & bảo hành</a>.</p>
    <form action="${pageContext.request.contextPath}/orders/${order.id}/support" method="post"><ui:csrf/>
      <div class="field"><label for="support-detail">Sản phẩm cần hỗ trợ</label><select id="support-detail" name="detailId" class="form-select"><c:forEach var="item" items="${order.orderDetails}"><option value="${item.id}"><c:out value="${item.displayName}"/> / <c:out value="${item.variantLabel}"/></option></c:forEach></select></div>
      <div class="field"><label for="support-type">Loại yêu cầu</label><select id="support-type" name="type" class="form-select"><option value="WARRANTY">Bảo hành</option><option value="RETURN">Đổi / trả hàng</option></select></div>
      <div class="field"><label for="support-quantity">Số lượng</label><input id="support-quantity" type="number" name="quantity" value="1" min="1" max="99" class="form-control" required></div>
      <div class="field"><label for="support-reason">Mô tả vấn đề</label><textarea id="support-reason" name="reason" minlength="10" maxlength="2000" rows="3" class="form-control" required></textarea></div>
      <button class="btn btn-brand">Gửi yêu cầu</button>
    </form>
  </section>
</c:if>
<jsp:include page="/WEB-INF/views/shared/store/footer.jsp" />
