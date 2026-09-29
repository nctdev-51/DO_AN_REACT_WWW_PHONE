<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<jsp:include page="components/header.jsp" />

<section style="min-height:calc(100vh - 200px);display:flex;align-items:center;justify-content:center;padding:60px 0;background:var(--surface-bg);position:relative;overflow:hidden;">
    <!-- Decorative -->
    <div style="position:absolute;top:-150px;left:-150px;width:400px;height:400px;border-radius:50%;background:radial-gradient(circle,var(--brand-yellow-muted) 0%,transparent 70%);pointer-events:none;"></div>

    <div class="container" style="position:relative;z-index:2;">
        <div class="row justify-content-center">
            <div class="col-md-7 col-lg-6 col-xl-5">
                <div class="card-modern animate-fadeInUp" style="border-radius:24px;border:1px solid var(--border-primary);box-shadow:var(--shadow-xl);">
                    <!-- Card header -->
                    <div style="padding:40px 40px 0;text-align:center;">
                        <div style="width:70px;height:70px;border-radius:20px;background:var(--brand-yellow-muted);border:1px solid var(--brand-yellow-glow);display:flex;align-items:center;justify-content:center;margin:0 auto 20px;">
                            <i class="bi bi-person-plus-fill" style="font-size:2rem;color:var(--brand-yellow);"></i>
                        </div>
                        <h2 style="color:var(--text-primary);font-weight:900;font-size:1.8rem;margin-bottom:8px;letter-spacing:-0.5px;">Tạo Tài Khoản</h2>
                        <p style="color:var(--text-secondary);font-size:0.95rem;margin:0;">Đăng ký để mua sắm thuận tiện hơn</p>
                    </div>

                    <div style="padding:40px;">
                        <c:if test="${param.success != null}">
                            <div style="background:rgba(14,203,129,0.1);border:1px solid rgba(14,203,129,0.2);border-radius:12px;padding:14px 18px;margin-bottom:24px;color:var(--color-success);font-size:0.95rem;display:flex;align-items:center;gap:10px;">
                                <i class="bi bi-check-circle-fill"></i> 
                                <span>Đăng ký thành công! Vui lòng <a href="${pageContext.request.contextPath}/login" style="color:var(--brand-yellow);font-weight:700;text-decoration:none;">đăng nhập</a></span>
                            </div>
                        </c:if>

                        <form:form action="${pageContext.request.contextPath}/register" method="post" modelAttribute="user">
                            <div style="margin-bottom:20px;">
                                <label for="fullName" style="font-size:0.9rem;font-weight:600;color:var(--text-secondary);margin-bottom:8px;display:block;">Họ và tên</label>
                                <form:input path="fullName" id="fullName" placeholder="VD: Nguyễn Văn A" cssStyle="width:100%;background:var(--surface-primary);color:var(--text-primary);border-radius:12px;border:1.5px solid var(--border-primary);padding:14px 16px;font-size:1rem;transition:var(--transition-base);outline:none;" onfocus="this.style.borderColor='var(--brand-yellow)';this.style.boxShadow='0 0 0 3px var(--brand-yellow-muted)'" onblur="this.style.borderColor='var(--border-primary)';this.style.boxShadow='none'" />
                                <form:errors path="fullName" cssStyle="color:var(--color-danger);font-size:0.85rem;margin-top:6px;display:block;" />
                            </div>

                            <div style="margin-bottom:20px;">
                                <label for="email" style="font-size:0.9rem;font-weight:600;color:var(--text-secondary);margin-bottom:8px;display:block;">Email</label>
                                <form:input path="email" type="email" id="email" placeholder="email@example.com" cssStyle="width:100%;background:var(--surface-primary);color:var(--text-primary);border-radius:12px;border:1.5px solid var(--border-primary);padding:14px 16px;font-size:1rem;transition:var(--transition-base);outline:none;" onfocus="this.style.borderColor='var(--brand-yellow)';this.style.boxShadow='0 0 0 3px var(--brand-yellow-muted)'" onblur="this.style.borderColor='var(--border-primary)';this.style.boxShadow='none'" />
                                <form:errors path="email" cssStyle="color:var(--color-danger);font-size:0.85rem;margin-top:6px;display:block;" />
                            </div>

                            <div class="row g-3" style="margin-bottom:20px;">
                                <div class="col-sm-6">
                                    <label for="password" style="font-size:0.9rem;font-weight:600;color:var(--text-secondary);margin-bottom:8px;display:block;">Mật khẩu</label>
                                    <form:password path="password" id="password" placeholder="••••••••" cssStyle="width:100%;background:var(--surface-primary);color:var(--text-primary);border-radius:12px;border:1.5px solid var(--border-primary);padding:14px 16px;font-size:1rem;transition:var(--transition-base);outline:none;" onfocus="this.style.borderColor='var(--brand-yellow)';this.style.boxShadow='0 0 0 3px var(--brand-yellow-muted)'" onblur="this.style.borderColor='var(--border-primary)';this.style.boxShadow='none'" />
                                    <form:errors path="password" cssStyle="color:var(--color-danger);font-size:0.85rem;margin-top:6px;display:block;" />
                                </div>
                                <div class="col-sm-6">
                                    <label for="confirmPassword" style="font-size:0.9rem;font-weight:600;color:var(--text-secondary);margin-bottom:8px;display:block;">Xác nhận MK</label>
                                    <form:password path="confirmPassword" id="confirmPassword" placeholder="••••••••" cssStyle="width:100%;background:var(--surface-primary);color:var(--text-primary);border-radius:12px;border:1.5px solid var(--border-primary);padding:14px 16px;font-size:1rem;transition:var(--transition-base);outline:none;" onfocus="this.style.borderColor='var(--brand-yellow)';this.style.boxShadow='0 0 0 3px var(--brand-yellow-muted)'" onblur="this.style.borderColor='var(--border-primary)';this.style.boxShadow='none'" />
                                    <form:errors path="confirmPassword" cssStyle="color:var(--color-danger);font-size:0.85rem;margin-top:6px;display:block;" />
                                </div>
                            </div>

                            <div style="margin-bottom:20px;">
                                <label for="phone" style="font-size:0.9rem;font-weight:600;color:var(--text-secondary);margin-bottom:8px;display:block;">Số điện thoại</label>
                                <form:input path="phone" id="phone" placeholder="09xxxxxxxx" cssStyle="width:100%;background:var(--surface-primary);color:var(--text-primary);border-radius:12px;border:1.5px solid var(--border-primary);padding:14px 16px;font-size:1rem;transition:var(--transition-base);outline:none;" onfocus="this.style.borderColor='var(--brand-yellow)';this.style.boxShadow='0 0 0 3px var(--brand-yellow-muted)'" onblur="this.style.borderColor='var(--border-primary)';this.style.boxShadow='none'" />
                            </div>

                            <div style="margin-bottom:30px;">
                                <label for="address" style="font-size:0.9rem;font-weight:600;color:var(--text-secondary);margin-bottom:8px;display:block;">Địa chỉ</label>
                                <form:input path="address" id="address" placeholder="Số nhà, đường, quận, thành phố..." cssStyle="width:100%;background:var(--surface-primary);color:var(--text-primary);border-radius:12px;border:1.5px solid var(--border-primary);padding:14px 16px;font-size:1rem;transition:var(--transition-base);outline:none;" onfocus="this.style.borderColor='var(--brand-yellow)';this.style.boxShadow='0 0 0 3px var(--brand-yellow-muted)'" onblur="this.style.borderColor='var(--border-primary)';this.style.boxShadow='none'" />
                            </div>

                            <button type="submit" class="btn-gradient w-100" style="padding:16px;font-size:1.05rem;border-radius:12px;margin-bottom:24px;">
                                Đăng ký tài khoản
                            </button>
                        </form:form>

                        <div style="text-align:center;border-top:1px solid var(--border-primary);padding-top:24px;">
                            <p style="color:var(--text-secondary);font-size:0.95rem;margin:0;">Đã có tài khoản?
                                <a href="${pageContext.request.contextPath}/login" style="color:var(--brand-yellow);font-weight:700;text-decoration:none;margin-left:4px;" onmouseover="this.style.textDecoration='underline'" onmouseout="this.style.textDecoration='none'">Đăng nhập</a>
                            </p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</section>

<jsp:include page="components/footer.jsp" />
