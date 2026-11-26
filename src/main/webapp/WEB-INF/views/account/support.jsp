<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<jsp:include page="/WEB-INF/views/shared/store/header.jsp" />
<jsp:include page="/WEB-INF/views/shared/store/account-nav.jsp" />
<div class="section-heading">
  <div>
    <span class="eyebrow">
      HỖ TRỢ SAU MUA
    </span>
    <h1>
      Đổi trả & bảo hành
    </h1>
    <p>
      Mở chi tiết đơn hàng đã giao để gửi yêu cầu. Cửa hàng kiểm tra thiết bị và phản hồi phương án xử lý.
    </p>
  </div>
  <a class="btn btn-outline-dark" href="${pageContext.request.contextPath}/orders">
    Đơn hàng của tôi
  </a>
</div>
<c:forEach var="request" items="${requests}">
  <article class="panel mb-3">
    <div class="section-heading">
      <h2>
        Yêu cầu #${request.id} ·
        <c:out value="${request.typeLabel}"/>
      </h2>
      <span class="status-badge">
        <c:out value="${request.statusLabel}"/>
      </span>
    </div>
    <p>
      <a href="${pageContext.request.contextPath}/orders/${request.orderDetail.order.id}">
        Đơn #${request.orderDetail.order.id}
      </a>
      ·
      <c:out value="${request.orderDetail.displayName}"/>
      × ${request.quantity}
    </p>
    <p class="muted">
      Gửi lúc
      <c:out value="${request.createdAt}"/>
    </p>
    <p class="description">
      <c:out value="${request.reason}"/>
    </p>
    <c:if test="${not empty request.response}">
      <div class="alert alert-info">
        <strong>
          Phản hồi từ cửa hàng
        </strong>
        <p class="description mb-0">
          <c:out value="${request.response}"/>
        </p>
      </div>
    </c:if>
    <c:if test="${request.status == 'REQUESTED'}">
      <form action="${pageContext.request.contextPath}/account/support/${request.id}/cancel" method="post" data-confirm="Hủy yêu cầu hỗ trợ này?">
        <ui:csrf/>
        <button class="btn btn-outline-danger">
          Hủy yêu cầu
        </button>
      </form>
    </c:if>
  </article>
</c:forEach>
<c:if test="${empty requests}">
  <div class="panel empty-state">
    <h2>
      Chưa có yêu cầu hỗ trợ
    </h2>
    <a class="btn btn-brand" href="${pageContext.request.contextPath}/orders">
      Xem đơn đã mua
    </a>
  </div>
</c:if>
<jsp:include page="/WEB-INF/views/shared/store/footer.jsp" />
