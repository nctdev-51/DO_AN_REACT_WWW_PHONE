<%@ tag pageEncoding="UTF-8" %>
<%@ attribute name="name" required="true" type="java.lang.String" %>
<%@ attribute name="label" required="true" type="java.lang.String" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:url var="removeFilter" value="/products"><c:forEach var="entry" items="${param}"><c:if test="${entry.key != name and entry.key != 'page'}"><c:param name="${entry.key}" value="${entry.value}"/></c:if></c:forEach></c:url>
<a class="filter-chip" href="<c:out value='${removeFilter}'/>" aria-label="Bỏ lọc: <c:out value='${label}'/>"><c:out value="${label}"/> <span aria-hidden="true">×</span></a>
