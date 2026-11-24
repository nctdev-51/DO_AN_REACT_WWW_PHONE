<%@ tag pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<%@ attribute name="products" required="true" type="java.util.List" %>
<%@ attribute name="variants" required="true" type="java.util.Map" %>
  <section class="collection-section">
    <div class="section-heading">
      <div>
        <span class="eyebrow">NGÂN SÁCH DƯỚI 10 TRIỆU</span>
        <h2>Vừa đủ cho mỗi ngày.</h2>
      </div>
      <a href="${pageContext.request.contextPath}/products?maxPrice=10000000&amp;inStock=true">Xem tất cả →</a>
    </div>
    <div class="product-rail-wrapper">
      <button type="button" class="rail-nav-btn rail-prev" aria-label="Cuộn sang trái" disabled>
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round"><polyline points="15 18 9 12 15 6"></polyline></svg>
      </button>
      <div class="product-rail" tabindex="0" aria-label="Điện thoại dưới 10 triệu, cuộn ngang để xem thêm">
        <c:forEach var="p" items="${products}">
          <ui:productCard product="${p}" selectedVariant="${variants[p.id]}"/>
        </c:forEach>
      </div>
      <button type="button" class="rail-nav-btn rail-next" aria-label="Cuộn sang phải">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round"><polyline points="9 18 15 12 9 6"></polyline></svg>
      </button>
    </div>
  </section>
