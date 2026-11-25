<%@ tag pageEncoding="UTF-8" %>
<%@ attribute name="action" required="true" type="java.lang.String" %>
<%@ attribute name="fields" fragment="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<c:url var="target" value="${action}"/>
<form id="admin-batch" class="d-flex flex-wrap align-items-end gap-2 mb-3"
      method="post" action="${target}" data-admin-batch>
  <ui:csrf/>
  <div>
    <label class="form-label" for="admin-batch-action">Thao tác hàng loạt</label>
    <select id="admin-batch-action" name="action" class="form-select" required>
      <option value="">Chọn thao tác</option>
      <jsp:doBody/>
    </select>
  </div>
  <jsp:invoke fragment="fields"/>
  <button class="btn btn-outline-dark" type="submit">Áp dụng</button>
  <span class="small muted align-self-center" data-batch-count aria-live="polite" hidden></span>
  <div data-batch-hidden-ids hidden></div>
</form>
<section class="admin-batch-selection d-none" data-batch-selection aria-label="Danh sách đã chọn">
  <div class="d-flex flex-wrap align-items-center gap-2 mb-2">
    <strong>Mục đã chọn</strong>
    <button type="button" class="btn btn-sm btn-light" data-batch-clear>Bỏ chọn tất cả</button>
  </div>
  <ul class="admin-batch-items" data-batch-items></ul>
</section>
