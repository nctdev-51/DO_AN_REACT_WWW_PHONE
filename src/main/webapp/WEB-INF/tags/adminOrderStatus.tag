<%@ tag pageEncoding="UTF-8" %>
<%@ attribute name="value" required="true" type="java.lang.String" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:choose>
  <c:when test="${value == 'Đã giao'}"><span class="status-badge status-success">Đã giao</span></c:when>
  <c:when test="${value == 'Đã hủy'}"><span class="status-badge status-danger">Đã hủy</span></c:when>
  <c:when test="${value == 'Chờ xử lý'}"><span class="status-badge status-warning">Chờ xử lý</span></c:when>
  <c:when test="${value == 'Đang giao'}"><span class="status-badge status-info">Đang giao</span></c:when>
  <c:when test="${value == 'Yêu cầu trả hàng'}"><span class="status-badge status-warning">Yêu cầu trả hàng</span></c:when>
  <c:when test="${value == 'Đã nhận hàng hoàn'}"><span class="status-badge status-info">Đã nhận hàng hoàn</span></c:when>
  <c:when test="${value == 'Từ chối trả hàng'}"><span class="status-badge status-danger">Từ chối trả hàng</span></c:when>
  <c:otherwise><span class="status-badge"><c:out value="${value}" default="Chưa xác định"/></span></c:otherwise>
</c:choose>
