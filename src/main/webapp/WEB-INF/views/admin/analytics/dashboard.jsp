<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<jsp:include page="/WEB-INF/views/shared/admin/header.jsp"/>

<section class="stat-grid">
  <article class="panel">
    <span>Lượt xem 7 ngày</span>
    <strong>${summary.views7Days}</strong>
  </article>
  <article class="panel">
    <span>Lượt xem 30 ngày</span>
    <strong>${summary.views30Days}</strong>
  </article>
  <article class="panel">
    <span>Tỷ lệ chuyển đổi</span>
    <strong><fmt:formatNumber value="${summary.conversionRate}" maxFractionDigits="2"/>%</strong>
  </article>
  <article class="panel">
    <span>Người dùng mới 30 ngày</span>
    <strong>${newUsers}</strong>
  </article>
  <article class="panel">
    <span>Lượt hỏi chatbot 30 ngày</span>
    <strong>${summary.chatbotMessages}</strong>
  </article>
</section>

<section class="panel mb-4">
  <div class="section-heading">
    <h2>Sản phẩm được xem nhiều nhất</h2>
    <a href="${pageContext.request.contextPath}/admin/analytics/export">Xuất CSV</a>
  </div>
  <div class="table-responsive">
    <table class="table">
      <thead><tr><th>Mã sản phẩm</th><th>Lượt tương tác</th></tr></thead>
      <tbody>
        <c:forEach var="product" items="${summary.topProducts}">
          <tr><td>#${product.productId}</td><td>${product.views}</td></tr>
        </c:forEach>
        <c:if test="${empty summary.topProducts}">
          <tr><td colspan="2" class="muted">Chưa có dữ liệu.</td></tr>
        </c:if>
      </tbody>
    </table>
  </div>
</section>

<section class="panel">
  <h2>Từ khóa tìm kiếm phổ biến</h2>
  <table class="table">
    <thead><tr><th>Từ khóa</th><th>Số lần</th></tr></thead>
    <tbody>
      <c:forEach var="keyword" items="${summary.topKeywords}">
        <tr><td><c:out value="${keyword.keyword}"/></td><td>${keyword.count}</td></tr>
      </c:forEach>
      <c:if test="${empty summary.topKeywords}">
        <tr><td colspan="2" class="muted">Chưa có dữ liệu.</td></tr>
      </c:if>
    </tbody>
  </table>
</section>

<jsp:include page="/WEB-INF/views/shared/admin/footer.jsp"/>
