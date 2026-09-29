<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<jsp:include page="components/header.jsp" />

<section class="container" style="padding-top:60px;padding-bottom:80px;">
    <div class="animate-fadeInUp">
        <!-- Page header -->
        <div class="d-flex align-items-center gap-4 mb-5">
            <div style="width:60px;height:60px;border-radius:16px;background:var(--brand-yellow-muted);display:flex;align-items:center;justify-content:center;box-shadow:var(--shadow-yellow);border:1px solid var(--brand-yellow-glow);">
                <i class="bi bi-bag-fill" style="color:var(--brand-yellow);font-size:1.5rem;"></i>
            </div>
            <div>
                <h2 style="font-weight:900;font-size:2rem;margin:0;letter-spacing:-0.5px;color:var(--text-primary);">Giỏ Hàng Của Bạn</h2>
                <p style="color:var(--text-secondary);margin:4px 0 0;font-size:0.95rem;">Kiểm tra các sản phẩm trước khi thanh toán</p>
            </div>
        </div>

        <c:choose>
            <c:when test="${empty cart.itemList}">
                <div class="card-modern" style="text-align:center;padding:100px 20px;border-radius:24px;">
                    <div style="width:100px;height:100px;border-radius:24px;background:var(--surface-primary);display:flex;align-items:center;justify-content:center;margin:0 auto 24px;border:1px solid var(--border-primary);">
                        <i class="bi bi-cart-x" style="font-size:3rem;color:var(--text-tertiary);"></i>
                    </div>
                    <h3 style="color:var(--text-primary);font-weight:800;margin-bottom:12px;">Giỏ hàng trống!</h3>
                    <p style="font-size:1.1rem;color:var(--text-secondary);margin-bottom:30px;">Bạn chưa chọn mua sản phẩm nào.</p>
                    <a href="${pageContext.request.contextPath}/" class="btn-gradient" style="padding:16px 32px;font-size:1.05rem;border-radius:12px;">
                        <i class="bi bi-arrow-left me-2"></i> Quay lại cửa hàng
                    </a>
                </div>
            </c:when>
            <c:otherwise>
                <div class="card-modern" style="border-radius:24px;">
                    <div class="table-responsive">
                        <table class="table align-middle mb-0" style="color:var(--text-primary);border-color:var(--border-primary);">
                            <thead>
                                <tr style="background:var(--surface-primary);">
                                    <th style="padding:20px 24px;font-size:0.85rem;font-weight:700;text-transform:uppercase;letter-spacing:1px;color:var(--text-secondary);border-bottom:1px solid var(--border-primary);">Sản phẩm</th>
                                    <th style="padding:20px;font-size:0.85rem;font-weight:700;text-transform:uppercase;letter-spacing:1px;color:var(--text-secondary);border-bottom:1px solid var(--border-primary);">Đơn giá</th>
                                    <th style="padding:20px;font-size:0.85rem;font-weight:700;text-transform:uppercase;letter-spacing:1px;color:var(--text-secondary);border-bottom:1px solid var(--border-primary);text-align:center;">Số lượng</th>
                                    <th style="padding:20px;font-size:0.85rem;font-weight:700;text-transform:uppercase;letter-spacing:1px;color:var(--text-secondary);border-bottom:1px solid var(--border-primary);">Thành tiền</th>
                                    <th style="padding:20px 24px;font-size:0.85rem;font-weight:700;text-transform:uppercase;letter-spacing:1px;color:var(--text-secondary);border-bottom:1px solid var(--border-primary);text-align:center;">Thao tác</th>
                                </tr>
                            </thead>
                            <tbody>
                            <c:forEach var="item" items="${cart.itemList}">
                                <tr style="border-bottom:1px solid var(--border-primary);transition:var(--transition-base);" onmouseover="this.style.background='var(--surface-primary)'" onmouseout="this.style.background='transparent'">
                                    <td style="padding:24px;border:none;">
                                        <div class="d-flex align-items-center gap-4">
                                            <c:if test="${not empty item.product.image}">
                                                <div style="width:80px;height:80px;border-radius:16px;background:var(--surface-primary);display:flex;align-items:center;justify-content:center;border:1px solid var(--border-primary);overflow:hidden;flex-shrink:0;">
                                                    <img src="${item.product.image}" style="width:60px;height:60px;object-fit:contain;" alt="phone"/>
                                                </div>
                                            </c:if>
                                            <b style="font-weight:700;font-size:1.1rem;">
                                                <a href="${pageContext.request.contextPath}/products/${item.product.id}" style="color:var(--text-primary);text-decoration:none;transition:var(--transition-base);" onmouseover="this.style.color='var(--brand-yellow)'" onmouseout="this.style.color='var(--text-primary)'">${item.product.name}</a>
                                            </b>
                                        </div>
                                    </td>
                                    <td style="padding:24px;border:none;font-size:1.05rem;font-weight:600;">
                                        <fmt:formatNumber value="${item.product.price}" type="currency" currencySymbol="₫"/>
                                    </td>
                                    <td style="padding:24px;border:none;text-align:center;">
                                        <form action="${pageContext.request.contextPath}/cart/update" method="POST" class="d-flex align-items-center justify-content-center gap-2" style="margin:0;">
                                            <input type="hidden" name="productId" value="${item.product.id}"/>
                                            <input type="number" name="quantity" value="${item.quantity}" min="1"
                                                   style="width:60px;padding:10px;text-align:center;background:var(--surface-primary);color:var(--text-primary);border:1.5px solid var(--border-primary);border-radius:12px;font-size:1rem;font-weight:700;outline:none;transition:all 0.2s ease;"
                                                   onfocus="this.style.borderColor='var(--brand-yellow)'" onblur="this.style.borderColor='var(--border-primary)'"/>
                                            <button type="submit" class="btn-ghost" style="padding:10px 16px;border-radius:12px;">Cập nhật</button>
                                        </form>
                                    </td>
                                    <td style="padding:24px;border:none;">
                                        <span class="price-tag" style="font-size:1.15rem;">
                                            <fmt:formatNumber value="${item.subTotal}" type="currency" currencySymbol="₫"/>
                                        </span>
                                    </td>
                                    <td style="padding:24px;border:none;text-align:center;">
                                        <a href="${pageContext.request.contextPath}/cart/remove/${item.product.id}"
                                           onclick="return confirm('Bạn có chắc chắn muốn bỏ sản phẩm này khỏi giỏ?');"
                                           style="display:inline-flex;align-items:center;gap:6px;padding:10px 16px;border-radius:12px;font-size:0.9rem;font-weight:600;color:var(--color-danger);background:rgba(246,70,93,0.1);border:1px solid rgba(246,70,93,0.2);text-decoration:none;transition:all 0.2s ease;"
                                           onmouseover="this.style.background='rgba(246,70,93,0.2)'" onmouseout="this.style.background='rgba(246,70,93,0.1)'">
                                            <i class="bi bi-trash3-fill"></i> Xóa
                                        </a>
                                    </td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>

                    <!-- Cart Footer -->
                    <div style="padding:32px 24px;background:var(--surface-primary);border-top:1px solid var(--border-primary);display:flex;justify-content:space-between;align-items:center;flex-wrap:wrap;gap:24px;">
                        <div class="d-flex gap-4 align-items-center flex-wrap">
                            <a href="${pageContext.request.contextPath}/cart/clear"
                               onclick="return confirm('Bạn muốn làm trống giỏ hàng?');"
                               style="color:var(--text-tertiary);font-weight:600;font-size:0.95rem;text-decoration:none;transition:var(--transition-base);"
                               onmouseover="this.style.color='var(--color-danger)'" onmouseout="this.style.color='var(--text-tertiary)'">
                                <i class="bi bi-x-circle me-1"></i> Xóa tất cả
                            </a>
                            <a href="${pageContext.request.contextPath}/products" class="btn-ghost" style="padding:10px 20px;border-radius:12px;">
                                <i class="bi bi-arrow-left me-1"></i> Tiếp tục mua sắm
                            </a>
                        </div>
                        <div class="d-flex align-items-center gap-4 flex-wrap" style="background:var(--surface-secondary);padding:20px 30px;border-radius:16px;border:1px solid var(--border-primary);">
                            <div>
                                <span style="color:var(--text-secondary);font-size:0.95rem;margin-bottom:4px;display:block;">Tổng thanh toán (${cart.totalQuantity} SP):</span>
                                <span class="price-tag" style="font-size:1.8rem;line-height:1;">
                                    <fmt:formatNumber value="${cart.totalAmount}" type="currency" currencySymbol="₫"/>
                                </span>
                            </div>
                            <a href="${pageContext.request.contextPath}/checkout" class="btn-gradient" style="padding:16px 32px;font-size:1.1rem;border-radius:12px;box-shadow:var(--shadow-yellow);">
                                Đặt hàng <i class="bi bi-arrow-right ms-2"></i>
                            </a>
                        </div>
                    </div>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</section>

<jsp:include page="components/footer.jsp" />