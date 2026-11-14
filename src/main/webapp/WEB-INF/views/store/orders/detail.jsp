<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<jsp:include page="/WEB-INF/views/shared/store/header.jsp" />
<ui:breadcrumb sectionLabel="Đơn hàng của tôi" sectionHref="/orders" currentLabel="Đơn hàng #${order.id}"/>
<a href="${pageContext.request.contextPath}/orders">← Đơn hàng của tôi</a>
<div class="page-heading">
  <div>
    <span class="eyebrow">THEO DÕI ĐƠN HÀNG</span>
    <h1>Đơn hàng #${order.id}</h1>
    <div class="d-flex flex-wrap gap-2 align-items-center mt-2">
      <span class="status-badge">Đơn hàng: <c:out value="${order.status}"/></span>
      <span class="status-badge">Thanh toán (<c:out value="${order.paymentMethod}"/>): <ui:paymentStatus value="${order.paymentStatus}"/></span>
    </div>
  </div>
  <strong class="price">
    <ui:money value="${order.totalAmount}"/>
  </strong>
</div>

<section class="panel mb-4" aria-label="Tiến trình đơn hàng">
  <h2 class="h5 mb-3">Tiến trình xử lý đơn hàng</h2>
  <c:choose>
    <c:when test="${milestoneStep == 0}">
      <div class="alert alert-danger mb-0">
        <strong>Đơn hàng đã hủy</strong>
        <c:if test="${not empty order.cancellationReason}">
          <p class="mb-0 mt-1">Lý do: <c:out value="${order.cancellationReason}"/></p>
        </c:if>
      </div>
    </c:when>
    <c:otherwise>
      <ol class="order-milestones">
        <li class="${milestoneStep >= 1 ? 'is-done' : ''}"><span>1</span> Đã đặt đơn</li>
        <li class="${milestoneStep >= 2 ? 'is-done' : ''}"><span>2</span> Đã xác nhận</li>
        <li class="${milestoneStep >= 3 ? 'is-done' : ''}"><span>3</span> Đang chuẩn bị / Vận đơn</li>
        <li class="${milestoneStep >= 4 ? 'is-done' : ''}"><span>4</span> Đang giao hàng</li>
        <li class="${milestoneStep >= 5 ? 'is-done' : ''}"><span>5</span> Đã giao thành công</li>
      </ol>
    </c:otherwise>
  </c:choose>
</section>

<c:if test="${order.status == 'Chờ xử lý' and order.paymentMethod == 'VNPAY' and (order.paymentStatus == 'UNPAID' or order.paymentStatus == 'FAILED')}">
  <section class="panel mb-4 payment-recovery-box">
    <h2>Hoàn tất thanh toán đơn hàng</h2>
    <c:choose>
      <c:when test="${not paymentExpired}">
        <p>Đơn hàng VNPay của bạn đang giữ tồn kho. Thời gian còn lại: <strong data-countdown-deadline="${paymentDeadlineIso}">15 phút kể từ lúc đặt</strong>.</p>
        <div class="d-flex flex-wrap gap-2">
          <c:if test="${vnpayAvailable}">
            <form action="${pageContext.request.contextPath}/orders/${order.id}/retry-vnpay" method="post">
              <ui:csrf/>
              <button class="btn btn-brand">Thanh toán lại qua VNPay</button>
            </form>
          </c:if>
          <form action="${pageContext.request.contextPath}/orders/${order.id}/switch-cod" method="post" data-confirm="Chuyển đơn hàng #${order.id} sang thanh toán khi nhận hàng (COD)?">
            <ui:csrf/>
            <button class="btn btn-outline-dark">Chuyển sang thanh toán khi nhận hàng (COD)</button>
          </form>
        </div>
      </c:when>
      <c:otherwise>
        <p class="text-danger mb-0">Đã hết thời hạn 15 phút thanh toán trực tuyến; hệ thống sẽ giải phóng tồn kho đơn hàng này.</p>
      </c:otherwise>
    </c:choose>
  </section>
</c:if>

