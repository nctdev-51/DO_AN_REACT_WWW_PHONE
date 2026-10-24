<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<!doctype html>
<html lang="vi">
  <head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width,initial-scale=1">
    <title>
      <c:out value="${pageTitle}"/> — PTV Mobile Admin</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/vendor/bootstrap/bootstrap.min.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/store.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tokens.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/theme.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/motion.css"><link rel="stylesheet" href="${pageContext.request.contextPath}/css/retail.css">
      </head>
      <body class="admin-body">
        <aside class="admin-sidebar">
          <a class="brand" href="${pageContext.request.contextPath}/admin">PTV <span>Mobile</span>
          </a>
          <p class="eyebrow">QUẢN TRỊ CỬA HÀNG</p>
          <nav>
            <a class="${currentPage == 'dashboard' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin">Tổng quan</a>
            <a class="${currentPage == 'products' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/products">Sản phẩm & tồn kho</a>
            <a class="${currentPage == 'categories' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/categories">Danh mục</a>
            <a class="${currentPage == 'orders' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/orders">Đơn hàng</a>
            <a class="${currentPage == 'users' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/users">Người dùng</a>
            <a class="${currentPage == 'promotions' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/promotions">Khuyến mãi & giao hàng</a>
            <a class="${currentPage == 'support' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/support">Đổi trả & bảo hành</a>
            <a class="${currentPage == 'experience' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/experience">Nội dung & điểm thưởng</a>
            <a class="${currentPage == 'consultations' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/consultations">Tư vấn trả góp & thu cũ</a>
            <a class="${currentPage == 'banners' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/banners">Banner & chiến dịch</a>
          </nav>
          <form action="${pageContext.request.contextPath}/logout" method="post">
            <ui:csrf/>
            <button class="btn btn-outline-light w-100 mt-4">Đăng xuất</button>
          </form>
        </aside>
        <main class="admin-main">
          <div class="admin-topbar"><span>PTV Mobile <span aria-hidden="true">/</span> <strong>Quản trị cửa hàng</strong></span><a href="${pageContext.request.contextPath}/" target="_blank" rel="noopener">Xem cửa hàng <ui:icon name="arrow"/></a></div>
          <div class="page-heading">
            <div>
              <span class="eyebrow">KHÔNG GIAN QUẢN LÝ</span>
              <h1>
                <c:out value="${pageTitle}"/>
              </h1>
            </div>
          </div>
          <jsp:include page="/WEB-INF/views/shared/fragments/alerts.jsp"/>
