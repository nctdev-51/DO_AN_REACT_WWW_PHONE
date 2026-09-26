<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html>
<head>
    <title>Đặt hàng thành công</title>
    <style>
        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f6f9; text-align: center; padding-top: 50px; }
        .card { max-width: 550px; margin: 0 auto; background: white; padding: 40px; border-radius: 8px; box-shadow: 0 4px 15px rgba(0,0,0,0.08); }
        .icon { font-size: 50px; color: #28a745; margin-bottom: 10px; }
        h2 { color: #28a745; margin-top: 0; }
        .info { text-align: left; background: #f8f9fa; padding: 15px; border-radius: 6px; margin: 20px 0; }
        .btn-home { display: inline-block; padding: 10px 20px; background-color: #007bff; color: white; text-decoration: none; border-radius: 5px; font-weight: bold; }
    </style>
</head>
<body>

<div class="card">
    <div class="icon">✔</div>
    <h2>ĐẶT HÀNG THÀNH CÔNG!</h2>
    <p>Hệ thống đã ghi nhận đơn hàng và gửi email chi tiết đến quý khách.</p>

    <div class="info">
        <p>Mã đơn hàng: <b>#${order.id}</b></p>
        <p>Trạng thái: <b>${order.status}</b></p>
        <p>Số điện thoại nhận: <b>${order.phone}</b></p>
        <p>Địa chỉ giao hàng: <b>${order.shippingAddress}</b></p>
        <p>Tổng tiền: <b style="color: #d9534f;"><fmt:formatNumber value="${order.totalAmount}" type="currency" currencySymbol="₫"/></b></p>
    </div>

    <a href="${pageContext.request.contextPath}/" class="btn-home">← Quay lại trang chủ</a>
</div>

</body>
</html>