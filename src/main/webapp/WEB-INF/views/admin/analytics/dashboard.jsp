<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<jsp:include page="/WEB-INF/views/shared/admin/header.jsp"/>
<fmt:setLocale value="vi_VN"/>
<c:url var="analyticsUrl" value="/admin/analytics"/>
<c:url var="exportUrl" value="/admin/analytics/export">
  <c:param name="from" value="${from}"/>
  <c:param name="to" value="${to}"/>
</c:url>

<div class="insights">
  <section class="insights-toolbar" aria-label="Bộ lọc thống kê">
    <nav class="insights-segment" aria-label="Khoảng thời gian nhanh">
      <a href="${analyticsUrl}?days=7" aria-current="${selectedPreset == '7' ? 'page' : 'false'}">7 ngày</a>
      <a href="${analyticsUrl}?days=30" aria-current="${selectedPreset == '30' ? 'page' : 'false'}">30 ngày</a>
    </nav>
    <form method="get" action="${analyticsUrl}" class="insights-dates">
      <label for="analytics-from">Từ ngày</label>
      <input id="analytics-from" name="from" type="date" value="${from}" required>
      <label for="analytics-to">Đến ngày</label>
      <input id="analytics-to" name="to" type="date" value="${to}" required>
      <button class="insights-action insights-action-primary" type="submit"><ui:icon name="filter"/>Áp dụng</button>
    </form>
    <a class="insights-action" href="${exportUrl}">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" aria-hidden="true"><path d="M12 3v12m-4-4 4 4 4-4M5 16v4h14v-4"/></svg>
      Xuất CSV
    </a>
  </section>

  <dl class="insights-metrics" aria-label="Chỉ số trong kỳ">
    <div class="insights-metric">
      <dt>Tổng lượt xem <span aria-hidden="true"><ui:icon name="search"/></span></dt>
      <dd><fmt:formatNumber value="${report.views}"/></dd>
    </div>
    <div class="insights-metric">
      <dt>Khách truy cập mới <span aria-hidden="true"><ui:icon name="user"/></span></dt>
      <dd><fmt:formatNumber value="${report.newVisitors}"/></dd>
    </div>
    <div class="insights-metric">
      <dt>Tỷ lệ thêm giỏ <span aria-hidden="true"><ui:icon name="cart"/></span></dt>
      <dd>
        <fmt:formatNumber value="${report.conversionRate}" maxFractionDigits="2"/>%
        <p class="insights-metric-note">Trên số lượt nhấp sản phẩm</p>
      </dd>
    </div>
    <div class="insights-metric">
      <dt>Đơn hàng <span aria-hidden="true"><ui:icon name="box"/></span></dt>
      <dd><fmt:formatNumber value="${report.orderCount}"/></dd>
    </div>
    <div class="insights-metric">
      <dt>Lượt hỏi chatbot <span aria-hidden="true"><ui:icon name="compare"/></span></dt>
      <dd><fmt:formatNumber value="${report.chatbotMessages}"/></dd>
    </div>
  </dl>

  <div class="insights-grid">
    <section class="insights-card insights-trend" aria-labelledby="analytics-trend-title">
      <header class="insights-card-head">
        <div>
          <h2 id="analytics-trend-title">Xu hướng lượt xem</h2>
          <p><c:out value="${from}"/> – <c:out value="${to}"/></p>
        </div>
        <c:if test="${report.views > 0}">
          <div class="insights-segment insights-chart-switch" role="group" aria-label="Kiểu biểu đồ lượt xem" hidden>
            <button type="button" data-chart-type="line" aria-pressed="true">Đường</button>
            <button type="button" data-chart-type="bar" aria-pressed="false">Cột</button>
          </div>
        </c:if>
      </header>
      <c:choose>
        <c:when test="${report.views > 0}">
          <p class="insights-trend-total"><strong><fmt:formatNumber value="${report.views}"/></strong> lượt xem</p>
          <div class="insights-plot insights-trend-plot">
            <canvas id="analytics-views-chart" role="img" aria-label="Lượt xem theo ngày"
                    data-labels="<c:out value='${dailyLabels}'/>"
                    data-values="<c:out value='${dailyValues}'/>"></canvas>
            <noscript><p>Bật JavaScript để xem biểu đồ lượt xem theo ngày.</p></noscript>
          </div>
        </c:when>
        <c:otherwise><p class="insights-empty">Chưa có lượt xem trong khoảng ngày đã chọn.</p></c:otherwise>
      </c:choose>
    </section>

    <section class="insights-card insights-topics" aria-labelledby="analytics-topics-title">
      <header class="insights-card-head">
        <div><h2 id="analytics-topics-title">Chủ đề chatbot</h2><p>Phân bố câu hỏi trong kỳ</p></div>
      </header>
      <c:choose>
        <c:when test="${not empty aiTopicStats}">
          <div class="insights-ring" hidden>
            <canvas id="analytics-topics-chart" role="img" aria-label="Phân bố các chủ đề chatbot"></canvas>
            <div class="insights-ring-total" aria-hidden="true">
              <strong><fmt:formatNumber value="${report.chatbotMessages}"/></strong>
              <span>lượt hỏi</span>
            </div>
          </div>
          <ul class="insights-topic-list">
            <c:forEach var="topic" items="${aiTopicStats}">
              <li data-topic-count="${topic.count}">
                <span class="insights-topic-dot" aria-hidden="true"></span>
                <span data-topic-label><c:out value="${topic.topic}"/></span>
                <strong><fmt:formatNumber value="${topic.count}"/></strong>
                <span class="insights-topic-share">${topic.percentage}%</span>
              </li>
            </c:forEach>
          </ul>
        </c:when>
        <c:otherwise><p class="insights-empty">Chưa có câu hỏi trong khoảng ngày đã chọn.</p></c:otherwise>
      </c:choose>
    </section>

    <section class="insights-card" aria-labelledby="analytics-products-title">
      <header class="insights-card-head">
        <div><h2 id="analytics-products-title">Tương tác sản phẩm</h2><p>So sánh 5 sản phẩm có nhiều lượt xem nhất</p></div>
      </header>
      <c:choose>
        <c:when test="${not empty report.topProducts}">
          <div class="insights-product-visual" hidden>
            <div class="insights-key" aria-hidden="true"><span>Lượt xem</span><span>Lượt nhấp</span><span>Thêm giỏ</span></div>
            <div class="insights-plot insights-product-plot">
              <canvas id="analytics-products-chart" role="img" aria-label="So sánh lượt xem, lượt nhấp và thêm giỏ theo sản phẩm"></canvas>
            </div>
          </div>
          <details class="insights-details" open>
            <summary>Xem số liệu sản phẩm</summary>
            <div class="insights-table-scroll">
              <table class="insights-table">
                <thead><tr><th scope="col">Sản phẩm</th><th scope="col">Xem</th><th scope="col">Nhấp</th><th scope="col">Thêm giỏ</th><th scope="col">Tỷ lệ</th></tr></thead>
                <tbody>
                  <c:forEach var="product" items="${report.topProducts}">
                    <tr data-product-views="${product.views}" data-product-clicks="${product.clicks}" data-product-carts="${product.cartAdds}">
                      <th scope="row"><a href="${pageContext.request.contextPath}/products/${product.productId}"><c:out value="${product.productName}"/></a></th>
                      <td><fmt:formatNumber value="${product.views}"/></td>
                      <td><fmt:formatNumber value="${product.clicks}"/></td>
                      <td><fmt:formatNumber value="${product.cartAdds}"/></td>
                      <td><fmt:formatNumber value="${product.conversionRate}" maxFractionDigits="1"/>%</td>
                    </tr>
                  </c:forEach>
                </tbody>
              </table>
            </div>
          </details>
        </c:when>
        <c:otherwise><p class="insights-empty">Chưa có tương tác sản phẩm trong khoảng ngày đã chọn.</p></c:otherwise>
      </c:choose>
    </section>

    <section class="insights-card" aria-labelledby="analytics-keywords-title">
      <header class="insights-card-head">
        <div><h2 id="analytics-keywords-title">Từ khóa tìm kiếm</h2><p>Xếp theo số lượt tìm</p></div>
      </header>
      <c:choose>
        <c:when test="${not empty report.topKeywords}">
          <ol class="insights-keywords">
            <c:forEach var="keyword" items="${report.topKeywords}">
              <li>
                <div><span><c:out value="${keyword.keyword}"/></span><strong><fmt:formatNumber value="${keyword.count}"/></strong></div>
                <meter min="0" max="${report.topKeywords[0].count}" value="${keyword.count}" aria-hidden="true"></meter>
              </li>
            </c:forEach>
          </ol>
        </c:when>
        <c:otherwise><p class="insights-empty">Chưa có từ khóa tìm kiếm trong khoảng ngày đã chọn.</p></c:otherwise>
      </c:choose>
    </section>
  </div>

  <section class="insights-card" aria-labelledby="analytics-stock-title">
    <header class="insights-card-head">
      <div><h2 id="analytics-stock-title">Cảnh báo tồn kho</h2><p>Dự báo 48 giờ. Tín hiệu gần nhất được so với 14 ngày trước.</p></div>
    </header>
    <c:choose>
      <c:when test="${not empty stockoutForecasts}">
        <div class="insights-table-scroll">
          <table class="insights-table insights-stock-table">
            <thead><tr><th scope="col">Sản phẩm</th><th scope="col">Tồn kho</th><th scope="col">Lượt xem</th><th scope="col">Thêm giỏ</th><th scope="col">Tín hiệu</th><th scope="col">Đề xuất</th></tr></thead>
            <tbody>
              <c:forEach var="item" items="${stockoutForecasts}">
                <tr>
                  <th scope="row"><a href="${pageContext.request.contextPath}/products/${item.productId}"><c:out value="${item.productName}"/></a></th>
                  <td><fmt:formatNumber value="${item.stock}"/></td>
                  <td><fmt:formatNumber value="${item.views}"/></td>
                  <td><fmt:formatNumber value="${item.cartAdds}"/></td>
                  <td><span class="insights-risk ${item.riskLevel == 'HIGH' or item.riskLevel == 'OUT' ? 'insights-risk-high' : 'insights-risk-watch'}"><c:out value="${item.riskBadge}"/></span></td>
                  <td><c:out value="${item.recommendation}"/></td>
                </tr>
              </c:forEach>
            </tbody>
          </table>
        </div>
      </c:when>
      <c:otherwise><p class="insights-empty">Chưa phát hiện tín hiệu cần xử lý trong 48 giờ gần nhất.</p></c:otherwise>
    </c:choose>
  </section>

  <section class="insights-card" aria-labelledby="analytics-questions-title">
    <header class="insights-card-head"><h2 id="analytics-questions-title">Câu hỏi chatbot gần đây</h2></header>
    <c:choose>
      <c:when test="${not empty recentAiQuestions}">
        <ul class="insights-questions">
          <c:forEach var="chat" items="${recentAiQuestions}">
            <li>
              <p><c:out value="${chat.question}"/></p>
              <div><span><c:out value="${chat.topicTag}"/></span><small><c:out value="${chat.timeAgo}"/></small></div>
            </li>
          </c:forEach>
        </ul>
      </c:when>
      <c:otherwise><p class="insights-empty">Chưa có câu hỏi nào từ khách hàng.</p></c:otherwise>
    </c:choose>
  </section>
</div>

<jsp:include page="/WEB-INF/views/shared/admin/footer.jsp"/>
