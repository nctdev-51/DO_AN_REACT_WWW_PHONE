<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<jsp:include page="/WEB-INF/views/shared/admin/header.jsp" />
<section class="panel">
  <h2>
    ${promotionId == null ? 'Thêm mã giảm giá' : 'Sửa mã giảm giá'}
  </h2>
  <form:form modelAttribute="promotionForm" method="post" action="${pageContext.request.contextPath}/admin/promotions/save">
    <form:errors path="*" element="div" cssClass="alert alert-danger"/>
    <c:if test="${promotionId != null}">
      <input type="hidden" name="id" value="${promotionId}">
    </c:if>
    <form:hidden path="version"/>
    <div class="field">
      <label for="code">
        Mã
      </label>
      <form:input path="code" id="code" cssClass="form-control" maxlength="40" required="required"/>
    </div>
    <div class="field">
      <label for="description">
        Mô tả
      </label>
      <form:input path="description" id="description" cssClass="form-control" maxlength="200" required="required"/>
    </div>
    <div class="field"><label for="requiredSkus">SKU bắt buộc có trong giỏ cho combo / quà tặng (cách nhau dấu phẩy)</label><form:input path="requiredSkus" id="requiredSkus" cssClass="form-control" maxlength="1000"/><small>Để trống nếu không có điều kiện mua kèm. Quà tặng cần có sản phẩm thật trong giỏ, giá trị giảm bị giới hạn bởi trần giảm.</small></div>
    <div class="field"><label for="scopeBrand">Giới hạn hãng (để trống: mọi hãng)</label><form:input path="scopeBrand" id="scopeBrand" cssClass="form-control" maxlength="100"/></div><div class="field"><label for="scopeSkus">SKU sản phẩm áp dụng, cách nhau dấu phẩy (để trống: không giới hạn SKU)</label><form:input path="scopeSkus" id="scopeSkus" cssClass="form-control" maxlength="1000"/><small>Nếu chọn cả hãng và SKU, sản phẩm phải thỏa cả hai. Giá trị tối thiểu chỉ tính các sản phẩm đủ điều kiện.</small></div>
    <div class="field">
      <label for="discountType">
        Cách giảm
      </label>
      <form:select path="discountType" id="discountType" cssClass="form-select">
        <form:option value="FIXED">
          Số tiền cố định (đồng)
        </form:option>
        <form:option value="PERCENT">
          Phần trăm (%)
        </form:option>
      </form:select>
    </div>
    <div class="field">
      <label for="amount">
        Mức giảm
      </label>
      <form:input path="amount" id="amount" type="number" min="1" step="1" cssClass="form-control" required="required"/>
    </div>
    <div class="field">
      <label for="minimumSubtotal">
        Đơn tối thiểu (đồng)
      </label>
      <form:input path="minimumSubtotal" id="minimumSubtotal" type="number" min="0" step="1" cssClass="form-control" required="required"/>
    </div>
    <div class="field">
      <label for="maximumDiscount">
        Trần giảm (đồng, để trống nếu không có)
      </label>
      <form:input path="maximumDiscount" id="maximumDiscount" type="number" min="1" step="1" cssClass="form-control"/>
    </div>
    <div class="field">
      <label for="startsAt">
        Bắt đầu
      </label>
      <form:input path="startsAt" id="startsAt" type="datetime-local" cssClass="form-control" required="required"/>
    </div>
    <div class="field">
      <label for="endsAt">
        Kết thúc
      </label>
      <form:input path="endsAt" id="endsAt" type="datetime-local" cssClass="form-control" required="required"/>
    </div>
    <div class="field">
      <label for="usageLimit">
        Tổng lượt sử dụng
      </label>
      <form:input path="usageLimit" id="usageLimit" type="number" min="1" max="1000000" cssClass="form-control" required="required"/>
    </div>
    <label class="d-block mb-3">
      <form:checkbox path="active"/>
      Cho phép sử dụng
    </label>
    <button class="btn btn-brand">
      Lưu khuyến mãi
    </button>
    <a class="btn btn-light" href="${pageContext.request.contextPath}/admin/promotions">
      Quay lại
    </a>
  </form:form>
</section>
<jsp:include page="/WEB-INF/views/shared/admin/footer.jsp" />
