<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
            <%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
                <%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
                    <jsp:include page="/WEB-INF/views/shared/admin/header.jsp" />
                    <section class="panel form-panel">
                        <h2>${empty bannerId ? 'Thêm chiến dịch' : 'Sửa chiến dịch'}</h2>
                        <c:if test="${not empty draftNote}">
                            <p class="alert alert-info">
                                <c:out value="${draftNote}" /> Chọn “Cho phép hiển thị theo lịch” nếu muốn phát banner
                                sau khi lưu.
                            </p>
                        </c:if>
                        <form:form modelAttribute="bannerForm"
                            action="${pageContext.request.contextPath}/admin/banners/save" method="post"
                            enctype="multipart/form-data">
                            <form:errors path="*" element="div" cssClass="alert alert-danger" /><input type="hidden"
                                name="id" value="${bannerId}">
                            <form:hidden path="version" />
                            <div class="field"><label for="title">Tiêu đề</label>
                                <form:input path="title" id="title" cssClass="form-control" maxlength="120" />
                            </div>
                            <div class="field"><label for="subtitle">Mô tả ngắn</label>
                                <form:input path="subtitle" id="subtitle" cssClass="form-control" maxlength="240" />
                            </div>
                            <div class="field"><label for="actionLabel">Nhãn nút hành động</label>
                                <form:input path="actionLabel" id="actionLabel" cssClass="form-control"
                                    maxlength="40" />
                            </div>
                            <div class="field"><label for="imageAlt">Mô tả ảnh dành cho trình đọc màn hình</label>
                                <form:input path="imageAlt" id="imageAlt" cssClass="form-control" maxlength="160" />
                            </div>
                            <div class="field"><label for="target">Đích đến</label>
                                <form:select path="target" id="target" cssClass="form-select">
                                    <form:option value="PRODUCT">Chi tiết sản phẩm</form:option>
                                    <form:option value="OFFERS">Trang ưu đãi</form:option>
                                    <form:option value="CHOOSE">Chọn theo nhu cầu</form:option>
                                    <form:option value="SEARCH">Kết quả tìm kiếm</form:option>
                                </form:select>
                            </div>
                            <div class="field"><label for="searchKeyword">Từ khóa đích (khi chọn kết quả tìm
                                    kiếm)</label>
                                <form:input path="searchKeyword" id="searchKeyword" cssClass="form-control"
                                    maxlength="120" />
                            </div>
                            <div class="field"><label for="placement">Vị trí</label>
                                <form:select path="placement" id="placement" cssClass="form-select">
                                    <form:option value="HERO">Khối giới thiệu chính</form:option>
                                    <form:option value="TILE">Thẻ chiến dịch</form:option>
                                </form:select>
                            </div>
                            <div class="field"><label for="theme">Màu nền</label>
                                <form:select path="theme" id="theme" cssClass="form-select">
                                    <form:option value="LIGHT">Sáng</form:option>
                                    <form:option value="DARK">Tối</form:option>
                                </form:select>
                            </div>
                            <div class="field"><label for="productId">Sản phẩm (bắt buộc nếu đích đến là chi tiết sản
                                    phẩm)</label>
                                <form:select path="productId" id="productId" cssClass="form-select">
                                    <form:option value="">Không chọn</form:option>
                                    <c:forEach var="p" items="${bannerProducts}">
                                        <form:option value="${p.id}">
                                            <c:out value="${p.name}" />
                                        </form:option>
                                    </c:forEach>
                                </form:select>
                            </div>
                            <div class="field"><label for="banner-image">Ảnh banner PNG/JPEG, tối đa 5 MB</label><input
                                    id="banner-image" type="file" name="image" accept="image/png,image/jpeg"
                                    class="form-control" onchange="previewBannerImage(this)"><small
                                    class="d-block mt-1 text-muted">Không tải ảnh riêng sẽ dùng ảnh sản phẩm được chọn.
                                    Để trống khi sửa để giữ ảnh cũ.</small>
                                <div id="banner-live-preview-wrap" class="mt-2">
                                    <c:if test="${not empty bannerImage}">
                                        <c:set var="resolvedBannerImg"
                                            value="${fn:startsWith(bannerImage, 'http') ? bannerImage : pageContext.request.contextPath.concat(bannerImage)}" />
                                        <img id="banner-preview-img" class="image-preview" src="${resolvedBannerImg}"
                                            alt="Ảnh hiện tại"
                                            style="max-height:160px; object-fit:contain; border-radius:8px;">
                                    </c:if><img id="banner-new-preview" class="image-preview d-none"
                                        alt="Xem trước ảnh mới"
                                        style="max-height:160px; object-fit:contain; border-radius:8px;">
                                </div>
                            </div>
                            <script>function previewBannerImage(input) { var p = document.getElementById('banner-new-preview'); var old = document.getElementById('banner-preview-img'); if (input.files && input.files[0]) { var r = new FileReader(); r.onload = function (e) { p.src = e.target.result; p.classList.remove('d-none'); if (old) old.classList.add('d-none'); }; r.readAsDataURL(input.files[0]); } }</script>
                            <div class="field"><label for="startsAt">Bắt đầu (giờ Việt Nam)</label>
                                <form:input path="startsAt" id="startsAt" type="datetime-local" cssClass="form-control"
                                    required="required" />
                            </div>
                            <div class="field"><label for="endsAt">Kết thúc (giờ Việt Nam)</label>
                                <form:input path="endsAt" id="endsAt" type="datetime-local" cssClass="form-control"
                                    required="required" />
                            </div>
                            <div class="field"><label for="position">Thứ tự hiển thị</label>
                                <form:input path="position" id="position" type="number" min="0" max="999"
                                    required="required" cssClass="form-control" />
                            </div>
                            <div class="mt-3"><label class="payment-choice">
                                    <form:checkbox path="active" /> Cho phép hiển thị theo lịch
                                </label></div>
                            <div class="d-flex flex-wrap gap-2 mt-3"><button class="btn btn-brand">Lưu chiến
                                    dịch</button><a class="btn btn-light"
                                    href="${pageContext.request.contextPath}/admin/banners">Quay lại danh sách</a></div>
                        </form:form>
                    </section>
                    <jsp:include page="/WEB-INF/views/shared/admin/footer.jsp" />