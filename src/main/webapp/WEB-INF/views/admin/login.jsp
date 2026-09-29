<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="../components/header.jsp" />

<section style="min-height:calc(100vh - 200px);display:flex;align-items:center;justify-content:center;padding:40px 0;background:linear-gradient(135deg,#f8f9fc 0%,#eef0f7 100%);position:relative;overflow:hidden;">
    <div style="position:absolute;top:-120px;right:-120px;width:350px;height:350px;border-radius:50%;background:radial-gradient(circle,rgba(102,126,234,0.08) 0%,transparent 70%);"></div>

    <div class="container" style="position:relative;z-index:2;">
        <div class="row justify-content-center">
            <div class="col-md-5 col-lg-4">
                <div class="animate-fadeInUp" style="background:#fff;border-radius:20px;box-shadow:0 20px 60px rgba(0,0,0,0.08);border:1px solid rgba(0,0,0,0.04);overflow:hidden;">
                    <div style="background:linear-gradient(135deg,#0f0f23,#1a1a3e);padding:32px;text-align:center;">
                        <div style="width:60px;height:60px;border-radius:16px;background:linear-gradient(135deg,#667eea,#764ba2);display:flex;align-items:center;justify-content:center;margin:0 auto 16px;box-shadow:0 8px 20px rgba(102,126,234,0.3);">
                            <i class="bi bi-shield-lock" style="font-size:1.6rem;color:#fff;"></i>
                        </div>
                        <h1 style="color:#fff;font-weight:800;font-size:1.3rem;margin-bottom:4px;">Đăng nhập quản trị</h1>
                        <p style="color:rgba(255,255,255,0.5);font-size:0.85rem;margin:0;">Đăng nhập để quản lý sản phẩm và danh mục.</p>
                    </div>

                    <div style="padding:28px 32px 32px;">
                        <c:if test="${param.error != null}">
                            <div style="background:rgba(239,68,68,0.08);border:1px solid rgba(239,68,68,0.15);border-radius:12px;padding:12px 16px;margin-bottom:20px;color:#ef4444;font-size:0.88rem;display:flex;align-items:center;gap:8px;">
                                <i class="bi bi-exclamation-circle"></i> Tên đăng nhập hoặc mật khẩu không đúng.
                            </div>
                        </c:if>
                        <c:if test="${param.logout != null}">
                            <div style="background:rgba(16,185,129,0.08);border:1px solid rgba(16,185,129,0.15);border-radius:12px;padding:12px 16px;margin-bottom:20px;color:#10b981;font-size:0.88rem;display:flex;align-items:center;gap:8px;">
                                <i class="bi bi-check-circle"></i> Bạn đã đăng xuất khỏi trang quản trị.
                            </div>
                        </c:if>

                        <form action="${pageContext.request.contextPath}/admin/login" method="post">
                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                            <div style="margin-bottom:18px;">
                                <label for="username" style="font-size:0.82rem;font-weight:600;color:#6b7280;margin-bottom:6px;display:block;">Tên đăng nhập</label>
                                <input type="text" id="username" name="username" autocomplete="username" required autofocus
                                       style="width:100%;border-radius:12px;border:1.5px solid #e5e7eb;padding:12px 16px;font-size:0.95rem;outline:none;transition:all 0.2s ease;"
                                       onfocus="this.style.borderColor='#667eea';this.style.boxShadow='0 0 0 3px rgba(102,126,234,0.1)'"
                                       onblur="this.style.borderColor='#e5e7eb';this.style.boxShadow='none'">
                            </div>
                            <div style="margin-bottom:24px;">
                                <label for="password" style="font-size:0.82rem;font-weight:600;color:#6b7280;margin-bottom:6px;display:block;">Mật khẩu</label>
                                <input type="password" id="password" name="password" autocomplete="current-password" required
                                       style="width:100%;border-radius:12px;border:1.5px solid #e5e7eb;padding:12px 16px;font-size:0.95rem;outline:none;transition:all 0.2s ease;"
                                       onfocus="this.style.borderColor='#667eea';this.style.boxShadow='0 0 0 3px rgba(102,126,234,0.1)'"
                                       onblur="this.style.borderColor='#e5e7eb';this.style.boxShadow='none'">
                            </div>
                            <button class="btn-gradient w-100" type="submit" style="padding:14px;font-size:1rem;">Đăng nhập</button>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>
</section>
<jsp:include page="../components/footer.jsp" />
