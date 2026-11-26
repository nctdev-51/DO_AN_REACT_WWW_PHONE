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
          <form method="get" action="${pageContext.request.contextPath}/admin/promotions"
            class="row g-2 align-items-end mb-3">
            <div class="col-sm-6 col-xl">
              <label for="promotion-query" class="form-label">Mã / mô tả</label>
              <input id="promotion-query" name="q" type="search" class="form-control" value="<c:out value='${q}'/>">
            </div>
            <div class="col-sm-6 col-xl-auto">
              <label for="promotion-year" class="form-label">Năm áp dụng</label>
              <select id="promotion-year" name="year" class="form-select">
                <option value="">Tất cả năm</option>
                <c:forEach var="optionYear" items="${availableYears}">
                  <option value="${optionYear}" ${year==optionYear ? 'selected' : '' }>${optionYear}</option>
                </c:forEach>
              </select>
            </div>
            <div class="col-sm-6 col-xl-auto">
              <label for="promotion-quarter" class="form-label">Quý</label>
              <select id="promotion-quarter" name="quarter" class="form-select">
                <option value="">Cả năm</option>
                <option value="1" ${quarter==1 ? 'selected' : '' }>Quý 1 · Tháng 1–3</option>
                <option value="2" ${quarter==2 ? 'selected' : '' }>Quý 2 · Tháng 4–6</option>
                <option value="3" ${quarter==3 ? 'selected' : '' }>Quý 3 · Tháng 7–9</option>
                <option value="4" ${quarter==4 ? 'selected' : '' }>Quý 4 · Tháng 10–12</option>
              </select>
            </div>
            <div class="col-sm-6 col-xl">
              <label for="promotion-state" class="form-label">Trạng thái</label>
              <select id="promotion-state" name="state" class="form-select">
                <option value="">Tất cả</option>
                <option value="active" ${state=='active' ? 'selected' : '' }>Đang chạy</option>
                <option value="scheduled" ${state=='scheduled' ? 'selected' : '' }>Sắp diễn ra</option>
                <option value="expired" ${state=='expired' ? 'selected' : '' }>Đã hết hạn</option>
                <option value="disabled" ${state=='disabled' ? 'selected' : '' }>Đang tắt</option>
              </select>
            </div>
            <div class="col-sm-6 col-xl">
              <label for="promotion-combination" class="form-label">Kết hợp điểm thưởng</label>
              <select id="promotion-combination" name="combination" class="form-select">
                <option value="">Tất cả</option>
                <option value="compatible" ${combination=='compatible' ? 'selected' : '' }>Áp dụng chung</option>
                <option value="exclusive" ${combination=='exclusive' ? 'selected' : '' }>Áp dụng riêng</option>
              </select>
            </div>
            <div class="col-auto"><button class="btn btn-brand" type="submit">Lọc</button></div>
          </form>
          <ui:adminBatchForm action="/admin/promotions/batch">
            <option value="enable">Bật</option>
            <option value="disable">Tắt</option>
            <option value="delete" data-destructive>Xóa mục đã chọn</option>
          </ui:adminBatchForm>
          <div class="table-responsive">
            <table class="table align-middle">
              <thead>
                <tr>
                  <th scope="col"><input type="checkbox" data-batch-all aria-label="Chọn tất cả kết quả"></th>
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
                  <th>Kết hợp điểm</th>
                  <th>
                  </th>
                </tr>
              </thead>
              <tbody>
                <c:forEach var="promotion" items="${promotions}">
                  <tr>
                    <td><input type="checkbox" name="ids" value="${promotion.id}" form="admin-batch"
                        data-batch-label="<c:out value='${promotion.code}'/>"
                        data-batch-actions="${promotion.active ? 'disable' : 'enable'}|delete"
                        aria-label="Chọn mục ${promotion.id}"></td>
                    <td>
                      <c:out value="${promotion.code}" />
                    </td>
                    <td>
                      <c:out value="${promotion.description}" />
                    </td>
                    <td>
                      Đơn từ
                      <ui:money value="${promotion.minimumSubtotal}" />
                      <small class="d-block">
                        <c:out value="${promotion.startsAt}" />
                        →
                        <c:out value="${promotion.endsAt}" />
                      </small>
                    </td>
                    <td>
                      ${remaining[promotion.id]} / ${promotion.usageLimit}
                    </td>
                    <td>
                      <c:choose>
                        <c:when test="${promotionStates[promotion.id] == 'expired'}"><span class="status-badge">Đã hết
                            hạn</span></c:when>
                        <c:when test="${promotionStates[promotion.id] == 'scheduled'}"><span
                            class="status-badge status-warning">Sắp diễn ra</span></c:when>
                        <c:when test="${promotionStates[promotion.id] == 'active'}"><span
                            class="status-badge status-success">Đang chạy</span></c:when>
                        <c:otherwise><span class="status-badge status-danger">Đang tắt</span></c:otherwise>
                      </c:choose>
                    </td>
                    <td>${promotion.combinableWithPoints ? 'Áp dụng chung' : 'Áp dụng riêng'}</td>
                    <td>
                      <a class="btn btn-sm btn-outline-dark"
                        href="${pageContext.request.contextPath}/admin/promotions/${promotion.id}/edit">
                        Sửa
                      </a>
                    </td>
                  </tr>
                </c:forEach>
                <c:if test="${empty promotions}">
                  <tr>
                    <td colspan="8">Không có khuyến mãi phù hợp.</td>
                  </tr>
                </c:if>
              </tbody>
            </table>
          </div>
        </section>
        <section class="panel mt-4" aria-labelledby="inventory-promotion-title">
          <h2 id="inventory-promotion-title">Gợi ý khuyến mãi theo tồn kho</h2>
          <p>Số bán trong ${salesDays} ngày chỉ tính đơn đã giao.</p>
          <c:choose>
            <c:when test="${empty inventorySuggestions}">
              <p class="text-muted mb-0">Chưa có sản phẩm đạt ngưỡng gợi ý.</p>
            </c:when>
            <c:otherwise>
              <div class="table-responsive">
                <table class="table align-middle">
                  <thead>
                    <tr>
                      <th scope="col">Sản phẩm</th>
                      <th scope="col">Tồn hiện tại</th>
                      <th scope="col">Bán trong ${salesDays} ngày</th>
                      <th scope="col">Giảm gợi ý</th>
                      <th scope="col">Thao tác</th>
                    </tr>
                  </thead>
                  <tbody>
                    <c:forEach var="row" items="${inventorySuggestions}">
                      <tr>
                        <th scope="row">
                          <c:out value="${row.productName}" />
                        </th>
                        <td>${row.stock}</td>
                        <td>${row.unitsSold}</td>
                        <td><strong>${row.discountPercent}%</strong></td>
                        <td><a class="btn btn-sm btn-outline-dark"
                            href="${pageContext.request.contextPath}/admin/promotions/inventory-suggestions/${row.productId}/apply">Áp
                            dụng khuyến mãi</a></td>
                      </tr>
                    </c:forEach>
                  </tbody>
                </table>
              </div>
            </c:otherwise>
          </c:choose>
        </section>
        <section class="panel mt-4">
          <h2>
            Phí giao hàng
          </h2>
          <p>
            Ngưỡng miễn phí tính theo tiền sản phẩm trước giảm giá. Đơn cũ giữ nguyên phí đã chốt.
          </p>
          <form:form modelAttribute="policyForm" method="post"
            action="${pageContext.request.contextPath}/admin/promotions/policy">
            <form:errors path="*" element="div" cssClass="alert alert-danger" />
            <form:hidden path="version" />
            <div class="field">
              <label for="shippingFee">
                Phí mặc định (đồng)
              </label>
              <form:input path="shippingFee" id="shippingFee" type="number" min="0" max="10000000" step="1"
                cssClass="form-control" required="required" />
            </div>
            <div class="field">
              <label for="freeShippingThreshold">
                Miễn phí cho đơn từ (đồng)
              </label>
              <form:input path="freeShippingThreshold" id="freeShippingThreshold" type="number" min="0" max="1000000000"
                step="1" cssClass="form-control" required="required" />
            </div>
            <div class="field">
              <label for="returnWindowDays">
                Thời hạn tiếp nhận đổi/trả sau giao hàng (ngày)
              </label>
              <form:input path="returnWindowDays" id="returnWindowDays" type="number" min="1" max="30"
                cssClass="form-control" required="required" />
            </div>
            <button class="btn btn-brand">
              Lưu chính sách cửa hàng
            </button>
          </form:form>
        </section>
        <jsp:include page="/WEB-INF/views/shared/admin/footer.jsp" />