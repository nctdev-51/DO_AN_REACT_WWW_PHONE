<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<jsp:include page="/WEB-INF/views/shared/store/header.jsp" />
<div class="section-heading">
  <div>
    <span class="eyebrow">
      ƯU ĐÃI TẠI PTV MOBILE
    </span>
    <h1>
      Ưu đãi dành cho lựa chọn của bạn.
    </h1>
    <p>
      Mỗi tài khoản được dùng một lần cho mỗi mã trên đơn chưa hủy. Nhập mã tại bước đặt hàng.
    </p>
  </div>
</div>
<div class="coupon-grid">
  <c:forEach var="promotion" items="${promotions}">
    <article class="panel coupon-card">
      <span class="coupon-code">
        <c:out value="${promotion.code}"/>
      </span>
      <h2>
        <c:out value="${promotion.description}"/>
      </h2>
      <p>
        Giảm
        <c:choose>
          <c:when test="${promotion.discountType == 'PERCENT'}">
            ${promotion.amount}%
          </c:when>
          <c:otherwise>
            <ui:money value="${promotion.amount}"/>
          </c:otherwise>
        </c:choose>
        <c:if test="${promotion.maximumDiscount != null and (promotion.discountType == 'PERCENT' or promotion.maximumDiscount lt promotion.amount)}">
          , tối đa
          <ui:money value="${promotion.maximumDiscount}"/>
        </c:if>
      </p>
      <c:if test="${promotion.minimumSubtotal gt 0}"><p>
        Giá trị sản phẩm áp dụng từ
        <ui:money value="${promotion.minimumSubtotal}"/>
        .
      </p>
      </c:if><p class="muted">
        Hết hạn:
        <c:out value="${promotion.endsAtLabel}"/>
        · Còn ${remaining[promotion.id]} lượt
      </p>
      <p class="muted small"><c:out value="${promotion.scopeLabel}"/>. Giá trị tối thiểu tính trên sản phẩm đủ điều kiện.</p><c:if test="${not empty requiredProducts[promotion.id]}"><p class="small mb-2">Khi mua cùng:</p><div class="offer-products"><c:forEach var="p" items="${requiredProducts[promotion.id]}"><a href="${pageContext.request.contextPath}/products/${p.id}"><img src="${pageContext.request.contextPath}${p.image}" alt="" loading="lazy"><span><c:out value="${p.name}"/> →</span></a></c:forEach></div></c:if><c:if test="${not empty eligibleProducts[promotion.id]}"><p class="small mb-2">Sản phẩm nhận ưu đãi:</p><div class="offer-products"><c:forEach var="p" items="${eligibleProducts[promotion.id]}"><a href="${pageContext.request.contextPath}/products/${p.id}"><img src="${pageContext.request.contextPath}${p.image}" alt="" loading="lazy"><span><c:out value="${p.name}"/> →</span></a></c:forEach></div><p class="small muted">Thêm sản phẩm nhận ưu đãi vào giỏ, rồi áp dụng mã.</p></c:if><div class="quick-links"><button class="btn btn-outline-dark" type="button" data-copy-code="<c:out value='${promotion.code}'/>">Sao chép mã</button><form method="post" action="${pageContext.request.contextPath}/offers/apply"><ui:csrf/><input type="hidden" name="code" value="<c:out value='${promotion.code}'/>"><button class="btn btn-brand" ${remaining[promotion.id] == 0 ? 'disabled' : ''}>Dùng mã này</button></form></div><p data-copy-status role="status" aria-live="polite"></p>
    </article>
  </c:forEach>
</div>
<c:if test="${empty promotions}">
  <div class="panel empty-state">
    <h2>
      Chưa có chương trình đang áp dụng
    </h2>
    <p>
      Hãy quay lại để xem ưu đãi mới.
    </p>
  </div>
</c:if>
<jsp:include page="/WEB-INF/views/shared/store/footer.jsp" />
