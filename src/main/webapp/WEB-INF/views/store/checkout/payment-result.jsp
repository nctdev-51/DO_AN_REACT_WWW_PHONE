<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<jsp:include page="/WEB-INF/views/shared/store/header.jsp" />
<ui:breadcrumb sectionLabel="Đơn hàng của tôi" sectionHref="/orders" currentLabel="Kết quả thanh toán"/>
<section class="success-panel panel">
  <span class="eyebrow">THANH TOÁN VNPAY · ĐƠN HÀNG #${order.id}</span>
  <c:choose>
    <c:when test="${order.paymentStatus == 'PAID'}">
      <h1>Thanh toán thành công</h1>
      <p class="fs-4"><ui:paymentStatus value="${order.paymentStatus}"/></p>
      <p>Cửa hàng đã ghi nhận khoản thanh toán <strong><ui:money value="${order.totalAmount}"/></strong> (Mã giao dịch tham chiếu: <code><c:out value="${order.paymentReference}"/></code>) và gửi email xác nhận đơn hàng.</p>
    </c:when>
    <c:when test="${order.status == 'Chờ xử lý' and order.paymentMethod == 'VNPAY' and (order.paymentStatus == 'UNPAID' or order.paymentStatus == 'FAILED')}">
      <h1>${order.paymentStatus == 'FAILED' ? 'Thanh toán chưa hoàn tất' : 'Đang chờ xác nhận thanh toán'}</h1>
      <p class="fs-4"><ui:paymentStatus value="${order.paymentStatus}"/></p>
      <p>Đơn hàng <strong>#<c:out value="${order.id}"/></strong> tổng tiền <strong><ui:money value="${order.totalAmount}"/></strong> vẫn đang giữ tồn kho cho bạn. Mã giao dịch: <code><c:out value="${order.paymentReference}"/></code>.</p>
      <c:choose>
        <c:when test="${not paymentExpired}">
          <div class="alert alert-info mb-3">
            Thời gian giữ đơn còn lại: <strong data-countdown-deadline="${paymentDeadlineIso}">trong vòng 15 phút từ lúc đặt hàng</strong>. Bạn có thể thử thanh toán lại qua VNPay hoặc chuyển sang COD mà không cần đặt lại giỏ hàng.
          </div>
          <div class="d-flex flex-wrap gap-2 justify-content-center mb-3">
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
          <div class="alert alert-danger mb-3">
            Đã quá hạn 15 phút thanh toán trực tuyến. Hệ thống sẽ tự động hủy giữ tồn kho cho đơn hàng này.
          </div>
        </c:otherwise>
      </c:choose>
    </c:when>
    <c:otherwise>
      <h1>Kết quả thanh toán</h1>
      <p class="fs-4"><ui:paymentStatus value="${order.paymentStatus}"/></p>
      <p>Trạng thái đơn hàng hiện tại: <strong><c:out value="${order.status}"/></strong>.</p>
    </c:otherwise>
  </c:choose>
  <div class="d-flex flex-wrap gap-2 justify-content-center mt-3">
    <a class="btn btn-brand" href="${pageContext.request.contextPath}/orders/${order.id}">Xem chi tiết đơn hàng</a>
    <a class="btn btn-light" href="${pageContext.request.contextPath}/products">Tiếp tục mua sắm</a>
  </div>
</section>
<jsp:include page="/WEB-INF/views/shared/store/footer.jsp" />
