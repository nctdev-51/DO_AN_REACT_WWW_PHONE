<%@ tag pageEncoding="UTF-8" %>
<%@ attribute name="path" required="true" type="java.lang.String" %>
<%@ attribute name="cssClass" required="false" type="java.lang.String" %>
<%@ attribute name="alt" required="false" type="java.lang.String" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:url var="placeholder" value="/images/products/placeholder.svg"/>
<c:url var="source" value="${empty path ? '/images/products/placeholder.svg' : path}"/>
<img class="<c:out value='${cssClass}'/>" src="<c:out value='${source}'/>"
     alt="<c:out value='${alt}'/>" loading="lazy" data-image-fallback="<c:out value='${placeholder}'/>">
