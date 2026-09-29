<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<jsp:include page="components/header.jsp" />

<section style="min-height:calc(100vh - 200px);display:flex;align-items:center;justify-content:center;padding:60px 0;background:var(--surface-bg);position:relative;overflow:hidden;">
    <div style="position:absolute;top:50%;left:50%;transform:translate(-50%, -50%);width:600px;height:600px;border-radius:50%;background:radial-gradient(circle,rgba(14,203,129,0.08) 0%,transparent 60%);pointer-events:none;"></div>

    <div class="container" style="position:relative;z-index:2;">
        <div class="row justify-content-center">
            <div class="col-md-7 col-lg-5">
                <div class="card-modern animate-fadeInUp" style="border-radius:24px;border:1px solid var(--border-primary);box-shadow:var(--shadow-xl);text-align:center;overflow:hidden;">
                    <!-- Success header -->
                    <div style="background:rgba(14,203,129,0.1);padding:50px 40px;border-bottom:1px solid rgba(14,203,129,0.2);">
                        <div style="width:80px;height:80px;border-radius:50%;background:var(--color-success);display:flex;align-items:center;justify-content:center;margin:0 auto 20px;animation:pulse-glow-success 2s infinite;box-shadow:0 0 20px rgba(14,203,129,0.4);">
                            <i class="bi bi-check-lg" style="font-size:3rem;color:var(--text-dark);"></i>
                        </div>
                        <h2 style="color:var(--text-primary);font-weight:900;font-size:1.8rem;margin-bottom:8px;">ĐẶT HÀNG THÀNH CÔNG!</h2>
                        <p style="color:var(--color-success);font-size:1rem;margin:0;font-weight:500;">Hệ thống đã ghi nhận đơn hàng của quý khách.</p>
                    </div>

                    <div style="padding:40px;">
                        <!-- Order Info -->
                        <div style="background:var(--surface-primary);border-radius:16px;padding:24px;text-align:left;margin-bottom:32px;border:1px solid var(--border-primary);">
                            <div class="d-flex justify-content-between align-items-center" style="padding:12px 0;border-bottom:1px dashed var(--border-primary);">
                                <span style="color:var(--text-secondary);font-size:0.95rem;">Mã đơn hàng</span>
                                <span style="font-weight:800;color:var(--brand-yellow);font-size:1.1rem;">#${order.id}</span>
                            </div>
                            <div class="d-flex justify-content-between align-items-center" style="padding:12px 0;border-bottom:1px dashed var(--border-primary);">
                                <span style="color:var(--text-secondary);font-size:0.95rem;">Trạng thái</span>
                                <span style="background:rgba(240,185,11,0.15);color:var(--color-warning);padding:6px 14px;border-radius:999px;font-size:0.85rem;font-weight:700;">${order.status}</span>
                            </div>
                            <div class="d-flex justify-content-between align-items-center" style="padding:12px 0;border-bottom:1px dashed var(--border-primary);">
                                <span style="color:var(--text-secondary);font-size:0.95rem;">Số điện thoại nhận</span>
                                <span style="font-weight:600;color:var(--text-primary);">${order.phone}</span>
                            </div>
                            <div class="d-flex justify-content-between align-items-start" style="padding:12px 0;border-bottom:1px dashed var(--border-primary);">
                                <span style="color:var(--text-secondary);font-size:0.95rem;">Địa chỉ giao hàng</span>
                                <span style="font-weight:600;color:var(--text-primary);text-align:right;max-width:200px;word-wrap:break-word;">${order.shippingAddress}</span>
                            </div>
                            <div class="d-flex justify-content-between align-items-center" style="padding-top:16px;">
                                <span style="color:var(--text-secondary);font-size:0.95rem;">Tổng tiền thanh toán</span>
                                <span class="price-tag" style="font-size:1.5rem;"><fmt:formatNumber value="${order.totalAmount}" type="currency" currencySymbol="₫"/></span>
                            </div>
                        </div>

                        <a href="${pageContext.request.contextPath}/" class="btn-gradient w-100" style="padding:16px;font-size:1.05rem;border-radius:12px;">
                            <i class="bi bi-house-door-fill me-2"></i> Trở về Trang Chủ
                        </a>
                    </div>
                </div>
            </div>
        </div>
    </div>
</section>

<style>
    @keyframes pulse-glow-success {
        0%, 100% { box-shadow: 0 0 0 0 rgba(14, 203, 129, 0.5); }
        50% { box-shadow: 0 0 0 15px rgba(14, 203, 129, 0); }
    }
</style>

<jsp:include page="components/footer.jsp" />