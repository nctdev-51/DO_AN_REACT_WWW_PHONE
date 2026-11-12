<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<jsp:include page="/WEB-INF/views/shared/store/header.jsp" />
<ui:breadcrumb sectionLabel="Giỏ hàng" sectionHref="/cart" currentLabel="Thanh toán"/>
<div class="page-heading">
  <div>
    <span class="eyebrow">HOÀN TẤT MUA SẮM</span>
    <h1>Thông tin đặt hàng</h1>
  </div>
</div>

<ol class="checkout-stepper" aria-label="Các bước đặt hàng">
  <li class="is-active"><span>1</span> Thông tin nhận hàng</li>
  <li class="is-active"><span>2</span> Giao hàng &amp; phí</li>
  <li class="is-active"><span>3</span> Ưu đãi &amp; thanh toán</li>
  <li><span>4</span> Xác nhận đơn</li>
</ol>

<div class="checkout-layout">
  <section class="panel">
    <div class="d-flex justify-content-between align-items-center flex-wrap gap-2 mb-3">
      <h2 class="m-0">1. Người nhận hàng &amp; Địa chỉ</h2>
      <button type="button" class="btn btn-outline-dark btn-sm" data-bs-toggle="modal" data-bs-target="#newAddressModal">+ Thêm địa chỉ mới</button>
    </div>
    <c:if test="${not empty addresses}">
      <label for="saved-address">Chọn nhanh địa chỉ đã lưu</label>
      <select id="saved-address" class="form-select mb-4">
        <option value="">Nhập địa chỉ khác</option>
        <c:forEach var="address" items="${addresses}">
          <option value="${address.id}" data-recipient="<c:out value='${address.recipientName}'/>" data-phone="<c:out value='${address.phone}'/>" data-address="<c:out value='${address.line}'/>" data-province-code="${address.provinceCode}" data-ward-code="${address.wardCode}">
            <c:out value="${address.fullLine}"/> ${address.defaultAddress ? '(Mặc định)' : ''}
          </option>
        </c:forEach>
      </select>
    </c:if>

    <form:form id="checkout-form" action="${pageContext.request.contextPath}/checkout/place-order" modelAttribute="checkoutForm" method="post">
      <c:if test="${not empty checkoutToken}">
        <input type="hidden" name="checkoutToken" value="<c:out value='${checkoutToken}'/>">
      </c:if>
      <form:errors path="*" element="div" cssClass="alert alert-danger"/>
      <div class="field">
        <label for="recipientName">Họ tên người nhận</label>
        <form:input path="recipientName" id="recipientName" cssClass="form-control" required="required" maxlength="100"/>
      </div>
      <div class="field">
        <label for="phone">Số điện thoại</label>
        <form:input path="phone" id="phone" cssClass="form-control" required="required" pattern="0[0-9]{9}" maxlength="10"/>
      </div>
      <div class="field">
        <label for="shippingAddress">Số nhà, tên đường / địa chỉ chi tiết</label>
        <form:textarea path="shippingAddress" id="shippingAddress" cssClass="form-control" rows="3" required="required" maxlength="255"/>
      </div>
      <ui:addressPicker id="checkout-address" provinceCode="${checkoutForm.provinceCode}" wardCode="${checkoutForm.wardCode}" province="${checkoutForm.province}" ward="${checkoutForm.ward}"/>

      <h3 class="mt-4">2. Giao hàng &amp; cước vận chuyển</h3>
      <p class="muted small">
        Giao tiêu chuẩn dự kiến ${experience.deliveryMinDays}–${experience.deliveryMaxDays} ngày. Miễn phí giao hàng cho đơn từ <ui:money value="${storePolicy.freeShippingThreshold}"/> (nếu thấp hơn, phí chuẩn là <ui:money value="${storePolicy.shippingFee}"/>). <a href="${pageContext.request.contextPath}/policies/shipping">Chính sách giao hàng</a>
      </p>
      <c:if test="${ghnAvailable}">
        <button type="submit" class="btn btn-outline-dark mb-3" formaction="${pageContext.request.contextPath}/checkout/delivery-quote">Kiểm tra khả năng giao qua GHN</button>
      </c:if>
      <c:if test="${not empty deliveryEstimate}">
        <div class="alert alert-info">
          <strong>${deliveryEstimate.environment == 'STAGING' ? 'GHN thử nghiệm' : 'GHN'}</strong>
          <p class="mb-1">Dự kiến: <c:out value="${deliveryEstimate.expectedLabel}"/>. Cước tham khảo: <ui:money value="${deliveryEstimate.fee}"/> với khối lượng ước tính 500g mỗi sản phẩm.</p>
          <p class="mb-0 small">Phí bạn thanh toán vẫn theo chính sách cửa hàng trong tổng đơn bên cạnh. Nhân viên kiểm tra kiện hàng và xác nhận khi gửi.</p>
        </div>
      </c:if>

      <h3 class="mt-4">3. Mã giảm giá, điểm thưởng &amp; Thanh toán</h3>
      <div class="field">
        <label for="promotionCode">Mã giảm giá</label>
        <div class="d-flex gap-2">
          <form:input path="promotionCode" id="promotionCode" cssClass="form-control" maxlength="40" placeholder="Nhập mã giảm giá"/>
          <button class="btn btn-outline-dark" type="submit" formaction="${pageContext.request.contextPath}/checkout/quote" formnovalidate>Áp dụng</button>
        </div>
        <a class="small" href="${pageContext.request.contextPath}/offers" target="_blank" rel="noopener">Xem mã ưu đãi đang có →</a>
      </div>
      <c:if test="${experience.loyaltyEnabled}">
        <div class="field">
          <label for="redeemPoints">Dùng điểm thưởng (số dư: ${pointsBalance} điểm)</label>
          <div class="d-flex gap-2">
            <form:input path="redeemPoints" id="redeemPoints" cssClass="form-control" type="number" min="0" max="1000000" step="1"/>
            <button class="btn btn-outline-dark" type="submit" formaction="${pageContext.request.contextPath}/checkout/quote" formnovalidate>Kiểm tra điểm</button>
          </div>
          <small class="muted">1 điểm = <ui:money value="${experience.pointValue}"/>, giảm tối đa ${experience.redeemPercent}% tiền hàng sau mã. <a href="${pageContext.request.contextPath}/account/points">Xem sổ điểm</a></small>
        </div>
      </c:if>
      <label class="payment-choice">
        <form:radiobutton path="paymentMethod" value="COD"/> Thanh toán khi nhận hàng (COD)
      </label>
      <c:if test="${vnpayAvailable}">
        <label class="payment-choice">
          <form:radiobutton path="paymentMethod" value="VNPAY"/> Thanh toán trực tuyến VNPay <span class="muted small d-block">Đơn hàng giữ tồn kho trong 15 phút để bạn hoàn tất thanh toán.</span>
        </label>
      </c:if>
      <button class="btn btn-brand w-100 mt-4 btn-lg">Xác nhận đặt hàng</button>
    </form:form>
  </section>

  <aside class="panel order-summary">
    <h2>Đơn hàng của bạn (${cart.totalQuantity} sản phẩm)</h2>
    <c:forEach var="item" items="${cart.itemList}">
      <div class="summary-line">
        <span>
          <c:out value="${item.productName}"/>
          <small><c:out value="${item.variantLabel}"/> × ${item.quantity}</small>
        </span>
        <ui:money value="${item.subTotal}"/>
      </div>
    </c:forEach>
    <div class="summary-line"><span>Tiền sản phẩm</span><ui:money value="${quote.subtotal}"/></div>
    <div class="summary-line"><span>Giảm giá <c:out value="${quote.promotion.code}"/></span><span>− <ui:money value="${quote.discount}"/></span></div>
    <div class="summary-line"><span>Phí giao hàng</span><ui:money value="${quote.shippingFee}"/></div>
    <div class="summary-line"><span>Đổi ${checkoutForm.redeemPoints} điểm</span><span>− <ui:money value="${pointsDiscount}"/></span></div>
    <div class="summary-total">
      <span>Tổng thanh toán</span>
      <ui:money value="${payableTotal}"/>
    </div>
    <p class="muted small mt-3 mb-0">
      <c:choose>
        <c:when test="${quote.shippingFee == 0}">Đơn hàng của bạn đã đủ điều kiện miễn phí vận chuyển tiêu chuẩn.</c:when>
        <c:otherwise>Mua thêm để đạt <ui:money value="${storePolicy.freeShippingThreshold}"/> sẽ được miễn phí giao hàng tiêu chuẩn.</c:otherwise>
      </c:choose>
    </p>
  </aside>
