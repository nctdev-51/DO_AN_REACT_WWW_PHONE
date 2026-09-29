<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<jsp:include page="components/header.jsp" />

<section style="min-height:calc(100vh - 200px);display:flex;align-items:center;justify-content:center;padding:60px 0;background:var(--surface-bg);position:relative;overflow:hidden;">
    <div style="position:absolute;top:-100px;right:-100px;width:300px;height:300px;border-radius:50%;background:radial-gradient(circle,rgba(252,213,53,0.05) 0%,transparent 70%);pointer-events:none;"></div>

    <div class="container" style="position:relative;z-index:2;">
        <div class="row justify-content-center">
            <div class="col-md-8 col-lg-6">
                <div class="card-modern animate-fadeInUp" style="border-radius:24px;border:1px solid var(--border-primary);box-shadow:var(--shadow-xl);">
                    <!-- Card header -->
                    <div style="padding:40px 40px 0;text-align:center;">
                        <div style="width:70px;height:70px;border-radius:20px;background:var(--brand-yellow-muted);border:1px solid var(--brand-yellow-glow);display:flex;align-items:center;justify-content:center;margin:0 auto 20px;">
                            <i class="bi bi-credit-card-fill" style="font-size:2rem;color:var(--brand-yellow);"></i>
                        </div>
                        <h2 style="color:var(--text-primary);font-weight:900;font-size:1.8rem;margin-bottom:0;">Thanh Toán Đơn Hàng</h2>
                    </div>

                    <div style="padding:40px;">
                        <!-- Order Summary -->
                        <div style="background:var(--surface-primary);border:1px solid var(--border-primary);border-radius:16px;padding:24px;margin-bottom:32px;">
                            <div class="d-flex justify-content-between align-items-center" style="margin-bottom:12px;">
                                <span style="color:var(--text-secondary);font-size:0.95rem;">Tổng số sản phẩm:</span>
                                <b style="color:var(--text-primary);font-size:1.05rem;">${cart.totalQuantity} món</b>
                            </div>
                            <div class="d-flex justify-content-between align-items-center">
                                <span style="color:var(--text-secondary);font-size:0.95rem;">Tổng tiền cần thanh toán:</span>
                                <span class="price-tag" style="font-size:1.5rem;"><fmt:formatNumber value="${cart.totalAmount}" type="currency" currencySymbol="₫"/></span>
                            </div>
                        </div>

                        <form action="${pageContext.request.contextPath}/checkout/place-order" method="POST">
                            <c:choose>
                                <c:when test="${not empty user}">
                                    <div style="display:flex;align-items:center;gap:12px;padding:16px 20px;background:rgba(14,203,129,0.1);border:1px solid rgba(14,203,129,0.2);border-radius:12px;margin-bottom:24px;font-size:0.95rem;">
                                        <i class="bi bi-person-check-fill" style="color:var(--color-success);font-size:1.2rem;"></i>
                                        <span style="color:var(--text-secondary);">Khách hàng: <b style="color:var(--text-primary);">${user.fullName}</b></span>
                                    </div>
                                    <div style="margin-bottom:20px;">
                                        <label style="font-size:0.9rem;font-weight:600;color:var(--text-secondary);margin-bottom:8px;display:block;">Địa chỉ nhận hàng (*)</label>
                                        <input type="text" name="shippingAddress" value="${user.address}" required
                                               style="width:100%;background:var(--surface-primary);color:var(--text-primary);border-radius:12px;border:1.5px solid var(--border-primary);padding:14px 16px;font-size:1rem;outline:none;transition:var(--transition-base);"
                                               onfocus="this.style.borderColor='var(--brand-yellow)';this.style.boxShadow='0 0 0 3px var(--brand-yellow-muted)'"
                                               onblur="this.style.borderColor='var(--border-primary)';this.style.boxShadow='none'" />
                                    </div>
                                    <div style="margin-bottom:30px;">
                                        <label style="font-size:0.9rem;font-weight:600;color:var(--text-secondary);margin-bottom:8px;display:block;">Số điện thoại liên lạc (*)</label>
                                        <input type="text" name="phone" value="${user.phone}" required
                                               style="width:100%;background:var(--surface-primary);color:var(--text-primary);border-radius:12px;border:1.5px solid var(--border-primary);padding:14px 16px;font-size:1rem;outline:none;transition:var(--transition-base);"
                                               onfocus="this.style.borderColor='var(--brand-yellow)';this.style.boxShadow='0 0 0 3px var(--brand-yellow-muted)'"
                                               onblur="this.style.borderColor='var(--border-primary)';this.style.boxShadow='none'" />
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <div style="margin-bottom:20px;">
                                        <label style="font-size:0.9rem;font-weight:600;color:var(--text-secondary);margin-bottom:8px;display:block;">Email nhận hóa đơn (*)</label>
                                        <input type="email" name="email" placeholder="example@gmail.com" required
                                               style="width:100%;background:var(--surface-primary);color:var(--text-primary);border-radius:12px;border:1.5px solid var(--border-primary);padding:14px 16px;font-size:1rem;outline:none;transition:var(--transition-base);"
                                               onfocus="this.style.borderColor='var(--brand-yellow)';this.style.boxShadow='0 0 0 3px var(--brand-yellow-muted)'"
                                               onblur="this.style.borderColor='var(--border-primary)';this.style.boxShadow='none'" />
                                    </div>
                                    <div style="margin-bottom:20px;">
                                        <label style="font-size:0.9rem;font-weight:600;color:var(--text-secondary);margin-bottom:8px;display:block;">Địa chỉ nhận hàng (*)</label>
                                        <input type="text" name="shippingAddress" placeholder="Số nhà, tên đường, phường/xã..." required
                                               style="width:100%;background:var(--surface-primary);color:var(--text-primary);border-radius:12px;border:1.5px solid var(--border-primary);padding:14px 16px;font-size:1rem;outline:none;transition:var(--transition-base);"
                                               onfocus="this.style.borderColor='var(--brand-yellow)';this.style.boxShadow='0 0 0 3px var(--brand-yellow-muted)'"
                                               onblur="this.style.borderColor='var(--border-primary)';this.style.boxShadow='none'" />
                                    </div>
                                    <div style="margin-bottom:30px;">
                                        <label style="font-size:0.9rem;font-weight:600;color:var(--text-secondary);margin-bottom:8px;display:block;">Số điện thoại liên lạc (*)</label>
                                        <input type="text" name="phone" placeholder="09xxxxxxxx" required
                                               style="width:100%;background:var(--surface-primary);color:var(--text-primary);border-radius:12px;border:1.5px solid var(--border-primary);padding:14px 16px;font-size:1rem;outline:none;transition:var(--transition-base);"
                                               onfocus="this.style.borderColor='var(--brand-yellow)';this.style.boxShadow='0 0 0 3px var(--brand-yellow-muted)'"
                                               onblur="this.style.borderColor='var(--border-primary)';this.style.boxShadow='none'" />
                                    </div>
                                </c:otherwise>
                            </c:choose>

                            <button type="submit" class="btn-gradient w-100" style="padding:16px;font-size:1.1rem;border-radius:12px;">
                                <i class="bi bi-bag-check-fill me-2"></i> Xác Nhận Đặt Hàng
                            </button>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>
</section>

<jsp:include page="components/footer.jsp" />