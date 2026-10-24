<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<jsp:include page="/WEB-INF/views/shared/store/header.jsp" />
<div class="page-heading">
  <div>
    <span class="eyebrow">HOÀN TẤT MUA SẮM</span>
    <h1>Thông tin đặt hàng</h1>
  </div>
</div>
<div class="checkout-layout">
  <section class="panel">
    <h2>Người nhận hàng</h2>
    <c:if test="${not empty addresses}">
      <label for="saved-address">Địa chỉ đã lưu</label>
      <select id="saved-address" class="form-select mb-4">
        <option value="">Nhập địa chỉ khác</option>
        <c:forEach var="address" items="${addresses}">
          <option value="${address.id}" data-recipient="<c:out value='${address.recipientName}'/>" data-phone="<c:out value='${address.phone}'/>" data-address="<c:out value='${address.line}'/>" data-province-code="${address.provinceCode}" data-ward-code="${address.wardCode}">
            <c:out value="${address.fullLine}"/> ${address.defaultAddress ? '(Mặc định)' : ''}</option>
            </c:forEach>
          </select>
        </c:if>
        <form:form action="${pageContext.request.contextPath}/checkout/place-order" modelAttribute="checkoutForm" method="post">
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
          <p class="muted">Giao tiêu chuẩn dự kiến ${experience.deliveryMinDays}–${experience.deliveryMaxDays} ngày. Cửa hàng xác nhận khả năng giao tới địa chỉ của bạn khi tiếp nhận. <a href="${pageContext.request.contextPath}/policies/shipping">Chính sách giao hàng</a></p>
          <c:if test="${ghnAvailable}"><button type="submit" class="btn btn-outline-dark mb-3" formaction="${pageContext.request.contextPath}/checkout/delivery-quote">Kiểm tra khả năng giao qua GHN</button></c:if><c:if test="${not empty deliveryEstimate}"><div class="alert alert-info"><strong>${deliveryEstimate.environment == 'STAGING' ? 'GHN thử nghiệm' : 'GHN'}</strong><p>Dự kiến: <c:out value="${deliveryEstimate.expectedLabel}"/>. Cước tham khảo: <ui:money value="${deliveryEstimate.fee}"/> với khối lượng ước tính 500g mỗi sản phẩm.</p><p>Phí bạn thanh toán vẫn theo chính sách cửa hàng trong tổng đơn bên cạnh. Nhân viên kiểm tra kiện hàng và xác nhận khi gửi.</p></div></c:if>
          <h3>Thanh toán</h3>
          <div class="field"><label for="promotionCode">Mã giảm giá</label><div class="d-flex gap-2">
            <form:input path="promotionCode" id="promotionCode" cssClass="form-control" maxlength="40" placeholder="Nhập mã giảm giá"/>
            <button class="btn btn-outline-dark" type="submit" formaction="${pageContext.request.contextPath}/checkout/quote" formnovalidate>Áp dụng</button>
          </div><a href="${pageContext.request.contextPath}/offers" target="_blank" rel="noopener">Xem mã đang có</a></div>
          <c:if test="${experience.loyaltyEnabled}"><div class="field"><label for="redeemPoints">Dùng điểm thưởng (số dư: ${pointsBalance} điểm)</label><div class="d-flex gap-2"><form:input path="redeemPoints" id="redeemPoints" cssClass="form-control" type="number" min="0" max="1000000" step="1"/><button class="btn btn-outline-dark" formaction="${pageContext.request.contextPath}/checkout/quote" formnovalidate>Kiểm tra điểm</button></div><small>1 điểm = <ui:money value="${experience.pointValue}"/>, tối đa ${experience.redeemPercent}% tiền hàng sau mã. <a href="${pageContext.request.contextPath}/account/points">Xem sổ điểm</a></small></div></c:if>
          <label class="payment-choice">
            <form:radiobutton path="paymentMethod" value="COD"/> Thanh toán khi nhận hàng (COD)</label>
              <c:if test="${vnpayAvailable}">
                <label class="payment-choice">
                  <form:radiobutton path="paymentMethod" value="VNPAY"/> Thanh toán VNPay</label>
                  </c:if>
                  <button class="btn btn-brand w-100 mt-4">Xác nhận đặt hàng</button>
                </form:form>
              </section>
              <aside class="panel order-summary">
                <h2>Đơn hàng của bạn</h2>
                <c:forEach var="item" items="${cart.itemList}">
                  <div class="summary-line">
                    <span>
                      <c:out value="${item.productName}"/>
                      <small>
                        <c:out value="${item.variantLabel}"/> × ${item.quantity}</small>
                        </span>
                        <ui:money value="${item.subTotal}"/>
                      </div>
                    </c:forEach>
                    <div class="summary-line"><span>Tiền sản phẩm</span><ui:money value="${quote.subtotal}"/></div>
                    <div class="summary-line"><span>Giảm giá <c:out value="${quote.promotion.code}"/></span><span>− <ui:money value="${quote.discount}"/></span></div>
                    <div class="summary-line"><span>Phí giao hàng</span><ui:money value="${quote.shippingFee}"/></div>
                    <div class="summary-line"><span>Đổi ${checkoutForm.redeemPoints} điểm</span><span>− <ui:money value="${pointsDiscount}"/></span></div><div class="summary-total">
                      <span>Tổng tiền</span>
                      <ui:money value="${payableTotal}"/>
                    </div>
                  </aside>
                </div>
                <jsp:include page="/WEB-INF/views/shared/store/footer.jsp" />
