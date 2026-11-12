<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
</main>
<footer class="store-footer">
  <div class="container footer-inner">
    <div>
      <a class="brand" href="${pageContext.request.contextPath}/">PTV <span>Mobile</span>
      </a>
      <p>Điện thoại, đúng với nhu cầu của bạn.</p>
    </div>
    <nav aria-label="Mua sắm"><h2>Khám phá cửa hàng</h2>
      <a href="${pageContext.request.contextPath}/products">Tất cả điện thoại</a>
      <a href="${pageContext.request.contextPath}/offers">Ưu đãi & mã giảm giá</a>
      <a href="${pageContext.request.contextPath}/compare">So sánh sản phẩm</a>
      <a href="${pageContext.request.contextPath}/account/wishlist">Sản phẩm yêu thích</a>
    </nav>
    <nav aria-label="Hỗ trợ mua hàng"><h2>Hỗ trợ mua hàng</h2>
      <a href="${pageContext.request.contextPath}/orders">Tra cứu & hủy đơn hàng</a>
      <a href="${pageContext.request.contextPath}/account/support">Yêu cầu đổi trả, bảo hành</a>
      <a href="${pageContext.request.contextPath}/account/addresses">Quản lý địa chỉ nhận hàng</a>
      <p>Gửi yêu cầu trong tài khoản để theo dõi tiến trình xử lý.</p>
    </nav>
    <nav aria-label="Chính sách"><h2>Thông tin cần biết</h2><a href="${pageContext.request.contextPath}/policies/shipping">Giao hàng & phí vận chuyển</a><a href="${pageContext.request.contextPath}/policies/returns">Chính sách đổi trả</a><a href="${pageContext.request.contextPath}/policies/warranty">Bảo hành</a><a href="${pageContext.request.contextPath}/policies/payment">Thanh toán</a><a href="${pageContext.request.contextPath}/policies/faq">Câu hỏi thường gặp</a><a href="${pageContext.request.contextPath}/policies/contact">Liên hệ cửa hàng</a></nav><nav aria-label="Tài khoản"><h2>Tài khoản của bạn</h2>
      <a href="${pageContext.request.contextPath}/account">Thông tin cá nhân</a>
      <a href="${pageContext.request.contextPath}/register">Đăng ký thành viên</a>
      <a href="${pageContext.request.contextPath}/forgot-password">Lấy lại mật khẩu</a>
      <a href="${pageContext.request.contextPath}/resend-verification">Xác minh email</a>
    </nav>
  </div>
  <div class="container footer-bottom"><span>PTV Mobile</span><span>Xem kỹ thông số. Chọn đúng phiên bản.</span><a href="#main-content">Lên đầu trang ↑</a></div>
</footer>
<nav class="mobile-bottom-nav" aria-label="Điều hướng nhanh trên di động">
  <a href="${pageContext.request.contextPath}/"><ui:icon name="home"/><span>Trang chủ</span></a>
  <a href="${pageContext.request.contextPath}/products"><ui:icon name="phone"/><span>Sản phẩm</span></a>
  <a href="${pageContext.request.contextPath}/compare">
    <span class="mobile-nav-icon"><ui:icon name="compare"/><c:if test="${not empty sessionScope.comparisonIds and sessionScope.comparisonIds.size() > 0}"><em class="mobile-nav-badge">${sessionScope.comparisonIds.size()}</em></c:if></span>
    <span>So sánh</span>
  </a>
  <a href="${pageContext.request.contextPath}/cart">
    <span class="mobile-nav-icon"><ui:icon name="cart"/><c:if test="${not empty sessionScope.cart and sessionScope.cart.totalQuantity > 0}"><em class="mobile-nav-badge">${sessionScope.cart.totalQuantity}</em></c:if></span>
    <span>Giỏ hàng</span>
  </a>
  <c:choose>
    <c:when test="${pageContext.request.userPrincipal != null}">
      <a href="${pageContext.request.contextPath}/account"><ui:icon name="user"/><span>Tài khoản</span></a>
    </c:when>
    <c:otherwise>
      <a href="${pageContext.request.contextPath}/login"><ui:icon name="user"/><span>Đăng nhập</span></a>
    </c:otherwise>
  </c:choose>
</nav>
<script src="${pageContext.request.contextPath}/vendor/bootstrap/bootstrap.bundle.min.js" defer>
</script>
<script src="${pageContext.request.contextPath}/js/store.js" defer>
</script>
<script src="${pageContext.request.contextPath}/js/catalog-cards.js" defer></script>
<script src="${pageContext.request.contextPath}/js/catalog-search.js" defer></script>
<script src="${pageContext.request.contextPath}/js/motion.js" defer></script>
<script src="${pageContext.request.contextPath}/js/retail.js" defer></script>
<script src="${pageContext.request.contextPath}/js/addresses.js" defer></script>
</body>
</html>
