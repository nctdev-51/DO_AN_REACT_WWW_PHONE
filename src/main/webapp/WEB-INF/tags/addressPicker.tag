<%@ tag pageEncoding="UTF-8" %>
<%@ attribute name="id" required="true" type="java.lang.String" %>
<%@ attribute name="provinceCode" required="false" type="java.lang.String" %>
<%@ attribute name="wardCode" required="false" type="java.lang.String" %>
<%@ attribute name="province" required="false" type="java.lang.String" %>
<%@ attribute name="ward" required="false" type="java.lang.String" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<div data-address-picker data-context="${pageContext.request.contextPath}" data-province="<c:out value='${provinceCode}'/>" data-ward="<c:out value='${wardCode}'/>">
  <input type="hidden" name="addressMode" value="structured">
  <div data-address-enhanced hidden class="filter-grid">
    <div class="field"><label for="${id}-province">Tỉnh / thành phố</label><select id="${id}-province" name="provinceCode" class="form-select" data-province-select disabled required><option value="">Chọn tỉnh / thành phố</option><c:forEach var="p" items="${locationProvinces}"><option value="${p.code}" ${provinceCode == p.code ? 'selected' : ''}><c:out value="${p.name}"/></option></c:forEach></select></div>
    <div class="field"><label for="${id}-ward">Phường / xã</label><select id="${id}-ward" name="wardCode" class="form-select" data-ward-select disabled required><option value="">Chọn tỉnh trước</option></select></div>
  </div>
  <div data-address-fallback class="filter-grid">
    <div class="field"><label for="${id}-province-name">Tên tỉnh / thành phố</label><input id="${id}-province-name" name="province" class="form-control" list="${id}-province-list" required maxlength="100" value="<c:out value='${province}'/>" autocomplete="address-level1"><datalist id="${id}-province-list"><c:forEach var="p" items="${locationProvinces}"><option value="<c:out value='${p.name}'/>"></option></c:forEach></datalist></div>
    <div class="field"><label for="${id}-ward-name">Tên phường / xã</label><input id="${id}-ward-name" name="ward" class="form-control" required maxlength="100" value="<c:out value='${ward}'/>" autocomplete="address-level2"></div>
  </div>
  <p class="small muted" data-address-status role="status">Chọn địa chỉ hành chính hiện hành để cửa hàng xác nhận giao nhận chính xác.</p>
  <button type="button" class="btn btn-light btn-sm" data-address-retry hidden>Thử tải lại danh sách phường / xã</button>
</div>
