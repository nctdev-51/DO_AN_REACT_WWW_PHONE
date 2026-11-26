<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
</main>
<script src="${pageContext.request.contextPath}/vendor/bootstrap/bootstrap.bundle.min.js" defer>
</script>
<script src="${pageContext.request.contextPath}/vendor/chart/chart.umd.min.js" defer>
</script>
<script src="${pageContext.request.contextPath}/js/store.js" defer>
</script>
<script src="${pageContext.request.contextPath}/js/admin.js" defer>
</script>
<script src="${pageContext.request.contextPath}/js/motion.js" defer></script>
<script src="${pageContext.request.contextPath}/js/retail.js" defer></script>
<c:if test="${currentPage == 'analytics'}">
  <script src="${pageContext.request.contextPath}/js/admin-analytics.js" defer></script>
</c:if>
</body>
</html>