</div>

<div class="modal fade" id="newAddressModal" tabindex="-1" aria-labelledby="newAddressModalLabel" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered">
    <div class="modal-content">
      <div class="modal-header">
        <h2 class="modal-title fs-5 m-0" id="newAddressModalLabel">Thêm địa chỉ nhận hàng mới</h2>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Đóng"></button>
      </div>
      <form id="checkout-address-modal-form" action="${pageContext.request.contextPath}/checkout/addresses" method="post" data-checkout-address-form>
        <div class="modal-body">
          <ui:csrf/>
          <div class="alert alert-danger" data-modal-address-error hidden></div>
          <div class="field">
            <label for="modal-recipientName">Họ tên người nhận</label>
            <input id="modal-recipientName" name="recipientName" class="form-control" required maxlength="100" value="<c:out value='${checkoutForm.recipientName}'/>">
          </div>
          <div class="field">
            <label for="modal-phone">Số điện thoại</label>
            <input id="modal-phone" name="phone" class="form-control" required pattern="0[0-9]{9}" maxlength="10" value="<c:out value='${checkoutForm.phone}'/>">
          </div>
          <div class="field">
            <label for="modal-line">Số nhà, tên đường</label>
            <input id="modal-line" name="line" class="form-control" required maxlength="255">
          </div>
          <ui:addressPicker id="modal-address" provinceCode="" wardCode="" province="" ward=""/>
          <label class="d-flex align-items-center gap-2 mt-2">
            <input type="checkbox" name="defaultAddress" value="true"> Đặt làm địa chỉ mặc định
          </label>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-light" data-bs-dismiss="modal">Hủy</button>
          <button type="submit" class="btn btn-brand">Lưu &amp; chọn địa chỉ này</button>
        </div>
      </form>
    </div>
  </div>
</div>
<jsp:include page="/WEB-INF/views/shared/store/footer.jsp" />
