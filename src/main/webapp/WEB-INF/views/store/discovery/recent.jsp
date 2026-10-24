<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<jsp:include page="/WEB-INF/views/shared/store/header.jsp"/>
<div class="page-heading"><div><span class="eyebrow">TIẾP TỤC KHÁM PHÁ</span><h1>Sản phẩm đã xem.</h1><p>Tối đa 10 sản phẩm gần nhất trong phiên trình duyệt này.</p></div><a href="${pageContext.request.contextPath}/products">Xem tất cả →</a></div><div class="product-grid"><c:forEach var="p" items="${recentProducts}"><ui:productCard product="${p}"/></c:forEach></div><c:if test="${empty recentProducts}"><div class="empty-state"><h2>Bắt đầu từ một sản phẩm bạn thích</h2><p>Sản phẩm đã xem sẽ xuất hiện ở đây để bạn quay lại dễ hơn.</p><a class="btn btn-brand" href="${pageContext.request.contextPath}/products">Khám phá sản phẩm</a></div></c:if>
<jsp:include page="/WEB-INF/views/shared/store/footer.jsp"/>
