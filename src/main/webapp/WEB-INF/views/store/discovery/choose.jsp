<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<jsp:include page="/WEB-INF/views/shared/store/header.jsp"/>
<ui:breadcrumb currentLabel="Chọn điện thoại phù hợp"/>
<section class="discovery-intro">
  <span class="eyebrow">HƯỚNG DẪN CHỌN MÁY 4 BƯỚC</span>
  <h1>Chọn điện thoại đúng với nhu cầu của bạn.</h1>
  <p>Trả lời 4 câu hỏi nhanh về ngân sách, mục đích dùng, ưu tiên phần cứng và kích thước để xem những phiên bản còn hàng phù hợp nhất.</p>
</section>

<form class="panel advisor-wizard" method="get" action="${pageContext.request.contextPath}/choose" data-advisor-wizard>
  <div class="advisor-progress" role="tablist" aria-label="Các bước chọn máy">
    <button type="button" class="advisor-step-tab is-active" data-advisor-tab="1"><strong>1.</strong> Ngân sách</button>
    <button type="button" class="advisor-step-tab" data-advisor-tab="2"><strong>2.</strong> Nhu cầu chính</button>
    <button type="button" class="advisor-step-tab" data-advisor-tab="3"><strong>3.</strong> Ưu tiên</button>
    <button type="button" class="advisor-step-tab" data-advisor-tab="4"><strong>4.</strong> Kích thước &amp; Hãng</button>
  </div>

  <div class="advisor-panel" data-advisor-panel="1">
    <h2>Bước 1: Ngân sách tối đa của bạn là bao nhiêu?</h2>
    <p class="muted small">Hệ thống chỉ đề xuất các phiên bản còn hàng có giá bằng hoặc thấp hơn mức bạn chọn.</p>
    <div class="advisor-preset-row">
      <button type="button" class="btn btn-light btn-sm" data-advisor-budget="10000000">Dưới 10 triệu</button>
      <button type="button" class="btn btn-light btn-sm" data-advisor-budget="15000000">Khoảng 15 triệu</button>
      <button type="button" class="btn btn-light btn-sm" data-advisor-budget="25000000">Khoảng 25 triệu</button>
      <button type="button" class="btn btn-light btn-sm" data-advisor-budget="45000000">Flagship (đến 45tr)</button>
    </div>
    <div class="field mt-3">
      <label for="budget">Ngân sách tối đa (VNĐ)</label>
      <input id="budget" name="budget" type="number" min="1000000" max="150000000" step="100000" value="${empty budget ? 20000000 : budget}" class="form-control" required>
    </div>
    <div class="advisor-nav-actions">
      <button type="button" class="btn btn-brand" data-advisor-next="2">Tiếp tục: Nhu cầu chính →</button>
    </div>
  </div>

  <div class="advisor-panel" data-advisor-panel="2">
    <h2>Bước 2: Bạn sử dụng điện thoại cho mục đích gì nhiều nhất?</h2>
    <div class="advisor-choice-grid">
      <label class="advisor-choice-card">
        <input type="radio" name="need" value="EVERYDAY" ${empty need or need == 'EVERYDAY' ? 'checked' : ''}>
        <strong>Dùng hằng ngày</strong>
        <span class="muted small">Liên lạc, mạng xã hội, lướt web ổn định, cân bằng chi phí.</span>
      </label>
      <label class="advisor-choice-card">
        <input type="radio" name="need" value="STUDY_WORK" ${need == 'STUDY_WORK' ? 'checked' : ''}>
        <strong>Học tập &amp; làm việc</strong>
        <span class="muted small">Đa nhiệm tài liệu, họp trực tuyến, pin đủ dùng cả ngày.</span>
      </label>
      <label class="advisor-choice-card">
        <input type="radio" name="need" value="CAMERA" ${need == 'CAMERA' ? 'checked' : ''}>
        <strong>Chụp ảnh &amp; du lịch</strong>
        <span class="muted small">Ưu tiên cảm biến camera sắc nét, zoom và chống rung.</span>
      </label>
      <label class="advisor-choice-card">
        <input type="radio" name="need" value="GAMING" ${need == 'GAMING' ? 'checked' : ''}>
        <strong>Chơi game &amp; đồ họa</strong>
        <span class="muted small">Chip xử lý mạnh, RAM lớn và màn hình mượt.</span>
      </label>
      <label class="advisor-choice-card">
        <input type="radio" name="need" value="CREATOR" ${need == 'CREATOR' ? 'checked' : ''}>
        <strong>Sáng tạo nội dung</strong>
        <span class="muted small">Quay chụp chất lượng cao cùng bộ nhớ từ 256GB trở lên.</span>
      </label>
    </div>
    <div class="advisor-nav-actions">
      <button type="button" class="btn btn-light" data-advisor-next="1">← Quay lại</button>
      <button type="button" class="btn btn-brand" data-advisor-next="3">Tiếp tục: Ưu tiên phần cứng →</button>
    </div>
  </div>

  <div class="advisor-panel" data-advisor-panel="3">
    <h2>Bước 3: Yếu tố nào bạn ưu tiên hàng đầu?</h2>
    <div class="advisor-choice-grid">
      <label class="advisor-choice-card">
        <input type="radio" name="priority" value="CAMERA" ${empty priority or priority == 'CAMERA' ? 'checked' : ''}>
        <strong>Camera sắc nét</strong>
        <span class="muted small">Độ phân giải cao, chụp thiếu sáng &amp; zoom tốt.</span>
      </label>
      <label class="advisor-choice-card">
        <input type="radio" name="priority" value="PERFORMANCE" ${priority == 'PERFORMANCE' ? 'checked' : ''}>
        <strong>Hiệu năng &amp; Chipset</strong>
        <span class="muted small">Xử lý mượt mà các ứng dụng nặng và đa nhiệm.</span>
      </label>
      <label class="advisor-choice-card">
        <input type="radio" name="priority" value="BATTERY" ${priority == 'BATTERY' ? 'checked' : ''}>
        <strong>Thời lượng pin</strong>
        <span class="muted small">Dung lượng pin lớn cho cường độ sử dụng cao.</span>
      </label>
      <label class="advisor-choice-card">
        <input type="radio" name="priority" value="DISPLAY" ${priority == 'DISPLAY' ? 'checked' : ''}>
        <strong>Màn hình đẹp, 120Hz</strong>
        <span class="muted small">Tấm nền OLED/AMOLED rực rỡ, cuộn chạm mượt mà.</span>
      </label>
      <label class="advisor-choice-card">
        <input type="radio" name="priority" value="STORAGE" ${priority == 'STORAGE' ? 'checked' : ''}>
        <strong>Bộ nhớ lớn</strong>
        <span class="muted small">Ưu tiên phiên bản 256GB, 512GB hoặc 1TB.</span>
      </label>
    </div>
    <div class="advisor-nav-actions">
      <button type="button" class="btn btn-light" data-advisor-next="2">← Quay lại</button>
      <button type="button" class="btn btn-brand" data-advisor-next="4">Tiếp tục: Kích thước &amp; Hãng →</button>
    </div>
  </div>

  <div class="advisor-panel" data-advisor-panel="4">
    <h2>Bước 4: Kích thước máy &amp; thương hiệu mong muốn</h2>
    <div class="advisor-choice-grid">
      <label class="advisor-choice-card">
        <input type="radio" name="sizePreference" value="ANY" ${empty sizePreference or sizePreference == 'ANY' ? 'checked' : ''}>
        <strong>Linh hoạt mọi kích thước</strong>
        <span class="muted small">Không giới hạn kích thước màn hình.</span>
      </label>
      <label class="advisor-choice-card">
        <input type="radio" name="sizePreference" value="COMPACT" ${sizePreference == 'COMPACT' ? 'checked' : ''}>
        <strong>Nhỏ gọn dễ cầm</strong>
        <span class="muted small">Màn hình gọn (~6.1–6.3"), thao tác thoải mái một tay.</span>
      </label>
      <label class="advisor-choice-card">
        <input type="radio" name="sizePreference" value="LARGE" ${sizePreference == 'LARGE' ? 'checked' : ''}>
        <strong>Màn hình lớn</strong>
        <span class="muted small">Từ 6.6" trở lên để xem phim, chơi game và làm việc.</span>
      </label>
    </div>
    <div class="field mt-3">
      <label for="choose-brand">Thương hiệu (tùy chọn)</label>
      <select id="choose-brand" name="brand" class="form-select">
        <option value="">Tất cả hãng</option>
        <c:forEach var="b" items="${brands}">
          <option value="<c:out value='${b}'/>" ${brand == b ? 'selected' : ''}><c:out value="${b}"/></option>
        </c:forEach>
      </select>
    </div>
    <div class="advisor-nav-actions">
      <button type="button" class="btn btn-light" data-advisor-next="3">← Quay lại</button>
      <button type="submit" class="btn btn-brand">Xem các lựa chọn phù hợp</button>
    </div>
  </div>
</form>

<c:if test="${not empty budget}">
  <div class="section-heading">
    <div>
      <span class="eyebrow">KẾT QUẢ GỢI Ý THEO CẤU HÌNH THỰC TẾ</span>
      <h2>Đề xuất trong ngân sách <ui:money value="${budget}"/></h2>
    </div>
    <a href="${pageContext.request.contextPath}/compare">Mở bảng so sánh →</a>
  </div>
  <c:choose>
    <c:when test="${not empty recommendations}">
      <div class="advisor-results-grid">
        <c:forEach var="rec" items="${recommendations}" varStatus="loop">
          <article class="panel advisor-result-card">
            <div class="advisor-result-head">
              <span class="status-badge">Phù hợp #${loop.index + 1}</span>
              <span class="muted small">Còn ${rec.recommendedVariant.stock} máy</span>
            </div>
            <div class="advisor-result-body">
              <a href="${pageContext.request.contextPath}/products/${rec.product.id}?variantId=${rec.recommendedVariant.id}">
                <img src="${pageContext.request.contextPath}${empty rec.recommendedVariant.image ? rec.product.image : rec.recommendedVariant.image}" alt="<c:out value='${rec.product.name}'/>" loading="lazy">
              </a>
              <div>
                <span class="eyebrow"><c:out value="${rec.product.brand}"/></span>
                <h3>
                  <a href="${pageContext.request.contextPath}/products/${rec.product.id}?variantId=${rec.recommendedVariant.id}">
                    <c:out value="${rec.product.name}"/>
                  </a>
                </h3>
                <p class="muted small mb-1">Phiên bản đề xuất: <strong><c:out value="${rec.recommendedVariant.label}"/></strong></p>
                <p class="price mb-2"><ui:money value="${rec.recommendedVariant.price}"/></p>
                <p class="mb-2"><strong><c:out value="${rec.headlineReason}"/></strong></p>
                <ul class="advisor-reason-list">
                  <c:forEach var="reason" items="${rec.reasons}">
                    <li><c:out value="${reason}"/></li>
                  </c:forEach>
                </ul>
              </div>
            </div>
            <div class="advisor-result-actions">
              <a class="btn btn-outline-dark btn-sm" href="${pageContext.request.contextPath}/products/${rec.product.id}?variantId=${rec.recommendedVariant.id}">Xem chi tiết</a>
              <form action="${pageContext.request.contextPath}/cart/add" method="post" class="d-inline">
                <ui:csrf/>
                <input type="hidden" name="variantId" value="${rec.recommendedVariant.id}">
                <input type="hidden" name="quantity" value="1">
                <button class="btn btn-brand btn-sm">Thêm vào giỏ hàng</button>
              </form>
              <form action="${pageContext.request.contextPath}/compare/add" method="post" class="d-inline" data-compare-form data-api-action="${pageContext.request.contextPath}/compare/api/add">
                <ui:csrf/>
                <input type="hidden" name="productId" value="${rec.product.id}">
                <input type="hidden" name="variantId" value="${rec.recommendedVariant.id}">
                <button class="btn btn-light btn-sm"><ui:icon name="compare"/> So sánh</button>
              </form>
            </div>
          </article>
        </c:forEach>
      </div>
    </c:when>
    <c:otherwise>
      <div class="empty-state">
        <h2>Chưa có phiên bản còn hàng phù hợp với ngân sách này</h2>
        <p>Thử tăng mức ngân sách hoặc bỏ giới hạn thương hiệu để xem thêm lựa chọn.</p>
        <a class="btn btn-brand" href="${pageContext.request.contextPath}/products?kind=PHONE&inStock=true">Xem tất cả điện thoại còn hàng</a>
      </div>
    </c:otherwise>
  </c:choose>
  <p class="muted small mt-4">Gợi ý được tính theo quy tắc đối chiếu trực tiếp dữ liệu giá bán, tồn kho, nhóm nhu cầu và thông số kỹ thuật hiện có trong hệ thống.</p>
</c:if>
<jsp:include page="/WEB-INF/views/shared/store/footer.jsp"/>
