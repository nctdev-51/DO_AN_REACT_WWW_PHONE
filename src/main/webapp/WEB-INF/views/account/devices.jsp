<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<jsp:include page="/WEB-INF/views/shared/store/header.jsp" />
<ui:breadcrumb sectionLabel="Tài khoản" sectionHref="/account" currentLabel="Thiết bị của tôi"/>
<jsp:include page="/WEB-INF/views/shared/store/account-nav.jsp" />
<div class="section-heading">
  <div>
    <span class="eyebrow">HẬU MÃI &amp; BẢO HÀNH CHÍNH HÃNG</span>
    <h1>Thiết bị của tôi</h1>
    <p>Theo dõi thời hạn bảo hành, hạn đổi trả và lịch sử hỗ trợ cho từng sản phẩm bạn đã nhận từ PTV Mobile.</p>
  </div>
  <a class="btn btn-outline-dark" href="${pageContext.request.contextPath}/account/support">Danh sách yêu cầu hỗ trợ →</a>
</div>

<c:choose>
  <c:when test="${empty devices}">
    <div class="panel empty-state">
      <h2>Bạn chưa có thiết bị nào được giao thành công</h2>
      <p>Sau khi đơn hàng chuyển sang trạng thái Đã giao và thanh toán hoàn tất, thiết bị cùng thời hạn bảo hành sẽ hiển thị tại đây.</p>
      <a class="btn btn-brand" href="${pageContext.request.contextPath}/orders">Xem đơn hàng của tôi</a>
    </div>
  </c:when>
  <c:otherwise>
    <div class="device-grid">
      <c:forEach var="d" items="${devices}">
        <article class="panel device-card">
          <div class="device-card-head">
            <img src="${pageContext.request.contextPath}${d.image}" alt="<c:out value='${d.productName}'/>" loading="lazy">
            <div>
              <span class="eyebrow">ĐƠN HÀNG #${d.orderId} · SỐ LƯỢNG: ${d.quantity}</span>
              <h2 class="m-0"><a href="${pageContext.request.contextPath}/products/${d.productId}"><c:out value="${d.productName}"/></a></h2>
              <p class="muted small mb-1"><c:out value="${d.variantLabel}"/></p>
              <p class="small mb-0">Ngày nhận máy: <strong><c:out value="${fn:replace(d.deliveredAt, 'T', ' ')}"/></strong></p>
            </div>
          </div>

          <div class="device-warranty-meta">
            <div>
              <span class="small muted d-block">Bảo hành chính hãng (${d.warrantyMonths} tháng)</span>
              <c:choose>
                <c:when test="${d.warrantyActive}">
                  <span class="status-badge">Còn bảo hành · ${d.warrantyRemainingDays} ngày (đến <c:out value="${d.warrantyExpiresAt}"/>)</span>
                </c:when>
                <c:otherwise>
                  <span class="status-badge">Đã hết hạn bảo hành (<c:out value="${d.warrantyExpiresAt}"/>)</span>
                </c:otherwise>
              </c:choose>
            </div>
            <div>
              <span class="small muted d-block">Hỗ trợ đổi trả</span>
              <span class="small">${d.returnEligible ? 'Trong thời hạn xem xét đổi/trả' : 'Đã qua hạn đổi trả tiêu chuẩn'}</span>
            </div>
          </div>

          <c:if test="${not empty d.tickets}">
            <div class="device-ticket-list">
              <strong class="small d-block mb-1">Yêu cầu hỗ trợ liên quan:</strong>
              <c:forEach var="t" items="${d.tickets}">
                <div class="small muted">#${t.id} · <c:out value="${t.typeLabel}"/> — <strong><c:out value="${t.statusLabel}"/></strong></div>
              </c:forEach>
            </div>
          </c:if>

          <div class="d-flex flex-wrap gap-2 mt-3">
            <a class="btn btn-brand btn-sm" href="${pageContext.request.contextPath}/orders/${d.orderId}#support-request">Gửi yêu cầu bảo hành / đổi trả</a>
            <a class="btn btn-outline-dark btn-sm" href="${pageContext.request.contextPath}/products/${d.productId}#reviews">Đánh giá sản phẩm</a>
            <a class="btn btn-light btn-sm" href="${pageContext.request.contextPath}/orders/${d.orderId}">Xem đơn #${d.orderId}</a>
          </div>
        </article>
      </c:forEach>
    </div>
  </c:otherwise>
</c:choose>
<jsp:include page="/WEB-INF/views/shared/store/footer.jsp" />