<div class="detail-sections">
  <section class="panel">
    <h2>Thông tin nhận hàng</h2>
    <p><strong><c:out value="${empty order.recipientName ? order.user.fullName : order.recipientName}"/></strong></p>
    <p><c:out value="${order.phone}"/></p>
    <p class="mb-0"><c:out value="${order.fullShippingAddress}"/></p>
  </section>
  <section class="panel">
    <h2>Thanh toán &amp; Đối soát</h2>
    <p>Phương thức: <strong><c:out value="${order.paymentMethod}"/></strong></p>
    <p>Trạng thái: <ui:paymentStatus value="${order.paymentStatus}"/></p>
    <c:if test="${not empty order.paymentReference}">
      <p class="small muted">Mã tham chiếu thanh toán: <code><c:out value="${order.paymentReference}"/></code></p>
    </c:if>
    <c:if test="${not empty order.gatewayTransaction}">
      <p class="small muted mb-0">Mã giao dịch cổng: <code><c:out value="${order.gatewayTransaction}"/></code></p>
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
              <small class="d-block muted"><c:out value="${item.variantLabel}"/></small>
            </td>
            <td>${item.quantity}</td>
            <td><ui:money value="${item.price}"/></td>
            <td><ui:money value="${item.subTotal}"/></td>
          </tr>
        </c:forEach>
      </tbody>
    </table>
  </div>
</section>

<c:if test="${order.status == 'Chờ xử lý' and order.paymentStatus != 'PAID'}">
  <section class="panel mt-4">
    <h2>Hủy đơn hàng</h2>
    <p class="muted small">Bạn có thể tự hủy đơn khi đơn chưa thanh toán trực tuyến và chưa chuyển sang giao hàng. Tồn kho, mã giảm giá và điểm đã dùng (nếu có) sẽ được hoàn lại tự động.</p>
    <form action="${pageContext.request.contextPath}/orders/${order.id}/cancel" method="post" data-confirm="Bạn muốn hủy đơn hàng này?">
      <ui:csrf/>
      <label for="cancel-reason">Lý do hủy</label>
      <input id="cancel-reason" class="form-control mb-3" name="reason" required maxlength="500" placeholder="Nhập lý do hủy đơn">
      <button class="btn btn-outline-danger">Hủy đơn hàng</button>
    </form>
  </section>
</c:if>

<section class="panel mt-4">
  <h2>Chi tiết thanh toán</h2>
  <ui:orderAmounts order="${order}"/>
</section>

<section class="panel mt-4">
  <h2>Giao nhận &amp; Vận đơn</h2>
  <c:if test="${not empty order.carrierStatus}">
    <p>
      <strong><c:out value="${order.carrierStatusLabel}"/></strong> · ${order.ghnEnvironment == 'STAGING' ? 'Vận đơn GHN thử nghiệm' : 'Vận đơn GHN'}
      <c:if test="${not empty order.carrierUpdatedAt}">
        <span class="muted small"> (cập nhật: <c:out value="${fn:replace(order.carrierUpdatedAt, 'T', ' ')}"/>)</span>
      </c:if>
    </p>
  </c:if>
  <c:if test="${not empty order.province or not empty order.ward}">
    <p><c:out value="${order.ward}"/> · <c:out value="${order.province}"/></p>
  </c:if>
  <c:if test="${not empty order.estimatedDeliveryFrom}">
    <p>Thời gian giao dự kiến: <strong><c:out value="${order.estimatedDeliveryLabel}"/></strong>. Cửa hàng xác nhận lại khi giao vận.</p>
  </c:if>
  <c:choose>
    <c:when test="${not empty order.trackingCode}">
      <div class="d-flex flex-wrap align-items-center gap-2 mb-2">
        <span>Đơn vị: <strong><c:out value="${order.carrier}"/></strong> · Mã vận đơn: <strong id="tracking-code-text"><c:out value="${order.trackingCode}"/></strong></span>
        <button type="button" class="btn btn-light btn-sm" data-copy-tracking="<c:out value='${order.trackingCode}'/>">Sao chép mã</button>
        <c:if test="${order.carrier == 'GHN'}">
          <a class="btn btn-outline-dark btn-sm" href="https://donhang.ghn.vn/?order_code=<c:out value='${order.trackingCode}'/>" target="_blank" rel="noopener noreferrer">Tra cứu trên GHN ↗</a>
        </c:if>
      </div>
      <span class="small muted d-block mb-2" data-copy-tracking-status></span>
    </c:when>
    <c:otherwise>
      <p class="muted mb-0">Chưa có mã vận đơn. Tiến trình xử lý và tạo vận đơn được cập nhật ở lịch sử bên dưới.</p>
    </c:otherwise>
  </c:choose>
