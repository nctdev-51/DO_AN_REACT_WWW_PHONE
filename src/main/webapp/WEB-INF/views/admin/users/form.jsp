<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<jsp:include page="/WEB-INF/views/shared/admin/header.jsp" />
<section class="panel form-panel">
  <h2>Sửa người dùng</h2>
  <p class="muted">
    <c:out value="${user.email}"/>
  </p>
  <form action="${pageContext.request.contextPath}/admin/users/${user.id}/edit" method="post">
    <ui:csrf/>
    <div class="field">
      <label>Họ tên<input class="form-control" name="fullName" value="<c:out value='${user.fullName}'/>" required maxlength="100">
      </label>
    </div>
    <div class="field">
      <label>Số điện thoại<input class="form-control" name="phone" value="<c:out value='${user.phone}'/>" maxlength="10" pattern="0[0-9]{9}">
      </label>
    </div>
    <div class="field">
      <label>Địa chỉ<input class="form-control" name="address" value="<c:out value='${user.address}'/>" maxlength="255">
      </label>
    </div>
    <div class="field">
      <label>Trạng thái<select name="status" class="form-select">
        <option value="1" ${user.status == 1 ? 'selected' : ''}>Hoạt động</option>
        <option value="0" ${user.status == 0 ? 'selected' : ''}>Đã khóa</option>
      </select>
    </label>
  </div>
  <button class="btn btn-brand">Lưu thay đổi</button>
  <a class="btn btn-light" href="${pageContext.request.contextPath}/admin/users">Quay lại</a>
</form>
</section>
<jsp:include page="/WEB-INF/views/shared/admin/footer.jsp" />
