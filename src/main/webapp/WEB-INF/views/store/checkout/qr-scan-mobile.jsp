<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<jsp:include page="/WEB-INF/views/shared/store/header.jsp" />
<section class="panel qr-scan-panel">
  <span class="eyebrow">${platform.displayName}</span>
  <h1>Mô phỏng xác nhận chuyển khoản</h1>
  <p>Đơn hàng: <strong>#${order.id}</strong></p>
  <p>Số tiền: <strong><ui:money value="${order.totalAmount}"/></strong></p>
  <p>Nội dung: <strong>PTVMOBILE #${order.id}</strong></p>
  <c:choose>
    <c:when test="${paymentExpired or order.status == 'Đã hủy'}">
      <div class="alert alert-danger">Mã thanh toán đã hết hạn hoặc đơn đã bị hủy.</div>
    </c:when>
    <c:otherwise>
      <button id="confirm-mobile" class="btn btn-brand w-100">Xác nhận chuyển khoản</button>
      <div id="mobile-result" class="alert alert-success mt-3" hidden>Thanh toán thành công.</div>
    </c:otherwise>
  </c:choose>
</section>
<script>
document.getElementById('confirm-mobile')?.addEventListener('click', function () {
  var body = new URLSearchParams({
    orderId: '${order.id}',
    ref: '${order.paymentReference}',
    token: '${order.qrToken}',
    platform: '${platform.name()}'
  });
  fetch('${pageContext.request.contextPath}/payments/qr/scan/confirm', {
    method: 'POST',
    headers: {'Content-Type': 'application/x-www-form-urlencoded'},
    body: body
  }).then(function (response) { return response.json(); })
    .then(function (result) {
      var box = document.getElementById('mobile-result');
      box.hidden = false;
      box.className = result.success ? 'alert alert-success mt-3' : 'alert alert-danger mt-3';
      box.textContent = result.message;
    });
});
</script>
<jsp:include page="/WEB-INF/views/shared/store/footer.jsp" />
