<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<div class="product-gallery panel" data-gallery data-endpoint="${pageContext.request.contextPath}/products/${product.id}/media" data-context="${pageContext.request.contextPath}">
  <div class="gallery-tabs" role="group" aria-label="Chế độ xem sản phẩm">
    <button type="button" data-gallery-photos aria-pressed="true">Hình ảnh</button>
    <button type="button" data-gallery-spin aria-pressed="false" hidden>Xem 360°</button>
  </div>
  <div data-photo-panel>
    <div class="gallery-stage" data-reveal>
      <button type="button" class="gallery-main" data-gallery-zoom aria-label="Phóng to ảnh sản phẩm"><img data-gallery-image src="${pageContext.request.contextPath}${product.image}" alt="<c:out value='${product.name}'/>" fetchpriority="high"></button>
      <button type="button" class="gallery-arrow gallery-prev" data-gallery-prev aria-label="Ảnh trước">‹</button>
      <button type="button" class="gallery-arrow gallery-next" data-gallery-next aria-label="Ảnh tiếp theo">›</button>
      <span class="gallery-counter" data-gallery-counter>1 / 1</span>
    </div>
    <p class="gallery-caption" data-gallery-caption aria-live="polite">Ảnh giới thiệu sản phẩm</p>
    <div class="gallery-thumbnails" data-gallery-thumbs role="group" aria-label="Các góc nhìn sản phẩm"></div>
    <p class="small muted" data-gallery-notice></p>
  </div>
  <div data-spin-panel hidden>
    <div class="spin-stage" data-spin-stage tabindex="0" role="group" aria-label="Ảnh xoay sản phẩm. Dùng phím mũi tên trái hoặc phải để xoay."><img data-spin-image alt="Ảnh xoay sản phẩm" hidden></div>
    <div class="spin-controls" data-spin-controls hidden><button type="button" class="btn btn-sm btn-light" data-spin-play aria-pressed="false">Tự xoay</button><input data-spin-range type="range" min="0" value="0" aria-label="Góc xoay sản phẩm"></div>
    <p class="small muted" data-spin-help></p>
    <p class="small muted" data-model-credit></p>
  </div>
  <dialog class="gallery-dialog" data-gallery-dialog aria-label="Ảnh sản phẩm phóng to">
    <button type="button" class="gallery-close" data-gallery-close aria-label="Đóng ảnh phóng to">×</button>
    <img data-gallery-large alt="Ảnh sản phẩm phóng to">
    <div class="gallery-dialog-controls"><button type="button" class="btn btn-light" data-gallery-prev aria-label="Ảnh trước">←</button><span data-gallery-large-caption></span><button type="button" class="btn btn-light" data-gallery-next aria-label="Ảnh tiếp theo">→</button></div>
  </dialog>
</div>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/gallery.css">
<script src="${pageContext.request.contextPath}/js/gallery.js" defer></script>
