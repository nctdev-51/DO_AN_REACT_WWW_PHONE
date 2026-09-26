<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html>
<head>
    <title>Giỏ hàng - WebPhone</title>
    <style>
        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f6f9; margin: 0; padding: 20px; }
        .cart-wrapper { max-width: 960px; margin: 0 auto; background: #fff; padding: 30px; border-radius: 8px; box-shadow: 0 4px 15px rgba(0,0,0,0.08); }
        h2 { color: #333; margin-top: 0; border-bottom: 2px solid #007bff; padding-bottom: 10px; }
        table { width: 100%; border-collapse: collapse; margin-top: 20px; }
        th, td { padding: 14px; text-align: left; border-bottom: 1px solid #e9ecef; }
        th { background-color: #f8f9fa; color: #495057; }
        .product-img { width: 60px; height: 60px; object-fit: contain; vertical-align: middle; margin-right: 10px; }
        .qty-input { width: 55px; padding: 6px; text-align: center; border: 1px solid #ced4da; border-radius: 4px; }
        .btn { padding: 7px 12px; border-radius: 4px; border: none; cursor: pointer; text-decoration: none; font-weight: bold; }
        .btn-update { background-color: #17a2b8; color: white; margin-left: 5px; }
        .btn-delete { background-color: #dc3545; color: white; font-size: 13px; }
        .cart-footer { margin-top: 25px; display: flex; justify-content: space-between; align-items: center; }
        .total-price { font-size: 20px; font-weight: bold; color: #d9534f; }
        .btn-checkout { background-color: #28a745; color: white; padding: 12px 25px; font-size: 16px; border-radius: 6px; }
        .btn-back { color: #007bff; text-decoration: none; font-weight: 500; }
    </style>
</head>
<body>

<div class="cart-wrapper">
    <h2>Giỏ Hàng Điện Thoại</h2>

    <c:choose>
        <c:when test="${empty cart.itemList}">
            <div style="text-align: center; padding: 40px 0;">
                <p style="font-size: 18px; color: #6c757d;">Giỏ hàng của bạn đang trống!</p>
                <a href="${pageContext.request.contextPath}/" class="btn-back">← Quay lại cửa hàng chọn điện thoại</a>
            </div>
        </c:when>
        <c:otherwise>
            <table>
                <thead>
                <tr>
                    <th>Sản phẩm</th>
                    <th>Đơn giá</th>
                    <th style="text-align: center;">Số lượng</th>
                    <th>Thành tiền</th>
                    <th>Hành động</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="item" items="${cart.itemList}">
                    <tr>
                        <td>
                            <c:if test="${not empty item.product.image}">
                                <img src="${item.product.image}" class="product-img" alt="phone"/>
                            </c:if>
                            <b>${item.product.name}</b>
                        </td>
                        <td><fmt:formatNumber value="${item.product.price}" type="currency" currencySymbol="₫"/></td>
                        <td style="text-align: center;">
                            <form action="${pageContext.request.contextPath}/cart/update" method="POST" style="margin: 0;">
                                <input type="hidden" name="productId" value="${item.product.id}"/>
                                <input type="number" name="quantity" value="${item.quantity}" min="1" class="qty-input"/>
                                <button type="submit" class="btn btn-update">Lưu</button>
                            </form>
                        </td>
                        <td style="color: #28a745; font-weight: bold;">
                            <fmt:formatNumber value="${item.subTotal}" type="currency" currencySymbol="₫"/>
                        </td>
                        <td>
                            <a href="${pageContext.request.contextPath}/cart/remove/${item.product.id}"
                               class="btn btn-delete"
                               onclick="return confirm('Bạn có chắc chắn muốn bỏ sản phẩm này?');">Xóa</a>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>

            <div class="cart-footer">
                <div>
                    <a href="${pageContext.request.contextPath}/cart/clear" class="btn btn-delete" onclick="return confirm('Bạn muốn làm trống giỏ hàng?');">Xóa hết giỏ</a>
                    <a href="${pageContext.request.contextPath}/" class="btn-back" style="margin-left: 15px;">← Tiếp tục mua sắm</a>
                </div>
                <div>
                    <span>Tổng tiền thanh toán: </span>
                    <span class="total-price"><fmt:formatNumber value="${cart.totalAmount}" type="currency" currencySymbol="₫"/></span>
                    <a href="${pageContext.request.contextPath}/checkout" class="btn btn-checkout" style="margin-left: 20px;">Thanh toán đơn hàng →</a>
                </div>
            </div>
        </c:otherwise>
    </c:choose>
</div>

</body>
</html>