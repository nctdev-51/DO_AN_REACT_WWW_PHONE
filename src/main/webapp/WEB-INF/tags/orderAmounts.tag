<%@ tag pageEncoding="UTF-8" %>
<%@ attribute name="order" required="true" type="iuh.fit.www_phone_ver1.entity.Order" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<div class="summary-line">
  <span>
    Tiền sản phẩm
  </span>
  <ui:money value="${order.subtotal}"/>
</div>
<div class="summary-line">
  <span>
    Giảm giá
    <c:out value="${order.promotionCode}"/>
  </span>
  <span>
    −
    <ui:money value="${order.discountAmount}"/>
  </span>
</div>
<div class="summary-line">
  <span>
    Phí giao hàng
  </span>
  <ui:money value="${order.shippingFee}"/>
</div>
<c:if test="${order.pointsSpent gt 0}"><div class="summary-line"><span>Đổi ${order.pointsSpent} điểm</span><span>− <ui:money value="${order.pointsDiscount}"/></span></div></c:if><div class="summary-total">
  <strong>
    Thanh toán
  </strong>
  <ui:money value="${order.totalAmount}"/>
</div>
