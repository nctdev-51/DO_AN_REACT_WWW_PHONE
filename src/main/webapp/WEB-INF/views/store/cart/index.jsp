<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<jsp:include page="/WEB-INF/views/shared/store/header.jsp" />
<div class="page-heading">
  <div>
    <span class="eyebrow">MUA SẮM CỦA BẠN</span>
    <h1>Giỏ hàng <small>(${cart.totalQuantity})</small>
    </h1>
  </div>
  <a href="${pageContext.request.contextPath}/products">Tiếp tục mua sắm →</a>
</div>
<c:choose>
  <c:when test="${empty cart.itemList}">
    <div class="empty-state">
      <h2>Giỏ hàng đang trống</h2>
      <p>Thêm những sản phẩm bạn yêu thích để bắt đầu.</p>
      <a class="btn btn-brand" href="${pageContext.request.contextPath}/products">Khám phá sản phẩm</a>
    </div>
  </c:when>
  <c:otherwise>
    <div class="checkout-layout">
      <section class="panel">
        <c:forEach var="item" items="${cart.itemList}">
          <article class="cart-line">
            <img src="${pageContext.request.contextPath}${item.image}" alt="<c:out value='${item.productName}'/>">
            <div class="cart-line-info">
              <a href="${pageContext.request.contextPath}/products/${item.productId}">
                <strong>
                  <c:out value="${item.productName}"/>
                </strong>
              </a>
              <p class="muted">
                <c:out value="${item.variantLabel}"/>
              </p>
              <ui:money value="${item.unitPrice}"/>
              <form action="${pageContext.request.contextPath}/cart/update" method="post" class="d-flex gap-2 mt-2">
                <ui:csrf/>
                <input type="hidden" name="variantId" value="${item.variantId}">
                <input class="form-control quantity-input" type="number" name="quantity" value="${item.quantity}" min="0" max="99" aria-label="Số lượng">
                <button class="btn btn-light">Cập nhật</button>
              </form>
            </div>
            <div class="text-end">
              <strong>
                <ui:money value="${item.subTotal}"/>
              </strong>
              <form action="${pageContext.request.contextPath}/cart/remove/${item.variantId}" method="post" class="mt-3">
                <ui:csrf/>
                <button class="btn btn-link text-danger">Xóa</button>
              </form>
            </div>
          </article>
        </c:forEach>
        <form action="${pageContext.request.contextPath}/cart/clear" method="post" data-confirm="Xóa tất cả sản phẩm khỏi giỏ?">
          <ui:csrf/>
          <button class="btn btn-outline-secondary mt-3">Xóa tất cả</button>
        </form>
      </section>
      <aside class="panel order-summary">
        <h2>Tóm tắt đơn hàng</h2>
        <p>${cart.totalQuantity} sản phẩm</p>
        <div class="summary-total">
          <span>Tổng tiền</span>
          <ui:money value="${cart.totalAmount}"/>
        </div>
        <a class="btn btn-brand w-100 mt-3" href="${pageContext.request.contextPath}/checkout">Tiến hành đặt hàng</a>
        <p class="muted small mt-3">Giá và tồn kho được kiểm tra lại khi xác nhận đơn.</p>
      </aside>
    </div>
  </c:otherwise>
</c:choose>
<jsp:include page="/WEB-INF/views/shared/store/footer.jsp" />
