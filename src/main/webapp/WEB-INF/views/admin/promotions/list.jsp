<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<jsp:include page="/WEB-INF/views/shared/admin/header.jsp" />
<section class="panel">
  <div class="section-heading">
    <h2>
      Mã giảm giá
    </h2>
    <a class="btn btn-brand" href="${pageContext.request.contextPath}/admin/promotions/new">
      Thêm khuyến mãi
    </a>
  </div>
  <div class="table-responsive">
    <table class="table align-middle">
      <thead>
        <tr>
          <th>
            Mã
          </th>
          <th>
            Nội dung
          </th>
          <th>
            Điều kiện
          </th>
          <th>
            Lượt còn
          </th>
          <th>
            Trạng thái
          </th>
          <th>
          </th>
        </tr>
      </thead>
      <tbody>
        <c:forEach var="promotion" items="${promotions}">
          <tr>
            <td>
              <c:out value="${promotion.code}"/>
            </td>
            <td>
              <c:out value="${promotion.description}"/>
            </td>
            <td>
              Đơn từ
              <ui:money value="${promotion.minimumSubtotal}"/>
              <small class="d-block">
                <c:out value="${promotion.startsAt}"/>
                →
                <c:out value="${promotion.endsAt}"/>
              </small>
            </td>
            <td>
              ${remaining[promotion.id]} / ${promotion.usageLimit}
            </td>
            <td>
              ${promotion.active ? 'Bật' : 'Tắt'}
            </td>
            <td>
              <a class="btn btn-sm btn-outline-dark" href="${pageContext.request.contextPath}/admin/promotions/${promotion.id}/edit">
                Sửa
              </a>
            </td>
          </tr>
        </c:forEach>
      </tbody>
    </table>
  </div>
</section>
<section class="panel mt-4">
  <h2>
    Phí giao hàng
  </h2>
  <p>
    Ngưỡng miễn phí tính theo tiền sản phẩm trước giảm giá. Đơn cũ giữ nguyên phí đã chốt.
  </p>
  <form:form modelAttribute="policyForm" method="post" action="${pageContext.request.contextPath}/admin/promotions/policy">
    <form:errors path="*" element="div" cssClass="alert alert-danger"/>
    <form:hidden path="version"/>
    <div class="field">
      <label for="shippingFee">
        Phí mặc định (đồng)
      </label>
      <form:input path="shippingFee" id="shippingFee" type="number" min="0" max="10000000" step="1" cssClass="form-control" required="required"/>
    </div>
    <div class="field">
      <label for="freeShippingThreshold">
        Miễn phí cho đơn từ (đồng)
      </label>
      <form:input path="freeShippingThreshold" id="freeShippingThreshold" type="number" min="0" max="1000000000" step="1" cssClass="form-control" required="required"/>
    </div>
    <div class="field">
      <label for="returnWindowDays">
        Thời hạn tiếp nhận đổi/trả sau giao hàng (ngày)
      </label>
      <form:input path="returnWindowDays" id="returnWindowDays" type="number" min="1" max="30" cssClass="form-control" required="required"/>
    </div>
    <button class="btn btn-brand">
      Lưu chính sách cửa hàng
    </button>
  </form:form>
</section>
<jsp:include page="/WEB-INF/views/shared/admin/footer.jsp" />
