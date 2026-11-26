<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="/WEB-INF/views/shared/store/header.jsp" />

<section class="panel text-center" style="max-width:680px;margin:2.5rem auto;">
  <img src="${pageContext.request.contextPath}/images/offline-placeholder.svg"
       alt="PTV Mobile Offline" width="120" height="120" class="mb-3">
  <span class="eyebrow">CHẾ ĐỘ NGOẠI TUYẾN</span>
  <h1 class="mt-1">Mất kết nối tới máy chủ PTV Mobile</h1>
  <p class="muted mt-2 mb-4">
    Trang bạn vừa mở chưa được lưu trong bộ nhớ đệm. Các thao tác đã thực hiện khi ngoại tuyến
    vẫn được giữ an toàn trong trình duyệt và sẽ tự động đồng bộ khi máy chủ trực tuyến trở lại.
  </p>
  <div class="d-flex flex-wrap justify-content-center gap-2">
    <button type="button" class="btn btn-brand" onclick="window.location.reload()">Thử kết nối lại</button>
    <a class="btn btn-outline-dark" href="${pageContext.request.contextPath}/products">Xem sản phẩm đã lưu</a>
    <a class="btn btn-light" href="${pageContext.request.contextPath}/">Về trang chủ</a>
  </div>
</section>

<jsp:include page="/WEB-INF/views/shared/store/footer.jsp" />
