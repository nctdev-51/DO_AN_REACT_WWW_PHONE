<%@ tag pageEncoding="UTF-8" %>
<%@ attribute name="value" required="false" type="java.lang.String" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:choose>
  <c:when test="${value == 'PAID'}">
    <span class="status-badge status-success">Đã thanh toán</span>
  </c:when>
  <c:when test="${value == 'FAILED'}">
    <span class="status-badge status-danger">Thanh toán thất bại</span>
  </c:when>
  <c:when test="${value == 'REFUND_REQUIRED'}">
    <span class="status-badge status-danger">Cần xử lý hoàn tiền</span>
  </c:when>
  <c:when test="${value == 'PARTIALLY_REFUNDED'}"><span class="status-badge">Đã hoàn tiền một phần</span></c:when><c:when test="${value == 'REFUNDED'}"><span class="status-badge">Đã hoàn tiền</span></c:when>
  <c:otherwise>
    <span class="status-badge">Chưa thanh toán</span>
  </c:otherwise>
</c:choose>
