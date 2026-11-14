<%@ tag pageEncoding="UTF-8" %>
<%@ attribute name="sectionLabel" required="false" type="java.lang.String" %>
<%@ attribute name="sectionHref" required="false" type="java.lang.String" %>
<%@ attribute name="currentLabel" required="true" type="java.lang.String" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<nav class="breadcrumb-line" aria-label="Đường dẫn">
  <a href="${pageContext.request.contextPath}/">Trang chủ</a>
  <c:if test="${not empty sectionLabel}">
    <span aria-hidden="true">/</span>
    <c:choose>
      <c:when test="${not empty sectionHref}">
        <a href="${pageContext.request.contextPath}${sectionHref}"><c:out value="${sectionLabel}"/></a>
      </c:when>
      <c:otherwise>
        <span><c:out value="${sectionLabel}"/></span>
      </c:otherwise>
    </c:choose>
  </c:if>
  <span aria-hidden="true">/</span>
  <span aria-current="page"><c:out value="${currentLabel}"/></span>
</nav>

