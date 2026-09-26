<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<jsp:include page="../components/header.jsp" />

<section class="container py-5" style="max-width: 850px">
    <h1 class="fw-bold mb-4">${empty productId ? 'Thêm sản phẩm' : 'Cập nhật sản phẩm'}</h1>
    <form:form modelAttribute="productForm" action="${pageContext.request.contextPath}/admin/products/save" method="post" enctype="multipart/form-data" cssClass="card card-body border-0 shadow-sm">
        <c:if test="${not empty productId}"><input type="hidden" name="id" value="${productId}"></c:if>
        <div class="mb-3"><label class="form-label" for="name">Tên sản phẩm</label><form:input path="name" cssClass="form-control" id="name"/><form:errors path="name" cssClass="text-danger small"/></div>
        <div class="row">
            <div class="col-md-6 mb-3"><label class="form-label" for="brand">Hãng</label><form:input path="brand" cssClass="form-control" id="brand"/><form:errors path="brand" cssClass="text-danger small"/></div>
            <div class="col-md-6 mb-3"><label class="form-label" for="price">Giá (₫)</label><form:input path="price" type="number" min="0.01" step="1" cssClass="form-control" id="price"/><form:errors path="price" cssClass="text-danger small"/></div>
        </div>
        <div class="row">
            <div class="col-md-6 mb-3">
                <label class="form-label" for="categoryId">Danh mục</label>
                <form:select path="categoryId" cssClass="form-select" id="categoryId">
                    <form:option value="" label="-- Chọn danh mục --"/>
                    <form:options items="${categories}" itemValue="id" itemLabel="name"/>
                </form:select>
                <form:errors path="categoryId" cssClass="text-danger small"/>
            </div>
            <div class="col-md-6 mb-3"><label class="form-label" for="status">Trạng thái</label><form:select path="status" cssClass="form-select" id="status"><form:option value="1" label="Đang bán"/><form:option value="0" label="Ngừng kinh doanh"/></form:select></div>
        </div>
        <div class="mb-3"><label class="form-label" for="description">Mô tả</label><form:textarea path="description" cssClass="form-control" id="description" rows="5"/></div>
        <div class="mb-3">
            <label class="form-label" for="image">Ảnh sản phẩm (PNG/JPEG, tối đa 5 MB)</label>
            <input class="form-control" type="file" id="image" name="image" accept="image/png,image/jpeg">
            <form:errors path="*" cssClass="text-danger small"/>
            <c:if test="${not empty image}"><img class="mt-3" src="${pageContext.request.contextPath}${image}" alt="Ảnh hiện tại" style="max-height: 140px"></c:if>
        </div>
        <div class="d-flex gap-2"><button class="btn btn-primary" type="submit">Lưu sản phẩm</button><a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/admin/products">Hủy</a></div>
    </form:form>
</section>
<jsp:include page="../components/footer.jsp" />
