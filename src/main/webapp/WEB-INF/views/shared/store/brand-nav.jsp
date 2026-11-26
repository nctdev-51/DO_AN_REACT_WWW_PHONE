<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<nav class="brand-nav" aria-label="Chọn hãng điện thoại">
  <a href="${pageContext.request.contextPath}/products" class="${empty brand ? 'is-active' : ''}" aria-current="${empty brand ? 'page' : 'false'}">Tất cả hãng</a>
  <c:forEach items="${brands}" var="brandItem">
    <c:url var="brandLink" value="/products"><c:param name="kind" value="${kind}"/><c:param name="brand" value="${brandItem}"/></c:url>
    <a href="${brandLink}" class="brand-wordmark ${brand == brandItem ? 'is-active' : ''}" data-brand="<c:out value='${brandItem}'/>" aria-current="${brand == brandItem ? 'page' : 'false'}"><c:out value="${brandItem == 'Apple' ? 'iPhone' : brandItem}"/></a>
  </c:forEach>
</nav>
