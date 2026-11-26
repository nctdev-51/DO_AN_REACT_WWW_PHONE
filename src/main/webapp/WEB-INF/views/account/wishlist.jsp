<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<jsp:include page="/WEB-INF/views/shared/store/header.jsp" />
<jsp:include page="/WEB-INF/views/shared/store/account-nav.jsp" />
<div class="section-heading">
  <div>
    <span class="eyebrow">
      ĐỂ DÀNH CHO LẦN MUA TỚI
    </span>
    <h1>
      Sản phẩm yêu thích
    </h1>
    <p>
      Giá và tồn kho được cập nhật theo cửa hàng.
    </p>
  </div>
</div>
<div class="product-grid">
  <c:forEach var="entry" items="${entries}">
    <section>
      <c:choose>
        <c:when test="${entry.product.status == 1}">
          <ui:productCard product="${entry.product}"/>
        </c:when>
        <c:otherwise>
          <div class="panel">
            <h2>
              <c:out value="${entry.product.name}"/>
            </h2>
            <p class="muted">
              Sản phẩm đã ngừng kinh doanh.
            </p>
          </div>
        </c:otherwise>
      </c:choose>
      <form class="mt-2" action="${pageContext.request.contextPath}/account/wishlist/remove" method="post">
        <ui:csrf/>
        <input type="hidden" name="productId" value="${entry.product.id}">
        <button class="btn btn-sm btn-outline-danger w-100">
          Bỏ khỏi yêu thích
        </button>
      </form>
    </section>
  </c:forEach>
</div>
<c:if test="${empty entries}">
  <div class="panel empty-state">
    <h2>
      Chưa có sản phẩm yêu thích
    </h2>
    <p>
      Lưu những điện thoại bạn quan tâm để xem lại sau.
    </p>
    <a class="btn btn-brand" href="${pageContext.request.contextPath}/products">
      Khám phá sản phẩm
    </a>
  </div>
</c:if>
<jsp:include page="/WEB-INF/views/shared/store/footer.jsp" />
