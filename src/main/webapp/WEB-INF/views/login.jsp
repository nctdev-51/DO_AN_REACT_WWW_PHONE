<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="components/header.jsp" />

<section style="min-height:calc(100vh - 200px);display:flex;align-items:center;justify-content:center;padding:60px 0;background:var(--surface-bg);position:relative;overflow:hidden;">
    <!-- Decorative -->
    <div style="position:absolute;top:-150px;right:-150px;width:400px;height:400px;border-radius:50%;background:radial-gradient(circle,var(--brand-yellow-muted) 0%,transparent 70%);pointer-events:none;"></div>
    <div style="position:absolute;bottom:-100px;left:-100px;width:300px;height:300px;border-radius:50%;background:radial-gradient(circle,rgba(255,255,255,0.03) 0%,transparent 70%);pointer-events:none;"></div>

    <div class="container" style="position:relative;z-index:2;">
        <div class="row justify-content-center">
            <div class="col-md-6 col-lg-5 col-xl-4">
                <div class="card-modern animate-fadeInUp" style="border-radius:24px;border:1px solid var(--border-primary);box-shadow:var(--shadow-xl);">
                    <!-- Card header -->
                    <div style="padding:40px 40px 0;text-align:center;">
                        <div style="width:70px;height:70px;border-radius:20px;background:var(--brand-yellow-muted);border:1px solid var(--brand-yellow-glow);display:flex;align-items:center;justify-content:center;margin:0 auto 20px;">
                            <i class="bi bi-shield-lock-fill" style="font-size:2rem;color:var(--brand-yellow);"></i>
                        </div>
                        <h2 style="color:var(--text-primary);font-weight:900;font-size:1.8rem;margin-bottom:8px;letter-spacing:-0.5px;">Đăng nhập</h2>
                        <p style="color:var(--text-secondary);font-size:0.95rem;margin:0;">Chào mừng bạn quay trở lại PhoneStore</p>
                    </div>

                    <div style="padding:40px;">
                        <c:if test="${param.error != null}">
                            <div style="background:rgba(246,70,93,0.1);border:1px solid rgba(246,70,93,0.2);border-radius:12px;padding:14px 18px;margin-bottom:24px;color:var(--color-danger);font-size:0.9rem;display:flex;align-items:flex-start;gap:10px;">
                                <i class="bi bi-exclamation-triangle-fill" style="margin-top:2px;"></i>
                                <span>Email hoặc mật khẩu không chính xác, hoặc tài khoản đã bị khóa.</span>
                            </div>
                        </c:if>
                        <c:if test="${param.logout != null}">
                            <div style="background:rgba(14,203,129,0.1);border:1px solid rgba(14,203,129,0.2);border-radius:12px;padding:14px 18px;margin-bottom:24px;color:var(--color-success);font-size:0.9rem;display:flex;align-items:center;gap:10px;">
                                <i class="bi bi-check-circle-fill"></i> Đăng xuất thành công. Hẹn gặp lại!
                            </div>
                        </c:if>

                        <form action="${pageContext.request.contextPath}/login" method="post">
                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                            
                            <div style="margin-bottom:20px;">
                                <label for="username" style="font-size:0.9rem;font-weight:600;color:var(--text-secondary);margin-bottom:8px;display:block;">Email cá nhân</label>
                                <div style="position:relative;">
                                    <i class="bi bi-envelope" style="position:absolute;left:16px;top:50%;transform:translateY(-50%);color:var(--text-tertiary);font-size:1.1rem;"></i>
                                    <input type="email" id="username" name="username" placeholder="Nhập email của bạn" required
                                           style="width:100%;background:var(--surface-primary);color:var(--text-primary);border-radius:12px;border:1.5px solid var(--border-primary);padding:14px 16px 14px 45px;font-size:1rem;transition:var(--transition-base);outline:none;"
                                           onfocus="this.style.borderColor='var(--brand-yellow)';this.style.boxShadow='0 0 0 3px var(--brand-yellow-muted)'"
                                           onblur="this.style.borderColor='var(--border-primary)';this.style.boxShadow='none'" />
                                </div>
                            </div>
                            
                            <div style="margin-bottom:30px;">
                                <label for="password" style="font-size:0.9rem;font-weight:600;color:var(--text-secondary);margin-bottom:8px;display:block;">Mật khẩu</label>
                                <div style="position:relative;">
                                    <i class="bi bi-key" style="position:absolute;left:16px;top:50%;transform:translateY(-50%);color:var(--text-tertiary);font-size:1.2rem;"></i>
                                    <input type="password" id="password" name="password" placeholder="••••••••" required
                                           style="width:100%;background:var(--surface-primary);color:var(--text-primary);border-radius:12px;border:1.5px solid var(--border-primary);padding:14px 16px 14px 45px;font-size:1rem;transition:var(--transition-base);outline:none;"
                                           onfocus="this.style.borderColor='var(--brand-yellow)';this.style.boxShadow='0 0 0 3px var(--brand-yellow-muted)'"
                                           onblur="this.style.borderColor='var(--border-primary)';this.style.boxShadow='none'" />
                                </div>
                            </div>
                            
                            <button type="submit" class="btn-gradient w-100" style="padding:16px;font-size:1.05rem;border-radius:12px;margin-bottom:24px;">
                                Đăng nhập
                            </button>
                        </form>

                        <div style="text-align:center;border-top:1px solid var(--border-primary);padding-top:24px;">
                            <p style="color:var(--text-secondary);font-size:0.95rem;margin:0;">Chưa có tài khoản?
                                <a href="${pageContext.request.contextPath}/register" style="color:var(--brand-yellow);font-weight:700;text-decoration:none;margin-left:4px;" onmouseover="this.style.textDecoration='underline'" onmouseout="this.style.textDecoration='none'">Đăng ký ngay</a>
                            </p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</section>

<jsp:include page="components/footer.jsp" />
