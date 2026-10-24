<%@ tag pageEncoding="UTF-8" %>
<%@ attribute name="name" required="true" type="java.lang.String" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<svg class="icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false">
  <c:choose>
    <c:when test="${name == 'search'}"><circle cx="10.5" cy="10.5" r="6.5"/><path d="m16 16 4 4"/></c:when>
    <c:when test="${name == 'cart'}"><path d="M3 4h2l3 12h11l2-8H6M9 20h.01M18 20h.01"/></c:when>
    <c:when test="${name == 'compare'}"><path d="M7 3v18M17 3v18M3 8h8M13 16h8"/><circle cx="7" cy="8" r="2"/><circle cx="17" cy="16" r="2"/></c:when>
    <c:when test="${name == 'user'}"><circle cx="12" cy="8" r="4"/><path d="M4 21v-2a8 8 0 0 1 16 0v2"/></c:when>
    <c:when test="${name == 'box'}"><path d="m12 3 9 5-9 5-9-5 9-5ZM3 8v9l9 5 9-5V8M12 13v9M8 5l9 5"/></c:when>
    <c:when test="${name == 'heart'}"><path d="M20.8 4.6a5.5 5.5 0 0 0-7.8 0L12 5.7l-1.1-1.1a5.5 5.5 0 0 0-7.8 7.8L12 21l8.8-8.6a5.5 5.5 0 0 0 0-7.8Z"/></c:when>
    <c:when test="${name == 'arrow'}"><path d="M4 12h16m-6-6 6 6-6 6"/></c:when>
    <c:when test="${name == 'check'}"><path d="m5 12 4 4L19 6"/></c:when>
    <c:when test="${name == 'phone'}"><rect x="6" y="2" width="12" height="20" rx="3"/><path d="M10 18h4"/></c:when>
    <c:otherwise><rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/></c:otherwise>
  </c:choose>
</svg>
