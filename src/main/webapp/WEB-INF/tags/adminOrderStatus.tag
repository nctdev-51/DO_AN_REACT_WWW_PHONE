<%@ tag pageEncoding="UTF-8" %>
<%@ attribute name="value" required="true" type="java.lang.String" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:choose>
  <c:when test="${value == 'Đã giao'}"><span class="status-badge status-success">Đã giao</span></c:when>
  <c:when test="${value == 'Đã hủy'}"><span class="status-badge status-danger">Đã hủy</span></c:when>
  <c:when test="${value == 'Chờ xử lý'}"><span class="status-badge status-warning">Chờ xử lý</span></c:when>
  <c:when test="${value == 'Đang giao'}"><span class="status-badge status-info">Đang giao</span></c:when>
  <c:otherwise><span class="status-badge"><c:out value="${value}" default="Chưa xác định"/></span></c:otherwise>
</c:choose>
