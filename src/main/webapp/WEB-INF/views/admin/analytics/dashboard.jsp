<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
  <%@ taglib prefix="c" uri="jakarta.tags.core" %>
    <%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
      <jsp:include page="/WEB-INF/views/shared/admin/header.jsp" />

      <section class="panel analytics-controls" aria-label="Lọc thống kê">
        <div class="section-heading">
          <div>
            <h2>Khoảng thời gian</h2>
            <p class="muted small mb-0">Chọn nhanh 7/30 ngày hoặc nhập khoảng ngày tùy chỉnh.</p>
          </div>
          <c:url var="exportUrl" value="/admin/analytics/export">
            <c:param name="from" value="${from}" />
            <c:param name="to" value="${to}" />
          </c:url>
          <a class="btn btn-brand btn-sm" href="${exportUrl}">Xuất CSV</a>
        </div>
        <div class="analytics-presets" role="group" aria-label="Khoảng thời gian nhanh">
          <a class="btn btn-sm ${selectedPreset == '7' ? 'btn-brand' : 'btn-outline-dark'}"
            href="${pageContext.request.contextPath}/admin/analytics?days=7" ${selectedPreset=='7'
            ? 'aria-current="true"' : '' }>7 ngày</a>
          <a class="btn btn-sm ${selectedPreset == '30' ? 'btn-brand' : 'btn-outline-dark'}"
            href="${pageContext.request.contextPath}/admin/analytics?days=30" ${selectedPreset=='30'
            ? 'aria-current="true"' : '' }>30 ngày</a>
        </div>
        <form class="analytics-date-form" method="get" action="${pageContext.request.contextPath}/admin/analytics">
          <div class="field">
            <label for="analytics-from">Từ ngày</label>
            <input class="form-control" id="analytics-from" name="from" type="date" value="${from}" required>
          </div>
          <div class="field">
            <label for="analytics-to">Đến ngày</label>
            <input class="form-control" id="analytics-to" name="to" type="date" value="${to}" required>
          </div>
          <button class="btn btn-brand align-self-end" type="submit">Áp dụng</button>
        </form>
      </section>

      <section class="stat-grid analytics-stat-grid" aria-label="Chỉ số tổng quan">
        <article class="panel">
          <span>Lượt xem trong kỳ</span>
          <strong>
            <fmt:formatNumber value="${report.views}" type="number" />
          </strong>
        </article>
        <article class="panel">
          <span>Tỷ lệ chuyển đổi</span>
          <strong>
            <fmt:formatNumber value="${report.conversionRate}" maxFractionDigits="2" />%
          </strong>
          <small class="muted">Từ lượt nhấp sang thêm giỏ</small>
        </article>
        <article class="panel">
          <span>Đơn hàng trong kỳ</span>
          <strong>
            <fmt:formatNumber value="${report.orderCount}" type="number" />
          </strong>
        </article>
        <article class="panel">
          <span>Khách truy cập mới</span>
          <strong>
            <fmt:formatNumber value="${report.newVisitors}" type="number" />
          </strong>
          <small class="muted">Phiên lần đầu ghi nhận trong kỳ</small>
        </article>
        <article class="panel">
          <span>Lượt hỏi chatbot</span>
          <strong>
            <fmt:formatNumber value="${report.chatbotMessages}" type="number" />
          </strong>
        </article>
      </section>

      <section class="panel mb-4">
        <h2>Lượt xem theo ngày</h2>
        <p class="muted small">
          <c:out value="${from}" /> –
          <c:out value="${to}" />
        </p>
        <div class="chart-box">
          <canvas id="analytics-views-chart" data-labels="<c:out value='${dailyLabels}'/>"
            data-values="<c:out value='${dailyValues}'/>" role="img" aria-label="Biểu đồ lượt xem theo ngày"></canvas>
        </div>
      </section>

      <section class="panel mb-4">
        <h2>Top sản phẩm</h2>
        <div class="table-responsive">
          <table class="table">
            <thead>
              <tr>
                <th>Sản phẩm</th>
                <th>Lượt xem</th>
                <th>Lượt nhấp</th>
                <th>Thêm giỏ</th>
                <th>Chuyển đổi</th>
              </tr>
            </thead>
            <tbody>
              <c:forEach var="product" items="${report.topProducts}">
                <tr>
                  <td><a href="${pageContext.request.contextPath}/products/${product.productId}" target="_blank"
                      rel="noopener">
                      <c:out value="${product.productName}" />
                    </a></td>
                  <td>
                    <fmt:formatNumber value="${product.views}" type="number" />
                  </td>
                  <td>
                    <fmt:formatNumber value="${product.clicks}" type="number" />
                  </td>
                  <td>
                    <fmt:formatNumber value="${product.cartAdds}" type="number" />
                  </td>
                  <td>
                    <fmt:formatNumber value="${product.conversionRate}" maxFractionDigits="2" />%
                  </td>
                </tr>
              </c:forEach>
              <c:if test="${empty report.topProducts}">
                <tr>
                  <td colspan="5" class="muted">Chưa có dữ liệu sản phẩm trong khoảng thời gian này.</td>
                </tr>
              </c:if>
            </tbody>
          </table>
        </div>
      </section>

      <section class="panel">
        <h2>Top từ khóa tìm kiếm</h2>
        <div class="table-responsive">
          <table class="table">
            <thead>
              <tr>
                <th>Từ khóa</th>
                <th>Số lượt tìm kiếm</th>
              </tr>
            </thead>
            <tbody>
              <c:forEach var="keyword" items="${report.topKeywords}">
                <tr>
                  <td>
                    <c:out value="${keyword.keyword}" />
                  </td>
                  <td>
                    <fmt:formatNumber value="${keyword.count}" type="number" />
                  </td>
                </tr>
              </c:forEach>
              <c:if test="${empty report.topKeywords}">
                <tr>
                  <td colspan="2" class="muted">Chưa có từ khóa trong khoảng thời gian này.</td>
                </tr>
              </c:if>
            </tbody>
          </table>
        </div>
      </section>

      <jsp:include page="/WEB-INF/views/shared/admin/footer.jsp" />