<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html>
<head>
    <title>Xác nhận thanh toán - WebPhone</title>
    <style>
        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f6f9; padding: 25px; }
        .checkout-box { max-width: 600px; margin: 0 auto; background: white; padding: 30px; border-radius: 8px; box-shadow: 0 4px 15px rgba(0,0,0,0.08); }
        h2 { color: #333; margin-top: 0; text-align: center; }
        .form-group { margin-bottom: 18px; }
        label { display: block; font-weight: bold; margin-bottom: 6px; color: #495057; }
        input[type="text"], input[type="email"] { width: 100%; box-sizing: border-box; padding: 10px; border: 1px solid #ced4da; border-radius: 5px; font-size: 14px; }
        .order-summary { background-color: #e9f7ef; padding: 15px; border-radius: 6px; margin-bottom: 20px; }
        .btn-order { width: 100%; padding: 14px; background-color: #28a745; color: white; border: none; font-size: 16px; font-weight: bold; border-radius: 6px; cursor: pointer; }
        .btn-order:hover { background-color: #218838; }
    </style>
</head>
<body>

<div class="checkout-box">
    <h2>Thông Tin Thanh Toán</h2>

    <div class="order-summary">
        <div>Tổng số sản phẩm: <b>${cart.totalQuantity}</b> món</div>
        <div style="font-size: 18px; margin-top: 5px;">
            Tổng tiền: <b style="color: #d9534f;"><fmt:formatNumber value="${cart.totalAmount}" type="currency" currencySymbol="₫"/></b>
        </div>
    </div>

    <form action="${pageContext.request.contextPath}/checkout/place-order" method="POST">
        <c:choose>
            <c:when test="${not empty user}">
                <p>Khách hàng: <b>${user.fullName}</b></p>
                <div class="form-group">
                    <label>Địa chỉ nhận hàng (*):</label>
                    <input type="text" name="shippingAddress" value="${user.address}" required />
                </div>
                <div class="form-group">
                    <label>Số điện thoại liên lạc (*):</label>
                    <input type="text" name="phone" value="${user.phone}" required />
                </div>
            </c:when>
            <c:otherwise>
                <div class="form-group">
                    <label>Email nhận hóa đơn (*):</label>
                    <input type="email" name="email" placeholder="example@gmail.com" required />
                </div>
                <div class="form-group">
                    <label>Địa chỉ nhận hàng (*):</label>
                    <input type="text" name="shippingAddress" placeholder="Số nhà, tên đường, phường/xã, quận/huyện..." required />
                </div>
                <div class="form-group">
                    <label>Số điện thoại liên lạc (*):</label>
                    <input type="text" name="phone" placeholder="09xxxxxxxx" required />
                </div>
            </c:otherwise>
        </c:choose>

        <button type="submit" class="btn-order">Xác Nhận Đặt Hàng & Gửi Hóa Đơn</button>
    </form>
</div>

</body>
</html>