<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:if test="${not empty successMsg or not empty message}">
  <div class="alert alert-success" role="status">
    <c:out value="${empty successMsg ? message : successMsg}" />
  </div>
</c:if>
<c:if test="${not empty errorMsg or not empty error}">
  <div class="alert alert-danger" role="alert">
    <c:out value="${empty errorMsg ? error : errorMsg}" />
  </div>
</c:if>
