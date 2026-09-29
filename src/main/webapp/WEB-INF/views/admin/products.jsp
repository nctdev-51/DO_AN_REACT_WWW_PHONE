<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<jsp:include page="../components/header.jsp" />

<section class="container" style="padding-top:40px;padding-bottom:60px;">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <div>
            <h1 style="font-weight:800;font-size:1.8rem;letter-spacing:-0.5px;margin-bottom:4px;">Quản lý sản phẩm</h1>
            <a href="${pageContext.request.contextPath}/admin/categories" style="color:#667eea;text-decoration:none;font-weight:600;font-size:0.88rem;">
                <i class="bi bi-tag me-1"></i>Quản lý danh mục
            </a>
        </div>
        <div class="d-flex gap-2">
            <form action="${pageContext.request.contextPath}/logout" method="post">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                <button style="padding:8px 18px;border-radius:10px;font-size:0.85rem;font-weight:600;color:#6b7280;border:1.5px solid #e5e7eb;background:#fff;cursor:pointer;transition:all 0.2s ease;" type="submit" onmouseover="this.style.borderColor='#ef4444';this.style.color='#ef4444'" onmouseout="this.style.borderColor='#e5e7eb';this.style.color='#6b7280'">Đăng xuất</button>
            </form>
            <a class="btn-gradient" style="padding:8px 18px;font-size:0.85rem;display:inline-flex;align-items:center;gap:6px;text-decoration:none;" href="${pageContext.request.contextPath}/admin/products/new"><i class="bi bi-plus-lg"></i>Thêm sản phẩm</a>
        </div>
    </div>

    <c:if test="${not empty message}">
        <div style="background:rgba(16,185,129,0.08);border:1px solid rgba(16,185,129,0.15);border-radius:12px;padding:14px 18px;margin-bottom:20px;color:#10b981;font-size:0.88rem;display:flex;align-items:center;gap:8px;">
            <i class="bi bi-check-circle"></i><c:out value="${message}"/>
        </div>
    </c:if>

    <div style="background:#fff;border-radius:16px;box-shadow:0 4px 20px rgba(0,0,0,0.06);border:1px solid rgba(0,0,0,0.04);overflow:hidden;">
        <table class="table align-middle mb-0">
            <thead>
                <tr style="background:#f8f9fc;">
                    <th style="padding:14px 20px;font-size:0.75rem;font-weight:700;text-transform:uppercase;letter-spacing:0.5px;color:#9ca3af;border:none;">Sản phẩm</th>
                    <th style="padding:14px 16px;font-size:0.75rem;font-weight:700;text-transform:uppercase;letter-spacing:0.5px;color:#9ca3af;border:none;">Hãng</th>
                    <th style="padding:14px 16px;font-size:0.75rem;font-weight:700;text-transform:uppercase;letter-spacing:0.5px;color:#9ca3af;border:none;">Danh mục</th>
                    <th style="padding:14px 16px;font-size:0.75rem;font-weight:700;text-transform:uppercase;letter-spacing:0.5px;color:#9ca3af;border:none;">Giá</th>
                    <th style="padding:14px 16px;font-size:0.75rem;font-weight:700;text-transform:uppercase;letter-spacing:0.5px;color:#9ca3af;border:none;">Trạng thái</th>
                    <th style="padding:14px 20px;font-size:0.75rem;font-weight:700;text-transform:uppercase;letter-spacing:0.5px;color:#9ca3af;border:none;text-align:right;">Thao tác</th>
                </tr>
            </thead>
            <tbody>
            <c:forEach items="${products}" var="product">
                <tr style="border-bottom:1px solid #f3f4f6;transition:all 0.2s ease;" onmouseover="this.style.background='#fafbfd'" onmouseout="this.style.background='transparent'">
                    <td style="padding:14px 20px;border:none;font-weight:600;font-size:0.9rem;"><c:out value="${product.name}"/></td>
                    <td style="padding:14px 16px;border:none;color:#6b7280;font-size:0.88rem;"><c:out value="${product.brand}"/></td>
                    <td style="padding:14px 16px;border:none;color:#6b7280;font-size:0.88rem;"><c:out value="${product.category.name}" default="—"/></td>
                    <td style="padding:14px 16px;border:none;font-weight:600;font-size:0.88rem;"><fmt:formatNumber value="${product.price}" pattern="#,##0"/>₫</td>
                    <td style="padding:14px 16px;border:none;">
                        <span style="${product.status == 1 ? 'background:rgba(16,185,129,0.1);color:#10b981;' : 'background:rgba(156,163,175,0.1);color:#9ca3af;'}padding:4px 12px;border-radius:999px;font-size:0.78rem;font-weight:600;">${product.status == 1 ? 'Đang bán' : 'Ngừng bán'}</span>
                    </td>
                    <td style="padding:14px 20px;border:none;text-align:right;white-space:nowrap;">
                        <a style="padding:6px 14px;border-radius:10px;font-size:0.8rem;font-weight:600;color:#667eea;background:rgba(102,126,234,0.08);text-decoration:none;display:inline-flex;align-items:center;gap:4px;transition:all 0.2s ease;margin-right:6px;" href="${pageContext.request.contextPath}/admin/products/${product.id}/edit" onmouseover="this.style.background='rgba(102,126,234,0.15)'" onmouseout="this.style.background='rgba(102,126,234,0.08)'">Sửa</a>
                        <form class="d-inline" action="${pageContext.request.contextPath}/admin/products/${product.id}/delete" method="post" onsubmit="return confirm('Xóa hoặc ngừng kinh doanh sản phẩm này?')">
                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                            <button style="padding:6px 14px;border-radius:10px;font-size:0.8rem;font-weight:600;color:#ef4444;background:rgba(239,68,68,0.08);border:none;cursor:pointer;transition:all 0.2s ease;" type="submit" onmouseover="this.style.background='rgba(239,68,68,0.15)'" onmouseout="this.style.background='rgba(239,68,68,0.08)'">Xóa</button>
                        </form>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty products}"><tr><td colspan="6" style="text-align:center;padding:40px;color:#9ca3af;border:none;">Chưa có sản phẩm.</td></tr></c:if>
            </tbody>
        </table>
    </div>
</section>
<jsp:include page="../components/footer.jsp" />
