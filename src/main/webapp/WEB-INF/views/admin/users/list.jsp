<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<jsp:include page="/WEB-INF/views/shared/admin/header.jsp" />
<section class="panel">
  <div class="table-responsive">
    <table class="table align-middle">
      <thead>
        <tr>
          <th>Người dùng</th>
          <th>Số điện thoại</th>
          <th>Số đơn</th>
          <th>Vai trò</th>
          <th>Trạng thái</th>
          <th>Thao tác</th>
        </tr>
      </thead>
      <tbody>
        <c:forEach var="user" items="${users}">
          <tr>
            <td>
              <strong>
                <c:out value="${user.fullName}"/>
              </strong>
              <small class="d-block muted">
                <c:out value="${user.email}"/>
              </small>
            </td>
            <td>
              <c:out value="${user.phone}"/>
            </td>
            <td>${user.orderCount}</td>
            <td>${user.admin ? 'Quản trị' : 'Khách hàng'}</td>
            <td>${user.status == 1 ? 'Hoạt động' : 'Đã khóa'}</td>
            <td>
              <div class="table-actions">
                <a class="btn btn-light btn-sm" href="${pageContext.request.contextPath}/admin/users/${user.id}/edit">Sửa</a>
                <c:if test="${not user.admin and user.orderCount == 0}">
                  <form action="${pageContext.request.contextPath}/admin/users/${user.id}/delete" method="post" data-confirm="Xóa người dùng này?">
                    <ui:csrf/>
                    <button class="btn btn-outline-danger btn-sm">Xóa</button>
                  </form>
                </c:if>
              </div>
            </td>
          </tr>
        </c:forEach>
      </tbody>
    </table>
  </div>
</section>
<jsp:include page="/WEB-INF/views/shared/admin/footer.jsp" />
