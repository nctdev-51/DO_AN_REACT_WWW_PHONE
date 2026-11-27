<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<jsp:include page="/WEB-INF/views/shared/store/header.jsp" />
<div class="home-page">
  <c:set var="budgetFirst" value="${not empty budgetProducts and (homeInterest == 'GAMING' or homeInterest == 'BUDGET')}"/>
  <section class="store-hero-overlay" data-reveal>
    <div class="store-hero-bg">
      <img src="https://getjar.com/_next/image?url=https%3A%2F%2Fwww.platformocho.io%2Fassets%2F51d48807-070c-48a0-9eb1-82a0287cf8c6&w=1920&q=75" alt="PTV Mobile Hero Banner" fetchpriority="high" onerror="this.style.display='none'">
    </div>
    <div class="store-hero-content">
      <span class="eyebrow">CỬA HÀNG PTV MOBILE</span>
      <c:set var="rawHeadline" value="${empty experience.headline ? 'Chọn công nghệ. Theo cách của bạn.' : experience.headline}"/>
      <c:set var="cleanHeadline" value="${fn:replace(rawHeadline, '.n', '.')}" />
      <h1><c:out value="${cleanHeadline}"/></h1>
      <p><c:out value="${empty experience.introduction ? 'Khám phá điện thoại, so sánh cấu hình và tìm lựa chọn vừa ngân sách.' : experience.introduction}"/></p>
      <div class="quick-links">
        <a href="${pageContext.request.contextPath}/choose">Chọn theo ngân sách →</a>
        <a href="${pageContext.request.contextPath}/offers">Ưu đãi đang có →</a>
        <a href="${pageContext.request.contextPath}/recently-viewed">Đã xem gần đây →</a>
      </div>
    </div>
  </section>
  <jsp:include page="/WEB-INF/views/shared/store/brand-nav.jsp" />
  <c:forEach var="b" items="${heroBanners}"><ui:campaignBanner banner="${b}"/></c:forEach>
  <c:if test="${empty heroBanners}"><c:choose><c:when test="${not empty heroProduct}">
    <ui:showcase product="${heroProduct}" primary="${true}" subtitle="Khám phá từng góc nhìn."/>
  </c:when><c:otherwise>
    <section class="empty-state" data-reveal><h1>Điện thoại. Lựa chọn của bạn.</h1><p>Khám phá các mẫu máy đang có tại PTV Mobile.</p><a class="btn btn-brand" href="${pageContext.request.contextPath}/products">Xem điện thoại</a></section>
  </c:otherwise></c:choose></c:if>
  <c:if test="${empty heroBanners and not empty showcaseProduct}"><ui:showcase product="${showcaseProduct}" subtitle="Một góc nhìn khác. Một lựa chọn mới."/></c:if>
  <div class="campaign-tiles"><c:forEach var="b" items="${tileBanners}"><ui:campaignBanner banner="${b}"/></c:forEach></div>
  <c:if test="${budgetFirst}"><ui:homeBudget products="${budgetProducts}" variants="${budgetVariants}"/></c:if>
  <c:if test="${not empty personalizedProducts}">
    <section class="collection-section mb-4" data-reveal>
      <div class="section-heading">
        <div>
          <span class="eyebrow" style="color: var(--ana-primary, #2563eb);">✨ DÀNH RIÊNG CHO BẠN</span>
          <h2>Gợi ý dựa trên <c:out value="${personalizedAnchorName}"/></h2>
        </div>
        <a href="${pageContext.request.contextPath}/recently-viewed">Đã xem gần đây →</a>
      </div>
      <div class="product-grid">
        <c:forEach var="product" items="${personalizedProducts}">
          <ui:productCard product="${product}"/>
        </c:forEach>
      </div>
    </section>
  </c:if>
  <c:if test="${not empty interestProducts}">
    <section class="collection-section mb-4" data-reveal>
      <div class="section-heading">
        <div>
          <span class="eyebrow" style="color: var(--ana-primary, #2563eb);">✨ DÀNH CHO BẠN</span>
          <h2>${homeInterest == 'CAMERA' ? 'Điện thoại chụp ảnh đỉnh cao' : 'Điện thoại chơi game mượt mà'}</h2>
        </div>
        <a href="${pageContext.request.contextPath}/products?purpose=${homeInterest}">Khám phá →</a>
      </div>
      <div class="product-grid">
        <c:forEach var="product" items="${interestProducts}">
          <ui:productCard product="${product}"/>
        </c:forEach>
      </div>
    </section>
  </c:if>

  <div class="section-heading"><div><span class="eyebrow">KHÁM PHÁ PTV MOBILE</span><h2>Tìm chiếc điện thoại<br>hợp với bạn.</h2></div><a href="${pageContext.request.contextPath}/products">Xem tất cả sản phẩm →</a></div>
  <div class="product-grid"><c:forEach var="product" items="${featuredProducts}"><ui:productCard product="${product}"/></c:forEach></div>
  <c:if test="${not budgetFirst}"><ui:homeBudget products="${budgetProducts}" variants="${budgetVariants}"/></c:if>
  <section class="journey-grid"><a href="${pageContext.request.contextPath}/choose"><span class="eyebrow">TÌM LỰA CHỌN PHÙ HỢP</span><h2>Bắt đầu từ ngân sách.</h2><span>Chọn hãng và mức chi →</span></a><a href="${pageContext.request.contextPath}/account/consultations?type=TRADE_IN"><span class="eyebrow">THU CŨ ĐỔI MỚI</span><h2>Sẵn sàng cho máy mới?</h2><span>Gửi yêu cầu kiểm tra máy cũ →</span></a><a href="${pageContext.request.contextPath}/account/points"><span class="eyebrow">THÀNH VIÊN PTV</span><h2>Mỗi lần mua, thêm điểm.</h2><span>Xem điểm và quy tắc →</span></a></section>
  <section class="benefits" aria-label="Mua sắm tại PTV Mobile">
    <div><ui:icon name="phone"/><strong>Chọn đúng phiên bản</strong><span>Màu sắc, bộ nhớ và giá rõ ràng.</span></div>
    <div><ui:icon name="compare"/><strong>Xem khác biệt trước khi chọn</strong><span>So sánh tối đa ba mẫu máy.</span></div>
    <div><ui:icon name="box"/><strong>Theo dõi từ lúc đặt hàng</strong><span>Xem trạng thái đơn trong tài khoản.</span></div>
  </section>
  <section class="discovery-grid" aria-label="Tiện ích mua sắm">
    <article class="discovery-card" data-reveal><span class="eyebrow">SO SÁNH SẢN PHẨM</span><h2>Đặt những lựa chọn cạnh nhau.</h2><p>Giá, màn hình, camera và bộ nhớ. Xem điểm khác biệt trên cùng một bảng.</p><a class="btn btn-brand" href="${pageContext.request.contextPath}/compare">So sánh điện thoại <ui:icon name="arrow"/></a></article>
    <article class="discovery-card discovery-card-inverse" data-reveal><span class="eyebrow">TÀI KHOẢN PTV MOBILE</span><h2>Đơn hàng trong tầm tay.</h2><p>Theo dõi giao hàng, quản lý địa chỉ và gửi yêu cầu bảo hành tại một nơi.</p><a class="btn" href="${pageContext.request.contextPath}/orders">Xem đơn hàng <ui:icon name="arrow"/></a></article>
  </section>
</div>
<jsp:include page="/WEB-INF/views/shared/store/footer.jsp" />
