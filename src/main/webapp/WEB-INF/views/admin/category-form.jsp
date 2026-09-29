<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<jsp:include page="../components/header.jsp" />

<section class="container" style="padding-top:40px;padding-bottom:60px;max-width:750px;">
    <h1 style="font-weight:800;font-size:1.8rem;letter-spacing:-0.5px;margin-bottom:24px;">${empty categoryId ? 'Thêm danh mục' : 'Cập nhật danh mục'}</h1>

    <form:form modelAttribute="categoryForm" action="${pageContext.request.contextPath}/admin/categories/save" method="post" cssStyle="background:#fff;border-radius:16px;padding:28px;box-shadow:0 4px 20px rgba(0,0,0,0.06);border:1px solid rgba(0,0,0,0.04);">
        <c:if test="${not empty categoryId}"><input type="hidden" name="id" value="${categoryId}"></c:if>

        <div style="margin-bottom:16px;">
            <label style="font-size:0.82rem;font-weight:600;color:#6b7280;margin-bottom:6px;display:block;" for="name">Tên danh mục</label>
            <form:input path="name" cssStyle="width:100%;border-radius:12px;border:1.5px solid #e5e7eb;padding:12px 16px;font-size:0.92rem;outline:none;transition:all 0.2s ease;" id="name" onfocus="this.style.borderColor='#667eea';this.style.boxShadow='0 0 0 3px rgba(102,126,234,0.1)'" onblur="this.style.borderColor='#e5e7eb';this.style.boxShadow='none'"/>
            <form:errors path="name" cssStyle="color:#ef4444;font-size:0.8rem;margin-top:4px;display:block;"/>
        </div>

        <div style="margin-bottom:24px;">
            <label style="font-size:0.82rem;font-weight:600;color:#6b7280;margin-bottom:6px;display:block;" for="description">Mô tả</label>
            <form:textarea path="description" cssStyle="width:100%;border-radius:12px;border:1.5px solid #e5e7eb;padding:12px 16px;font-size:0.92rem;outline:none;resize:vertical;" id="description" rows="4"/>
        </div>

        <div class="d-flex gap-2">
            <button class="btn-gradient" type="submit" style="padding:12px 24px;font-size:0.92rem;">Lưu danh mục</button>
            <a style="padding:12px 24px;border-radius:999px;font-size:0.92rem;font-weight:600;color:#6b7280;border:1.5px solid #e5e7eb;text-decoration:none;transition:all 0.2s ease;" href="${pageContext.request.contextPath}/admin/categories" onmouseover="this.style.borderColor='#667eea';this.style.color='#667eea'" onmouseout="this.style.borderColor='#e5e7eb';this.style.color='#6b7280'">Hủy</a>
        </div>
    </form:form>
</section>
<jsp:include page="../components/footer.jsp" />