</section>

<c:if test="${not empty refunds}">
  <section class="panel mt-4">
    <h2>Theo dõi hoàn tiền</h2>
    <c:forEach var="r" items="${refunds}">
      <article class="review">
        <strong>Khoản hoàn #${r.id}: <ui:money value="${r.amount}"/></strong>
        <span class="status-badge">${r.status == 'PENDING' ? 'Chờ đối soát' : r.status == 'COMPLETED' ? 'Đã xác nhận hoàn' : 'Đã từ chối'}</span>
        <p><c:out value="${r.reason}"/></p>
        <c:if test="${not empty r.settlementReference}"><p>Tham chiếu / phản hồi: <c:out value="${r.settlementReference}"/></p></c:if>
      </article>
    </c:forEach>
    <p class="muted small">Thời gian tiền về phụ thuộc phương thức và đơn vị xử lý thanh toán. Điểm thưởng được đối soát khi hoàn tiền xác nhận.</p>
  </section>
</c:if>

<ui:orderTimeline order="${order}"/>

<c:if test="${order.status == 'Đã giao' and (order.paymentStatus == 'PAID' or order.paymentStatus == 'PARTIALLY_REFUNDED')}">
  <section class="panel mt-4" id="support-request">
    <h2>Yêu cầu đổi trả / bảo hành</h2>
    <p>Cửa hàng xét duyệt dựa trên thời hạn đổi trả và bảo hành chính hãng. Theo dõi phản hồi tại <a href="${pageContext.request.contextPath}/account/support">Đổi trả &amp; bảo hành</a> hoặc <a href="${pageContext.request.contextPath}/account/devices">Thiết bị của tôi</a>.</p>
    <form action="${pageContext.request.contextPath}/orders/${order.id}/support" method="post">
      <ui:csrf/>
      <div class="field"><label for="support-detail">Sản phẩm cần hỗ trợ</label><select id="support-detail" name="detailId" class="form-select"><c:forEach var="item" items="${order.orderDetails}"><option value="${item.id}"><c:out value="${item.displayName}"/> / <c:out value="${item.variantLabel}"/></option></c:forEach></select></div>
      <div class="field"><label for="support-type">Loại yêu cầu</label><select id="support-type" name="type" class="form-select"><option value="WARRANTY">Bảo hành</option><option value="RETURN">Đổi / trả hàng</option></select></div>
      <div class="field"><label for="support-quantity">Số lượng</label><input id="support-quantity" type="number" name="quantity" value="1" min="1" max="99" class="form-control" required></div>
      <div class="field"><label for="support-reason">Mô tả vấn đề</label><textarea id="support-reason" name="reason" minlength="10" maxlength="2000" rows="3" class="form-control" required></textarea></div>
      <button class="btn btn-brand">Gửi yêu cầu</button>
    </form>
  </section>
</c:if>

<section class="panel mt-4">
  <h2>Quy tắc hủy đơn, đổi trả &amp; Cần hỗ trợ?</h2>
  <ul class="small muted mb-3">
    <li>Khách có thể hủy đơn khi đơn ở trạng thái <strong>Chờ xử lý</strong> và <strong>chưa thanh toán VNPay</strong>. Nếu đã thanh toán qua VNPay, vui lòng liên hệ cửa hàng để hủy và tạo yêu cầu hoàn tiền đối soát.</li>
    <li>Yêu cầu đổi/trả hàng áp dụng sau khi đơn giao thành công trong thời hạn chính sách cửa hàng; yêu cầu bảo hành hỗ trợ theo thời hạn từng thiết bị.</li>
  </ul>
  <div class="d-flex flex-wrap gap-2">
    <a class="btn btn-light btn-sm" href="${pageContext.request.contextPath}/account/support">Lịch sử hỗ trợ &amp; bảo hành</a>
    <a class="btn btn-light btn-sm" href="${pageContext.request.contextPath}/account/devices">Xem thiết bị &amp; hạn bảo hành</a>
    <a class="btn btn-light btn-sm" href="${pageContext.request.contextPath}/policies/returns">Chính sách đổi trả</a>
    <a class="btn btn-light btn-sm" href="${pageContext.request.contextPath}/policies/contact">Liên hệ cửa hàng</a>
  </div>
</section>
<jsp:include page="/WEB-INF/views/shared/store/footer.jsp" />
