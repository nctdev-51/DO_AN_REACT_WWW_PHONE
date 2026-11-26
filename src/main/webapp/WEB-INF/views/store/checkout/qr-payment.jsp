<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<jsp:include page="/WEB-INF/views/shared/store/header.jsp" />
<section class="panel" style="max-width:760px;margin:2rem auto;">
  <div class="page-heading">
    <div>
      <span class="eyebrow">THANH TOÁN QR</span>
      <h1>Đơn hàng #${order.id}</h1>
    </div>
    <strong class="price"><ui:money value="${order.totalAmount}"/></strong>
  </div>

  <div class="d-flex flex-wrap gap-2 mb-4" role="tablist">
    <c:forEach var="item" items="${platforms}">
      <a class="btn ${item.name() == platform.name() ? 'btn-brand' : 'btn-outline-dark'}"
         href="${pageContext.request.contextPath}/payments/qr/${order.id}?platform=${item.name()}">${item.displayName}</a>
    </c:forEach>
  </div>

  <c:choose>
    <c:when test="${paymentExpired}">
      <div class="alert alert-danger">Đơn hàng đã hết thời hạn thanh toán 5 phút.</div>
    </c:when>
    <c:otherwise>
      <div class="text-center">
        <div style="border-left:6px solid ${platform.color};padding:1rem;">
          <h2>${platform.displayName}</h2>
          <img src="${qrImage}" alt="Mã QR thanh toán ${platform.displayName}" width="320" height="320" />
          <p><strong>${platform.accountLabel}</strong></p>
          <p>Số tài khoản/ví: <strong>${platform.accountNumber}</strong></p>
          <p>Chủ tài khoản: <strong>${platform.accountOwner}</strong></p>
          <p>Nội dung: <strong>PTVMOBILE #${order.id}</strong></p>
        </div>
        <p class="mt-3">Thời gian còn lại:
          <strong id="qr-countdown" data-deadline="${order.paymentExpiresAt}">05:00</strong>
        </p>
        <form action="${pageContext.request.contextPath}/payments/qr/${order.id}/simulate" method="post" id="simulate-qr-form">
          <ui:csrf/>
          <input type="hidden" name="platform" value="${platform.name()}">
          <input type="hidden" name="ref" value="${order.paymentReference}">
          <input type="hidden" name="token" value="${order.qrToken}">
          <button type="submit" class="btn btn-brand">Giả lập quét QR thành công</button>
        </form>
      </div>
    </c:otherwise>
  </c:choose>
</section>
<script>
(function () {
  var deadline = new Date('${order.paymentExpiresAt}');
  var countdown = document.getElementById('qr-countdown');
  function tick() {
    var remaining = Math.max(0, Math.floor((deadline - new Date()) / 1000));
    if (countdown) countdown.textContent =
      String(Math.floor(remaining / 60)).padStart(2, '0') + ':' +
      String(remaining % 60).padStart(2, '0');
  }
  tick();
  setInterval(tick, 1000);

  var form = document.getElementById('simulate-qr-form');
  if (form) form.addEventListener('submit', function (event) {
    event.preventDefault();
    fetch(form.action, {
      method: 'POST',
      headers: {'Content-Type': 'application/x-www-form-urlencoded'},
      body: new URLSearchParams(new FormData(form))
    }).then(function (response) { return response.json(); })
      .then(function (result) {
        if (result.success) window.location.href = '${pageContext.request.contextPath}/checkout/success/${order.id}';
        else alert(result.message);
      });
  });

  setInterval(function () {
    fetch('${pageContext.request.contextPath}/api/payments/qr/${order.id}/status')
      .then(function (response) { return response.json(); })
      .then(function (status) {
        if (status.paymentStatus === 'PAID')
          window.location.href = '${pageContext.request.contextPath}/checkout/success/${order.id}';
      });
  }, 2000);
})();
</script>
<jsp:include page="/WEB-INF/views/shared/store/footer.jsp" />
