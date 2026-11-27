<%@ tag pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ attribute name="action" required="true" type="java.lang.String" %>
<%@ attribute name="label" required="true" type="java.lang.String" %>
<%@ attribute name="query" required="true" type="java.lang.String" %>
<c:url var="searchUrl" value="${action}"/>
<form method="get" action="${searchUrl}" class="d-flex flex-wrap align-items-end gap-2 mb-3" data-admin-name-search>
  <div>
    <label for="admin-name-query" class="form-label"><c:out value="${label}"/></label>
    <input id="admin-name-query" class="form-control" type="search" name="q"
           autocomplete="off" aria-controls="admin-name-results" value="<c:out value='${query}'/>">
  </div>
  <button type="submit" class="btn btn-brand">Tìm</button>
  <a class="btn btn-light ${empty query ? 'd-none' : ''}" href="${searchUrl}" data-search-clear>Xóa bộ lọc</a>
  <span class="small muted align-self-center" data-search-status role="status"></span>
</form>
