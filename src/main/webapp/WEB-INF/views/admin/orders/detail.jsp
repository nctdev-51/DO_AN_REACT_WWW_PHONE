<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<jsp:include page="/WEB-INF/views/shared/admin/header.jsp" />
<a href="${pageContext.request.contextPath}/admin/orders">← Danh sách đơn hàng</a>
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
<section class="panel mt-4">
  <h2>Cập nhật trạng thái</h2>
  <form action="${pageContext.request.contextPath}/admin/orders/${order.id}/status" method="post" class="d-flex gap-3">
    <ui:csrf/>
    <select class="form-select" name="status">
      <c:forEach var="status" items="${statuses}">
        <option value="<c:out value='${status}'/>" ${status == order.status ? 'selected' : ''}>
          <c:out value="${status}"/>
        </option>
      </c:forEach>
    </select>
    <button class="btn btn-brand text-nowrap">Cập nhật</button>
  </form>
  <p class="muted small mt-3">Đơn chờ xử lý có thể chuyển sang đang giao hoặc đã hủy. Đơn đang giao chỉ chuyển sang đã giao. Hủy đơn đã thanh toán sẽ trả tồn và điểm đã dùng; quản trị phải xử lý khoản hoàn và đối soát riêng.</p>
</section>
<section class="panel mt-4"><h2>Chi tiết thanh toán</h2><ui:orderAmounts order="${order}"/></section>
<section class="panel mt-4"><h2>Giao nhận</h2><c:if test="${not empty order.carrierStatus}"><p><strong><c:out value="${order.carrierStatusLabel}"/></strong> · ${order.ghnEnvironment == 'STAGING' ? 'Vận đơn thử nghiệm' : 'Vận đơn GHN'}</p></c:if><c:if test="${not empty order.province or not empty order.ward}"><p><c:out value="${order.ward}"/> · <c:out value="${order.province}"/></p></c:if><c:if test="${not empty order.estimatedDeliveryFrom}"><p>Dự kiến: <c:out value="${order.estimatedDeliveryLabel}"/>. Cửa hàng sẽ xác nhận khi tiếp nhận.</p></c:if><c:choose><c:when test="${not empty order.trackingCode}"><p>Đơn vị: <strong><c:out value="${order.carrier}"/></strong> · Mã vận đơn: <strong><c:out value="${order.trackingCode}"/></strong></p><p class="muted small">Thông tin do cửa hàng cập nhật. Dùng mã này tại trang tra cứu chính thức của đơn vị vận chuyển.</p></c:when><c:otherwise><p class="muted">Chưa có mã vận đơn. Tiến trình xử lý được cập nhật bên dưới.</p></c:otherwise></c:choose></section><c:if test="${not empty refunds}"><section class="panel mt-4"><h2>Theo dõi hoàn tiền</h2><c:forEach var="r" items="${refunds}"><article class="review"><strong>Khoản hoàn #${r.id}: <ui:money value="${r.amount}"/></strong><span class="status-badge">${r.status == 'PENDING' ? 'Chờ đối soát' : r.status == 'COMPLETED' ? 'Đã xác nhận hoàn' : 'Đã từ chối'}</span><p><c:out value="${r.reason}"/></p><c:if test="${not empty r.settlementReference}"><p>Tham chiếu / phản hồi: <c:out value="${r.settlementReference}"/></p></c:if><c:if test="${r.status == 'PENDING'}"><form method="post" action="${pageContext.request.contextPath}/admin/refunds/${r.id}"><ui:csrf/><div class="field"><label for="settle-${r.id}">Kết quả xử lý</label><select id="settle-${r.id}" name="status" class="form-select"><option value="COMPLETED">Đã đối soát giao dịch hoàn tiền thực tế</option><option value="REJECTED">Từ chối khoản hoàn</option></select></div><div class="field"><label for="reference-${r.id}">Mã giao dịch hoàn tiền / lý do từ chối</label><input id="reference-${r.id}" name="reference" class="form-control" required maxlength="100"></div><button class="btn btn-brand">Lưu kết quả đối soát #${r.id}</button></form></c:if></article></c:forEach><p class="muted small">Thời gian tiền về phụ thuộc phương thức và đơn vị xử lý thanh toán. Điểm được điều chỉnh khi hoàn tiền được xác nhận.</p></section></c:if><ui:orderTimeline order="${order}"/>
<c:if test="${order.status == 'Chờ xử lý' or order.status == 'Đang giao'}"><section class="panel mt-4"><h2>Cập nhật vận đơn</h2><form method="post" action="${pageContext.request.contextPath}/admin/orders/${order.id}/delivery"><ui:csrf/><div class="field"><label for="carrier">Đơn vị vận chuyển</label><input id="carrier" name="carrier" class="form-control" value="<c:out value='${order.carrier}'/>" maxlength="80" required></div><div class="field"><label for="tracking">Mã vận đơn</label><input id="tracking" name="trackingCode" class="form-control" value="<c:out value='${order.trackingCode}'/>" pattern="[A-Za-z0-9_-]{3,80}" maxlength="80" required></div><div class="field"><label for="delivery-note">Ghi chú tiến trình</label><input id="delivery-note" name="note" class="form-control" maxlength="500" required></div><button class="btn btn-brand">Lưu thông tin vận chuyển</button></form></section></c:if>
<c:if test="${order.paymentStatus == 'PAID' or order.paymentStatus == 'PARTIALLY_REFUNDED' or order.paymentStatus == 'REFUND_REQUIRED'}"><section class="panel mt-4"><h2>Ghi nhận khoản hoàn</h2><p>Kiểm tra thanh toán và phê duyệt đổi trả trước khi thực hiện hoàn tiền. Trang này lưu đối soát thủ công, không tự chuyển tiền.</p><form method="post" action="${pageContext.request.contextPath}/admin/orders/${order.id}/refunds"><ui:csrf/><div class="field"><label for="refund-amount">Số tiền hoàn (đồng)</label><input id="refund-amount" name="amount" type="number" min="1" max="${order.totalAmount}" step="1" required class="form-control"></div><div class="field"><label for="refund-reason">Lý do và yêu cầu hỗ trợ liên quan</label><input id="refund-reason" name="reason" maxlength="500" required class="form-control"></div><button class="btn btn-outline-dark">Tạo khoản hoàn chờ đối soát</button></form></section></c:if>
<section class="panel mt-4"><h2>Giao hàng qua GHN</h2><p>${ghnEnvironment == 'STAGING' ? 'Môi trường thử nghiệm — không giao hàng thật.' : 'Môi trường thực tế — thao tác tạo sẽ gửi vận đơn tới GHN.'}</p><c:choose><c:when test="${ghnAvailable}"><c:if test="${order.carrier == 'GHN' and not empty order.trackingCode}"><form method="post" action="${pageContext.request.contextPath}/admin/orders/${order.id}/ghn/sync"><ui:csrf/><button class="btn btn-brand">Đồng bộ trạng thái từ GHN</button></form></c:if><c:if test="${order.status == 'Chờ xử lý' and empty order.trackingCode}"><p>Nhập số đo kiện đã đóng gói. Giá trị khai bảo hiểm phải theo thỏa thuận thực tế; không tự coi toàn bộ giá điện thoại đã được bảo hiểm.</p><form method="post" action="${pageContext.request.contextPath}/admin/orders/${order.id}/ghn/create" data-confirm="Xác nhận gửi yêu cầu tạo vận đơn GHN theo môi trường đang cấu hình?"><ui:csrf/><div class="filter-grid"><div class="field"><label for="ghn-weight">Khối lượng kiện (g)</label><input id="ghn-weight" class="form-control" name="weight" type="number" min="1" max="19999" value="500" step="1" required></div><div class="field"><label for="ghn-length">Chiều dài (cm)</label><input id="ghn-length" class="form-control" name="length" type="number" min="1" max="200" value="25" step="1" required></div><div class="field"><label for="ghn-width">Chiều rộng (cm)</label><input id="ghn-width" class="form-control" name="width" type="number" min="1" max="200" value="20" step="1" required></div><div class="field"><label for="ghn-height">Chiều cao (cm)</label><input id="ghn-height" class="form-control" name="height" type="number" min="1" max="200" value="10" step="1" required></div><div class="field"><label for="ghn-insuranceValue">Giá trị khai bảo hiểm (đ)</label><input id="ghn-insuranceValue" class="form-control" name="insuranceValue" type="number" min="0" max="5000000" value="0" step="1" required></div></div><button class="btn btn-outline-dark">${order.carrierStatus == 'UNCONFIRMED' ? 'Kiểm tra / gửi lại cùng yêu cầu GHN' : 'Tạo vận đơn GHN'}</button><p class="muted small mt-3">Nếu lần trước chưa rõ kết quả, gửi lại giữ nguyên mã và thông tin kiện ban đầu để tránh trùng vận đơn. Muốn hủy: xử lý vận đơn trên GHN, đồng bộ xác nhận trạng thái hủy rồi hủy đơn cửa hàng.</p></form></c:if></c:when><c:otherwise><p class="muted">Chưa kết nối GHN. Có thể cập nhật vận đơn thủ công bên dưới; cấu hình dịch vụ theo hướng dẫn tích hợp để bật kết nối.</p></c:otherwise></c:choose></section>
<jsp:include page="/WEB-INF/views/shared/admin/footer.jsp" />
