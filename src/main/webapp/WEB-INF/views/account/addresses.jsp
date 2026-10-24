<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<jsp:include page="/WEB-INF/views/shared/store/header.jsp" />
<jsp:include page="/WEB-INF/views/shared/store/account-nav.jsp" />
<div class="page-heading">
  <h1>Sổ địa chỉ</h1>
</div>
<div class="address-grid">
  <c:forEach var="address" items="${addresses}">
    <article class="panel">
      <c:set var="editing" value="${failedAddressId == address.id and not empty failedAddressForm ? failedAddressForm : address}"/>
      <h2>${address.defaultAddress ? 'Địa chỉ mặc định' : 'Địa chỉ nhận hàng'}</h2>
      <form action="${pageContext.request.contextPath}/account/addresses" method="post">
        <ui:csrf/>
        <input type="hidden" name="id" value="${address.id}">
        <div class="field">
          <label>Người nhận<input class="form-control" name="recipientName" value="<c:out value='${editing.recipientName}'/>" required maxlength="100">
          </label>
        </div>
        <div class="field">
          <label>Số điện thoại<input class="form-control" name="phone" value="<c:out value='${editing.phone}'/>" required maxlength="10" pattern="0[0-9]{9}">
          </label>
        </div>
        <ui:addressPicker id="saved-${address.id}" provinceCode="${editing.provinceCode}" wardCode="${editing.wardCode}" province="${editing.province}" ward="${editing.ward}"/>
        <div class="field">
          <label>Số nhà, tên đường<textarea class="form-control" name="line" required maxlength="255">
            <c:out value="${editing.line}"/>
          </textarea>
        </label>
      </div>
      <label class="payment-choice">
        <input type="checkbox" name="defaultAddress" value="true" ${address.defaultAddress ? 'checked' : ''}>Đặt làm địa chỉ mặc định</label>
        <button class="btn btn-brand mt-3">Lưu địa chỉ</button>
      </form>
      <form action="${pageContext.request.contextPath}/account/addresses/${address.id}/delete" method="post" data-confirm="Xóa địa chỉ này?">
        <ui:csrf/>
        <button class="btn btn-link text-danger mt-2">Xóa</button>
      </form>
    </article>
  </c:forEach>
  <article class="panel">
    <h2>Thêm địa chỉ mới</h2><c:set var="newAddress" value="${empty failedAddressId ? failedAddressForm : null}"/>
    <form action="${pageContext.request.contextPath}/account/addresses" method="post">
      <ui:csrf/>
      <div class="field">
        <label>Người nhận<input class="form-control" name="recipientName" value="<c:out value='${newAddress.recipientName}'/>" required maxlength="100">
        </label>
      </div>
      <div class="field">
        <label>Số điện thoại<input class="form-control" name="phone" value="<c:out value='${newAddress.phone}'/>" required maxlength="10" pattern="0[0-9]{9}">
        </label>
      </div>
      <ui:addressPicker id="new-address" provinceCode="${newAddress.provinceCode}" wardCode="${newAddress.wardCode}" province="${newAddress.province}" ward="${newAddress.ward}"/>
      <div class="field">
        <label>Số nhà, tên đường<textarea class="form-control" name="line" required maxlength="255"><c:out value="${newAddress.line}"/></textarea>
      </label>
    </div>
    <label class="payment-choice">
      <input type="checkbox" name="defaultAddress" value="true">Đặt làm mặc định</label>
      <button class="btn btn-brand mt-3">Thêm địa chỉ</button>
    </form>
  </article>
</div>
<jsp:include page="/WEB-INF/views/shared/store/footer.jsp" />
