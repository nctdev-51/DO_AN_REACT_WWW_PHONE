<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<jsp:include page="../components/header.jsp" />

<section class="container" style="padding-top:40px;padding-bottom:60px;max-width:850px;">
    <h1 style="font-weight:800;font-size:1.8rem;letter-spacing:-0.5px;margin-bottom:24px;">${empty productId ? 'Thêm sản phẩm' : 'Cập nhật sản phẩm'}</h1>

    <form:form modelAttribute="productForm" action="${pageContext.request.contextPath}/admin/products/save" method="post" enctype="multipart/form-data" cssStyle="background:#fff;border-radius:16px;padding:28px;box-shadow:0 4px 20px rgba(0,0,0,0.06);border:1px solid rgba(0,0,0,0.04);">
        <c:if test="${not empty productId}"><input type="hidden" name="id" value="${productId}"></c:if>

        <div style="margin-bottom:16px;">
            <label style="font-size:0.82rem;font-weight:600;color:#6b7280;margin-bottom:6px;display:block;" for="name">Tên sản phẩm</label>
            <form:input path="name" cssStyle="width:100%;border-radius:12px;border:1.5px solid #e5e7eb;padding:12px 16px;font-size:0.92rem;outline:none;transition:all 0.2s ease;" id="name" onfocus="this.style.borderColor='#667eea';this.style.boxShadow='0 0 0 3px rgba(102,126,234,0.1)'" onblur="this.style.borderColor='#e5e7eb';this.style.boxShadow='none'"/>
            <form:errors path="name" cssStyle="color:#ef4444;font-size:0.8rem;margin-top:4px;display:block;"/>
        </div>

        <div class="row">
            <div class="col-md-6" style="margin-bottom:16px;">
                <label style="font-size:0.82rem;font-weight:600;color:#6b7280;margin-bottom:6px;display:block;" for="brand">Hãng</label>
                <form:input path="brand" cssStyle="width:100%;border-radius:12px;border:1.5px solid #e5e7eb;padding:12px 16px;font-size:0.92rem;outline:none;" id="brand"/>
                <form:errors path="brand" cssStyle="color:#ef4444;font-size:0.8rem;margin-top:4px;display:block;"/>
            </div>
            <div class="col-md-6" style="margin-bottom:16px;">
                <label style="font-size:0.82rem;font-weight:600;color:#6b7280;margin-bottom:6px;display:block;" for="price">Giá (₫)</label>
                <form:input path="price" type="number" min="0.01" step="1" cssStyle="width:100%;border-radius:12px;border:1.5px solid #e5e7eb;padding:12px 16px;font-size:0.92rem;outline:none;" id="price"/>
                <form:errors path="price" cssStyle="color:#ef4444;font-size:0.8rem;margin-top:4px;display:block;"/>
            </div>
        </div>

        <div class="row">
            <div class="col-md-6" style="margin-bottom:16px;">
                <label style="font-size:0.82rem;font-weight:600;color:#6b7280;margin-bottom:6px;display:block;" for="categoryId">Danh mục</label>
                <form:select path="categoryId" cssStyle="width:100%;border-radius:12px;border:1.5px solid #e5e7eb;padding:12px 16px;font-size:0.92rem;" id="categoryId">
                    <form:option value="" label="-- Chọn danh mục --"/>
                    <form:options items="${categories}" itemValue="id" itemLabel="name"/>
                </form:select>
                <form:errors path="categoryId" cssStyle="color:#ef4444;font-size:0.8rem;margin-top:4px;display:block;"/>
            </div>
            <div class="col-md-6" style="margin-bottom:16px;">
                <label style="font-size:0.82rem;font-weight:600;color:#6b7280;margin-bottom:6px;display:block;" for="status">Trạng thái</label>
                <form:select path="status" cssStyle="width:100%;border-radius:12px;border:1.5px solid #e5e7eb;padding:12px 16px;font-size:0.92rem;" id="status">
                    <form:option value="1" label="Đang bán"/>
                    <form:option value="0" label="Ngừng kinh doanh"/>
                </form:select>
            </div>
        </div>

        <div style="margin-bottom:16px;">
            <label style="font-size:0.82rem;font-weight:600;color:#6b7280;margin-bottom:6px;display:block;" for="description">Mô tả</label>
            <form:textarea path="description" cssStyle="width:100%;border-radius:12px;border:1.5px solid #e5e7eb;padding:12px 16px;font-size:0.92rem;outline:none;resize:vertical;" id="description" rows="5"/>
        </div>

        <div style="margin-bottom:24px;">
            <label style="font-size:0.82rem;font-weight:600;color:#6b7280;margin-bottom:6px;display:block;" for="image">Ảnh sản phẩm (PNG/JPEG, tối đa 5 MB)</label>
            <input style="width:100%;border-radius:12px;border:1.5px solid #e5e7eb;padding:10px 16px;font-size:0.92rem;" type="file" id="image" name="image" accept="image/png,image/jpeg">
            <form:errors path="*" cssStyle="color:#ef4444;font-size:0.8rem;margin-top:4px;display:block;"/>
            <c:if test="${not empty image}"><img style="margin-top:12px;border-radius:12px;" src="${pageContext.request.contextPath}${image}" alt="Ảnh hiện tại" style="max-height:140px"></c:if>
        </div>

        <div class="d-flex gap-2">
            <button class="btn-gradient" type="submit" style="padding:12px 24px;font-size:0.92rem;">Lưu sản phẩm</button>
            <a style="padding:12px 24px;border-radius:999px;font-size:0.92rem;font-weight:600;color:#6b7280;border:1.5px solid #e5e7eb;text-decoration:none;transition:all 0.2s ease;" href="${pageContext.request.contextPath}/admin/products" onmouseover="this.style.borderColor='#667eea';this.style.color='#667eea'" onmouseout="this.style.borderColor='#e5e7eb';this.style.color='#6b7280'">Hủy</a>
        </div>
    </form:form>
</section>
<jsp:include page="../components/footer.jsp" />
