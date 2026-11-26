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
<<<<<<< HEAD
<<<<<<< HEAD
=======
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
>>>>>>> origin/taidev
=======
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
>>>>>>> origin/taidev
        <link rel="stylesheet" href="${pageContext.request.contextPath}/vendor/bootstrap/bootstrap.min.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/store.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tokens.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/theme.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/motion.css"><link rel="stylesheet" href="${pageContext.request.contextPath}/css/retail.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/vendor/inter/inter.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/components/admin-sidebar.css">
        <c:if test="${currentPage == 'analytics'}">
          <link rel="stylesheet" href="${pageContext.request.contextPath}/css/components/analytics.css">
        </c:if>
      </head>
      <body class="admin-body">
        <aside class="admin-sidebar">
          <a class="admin-sidebar-brand" href="${pageContext.request.contextPath}/admin">
            <span class="admin-sidebar-mark" aria-hidden="true">P</span>
            <span>PTV <strong>Mobile</strong></span>
          </a>
          <p class="admin-sidebar-label" id="admin-navigation-label">QUẢN TRỊ CỬA HÀNG</p>
          <nav class="admin-sidebar-nav" aria-labelledby="admin-navigation-label">
            <a href="${pageContext.request.contextPath}/admin" aria-current="${currentPage == 'dashboard' ? 'page' : 'false'}">
              <ui:adminMenuIcon name="layout-dashboard"/><span>Tổng quan</span>
            </a>
            <a href="${pageContext.request.contextPath}/admin/analytics" aria-current="${currentPage == 'analytics' ? 'page' : 'false'}">
              <ui:adminMenuIcon name="chart-no-axes-combined"/><span>Theo dõi &amp; thống kê</span>
            </a>
            <a href="${pageContext.request.contextPath}/admin/products" aria-current="${currentPage == 'products' ? 'page' : 'false'}">
              <ui:adminMenuIcon name="package"/><span>Sản phẩm &amp; tồn kho</span>
            </a>
            <a href="${pageContext.request.contextPath}/admin/categories" aria-current="${currentPage == 'categories' ? 'page' : 'false'}">
              <ui:adminMenuIcon name="layers"/><span>Danh mục</span>
            </a>
            <a href="${pageContext.request.contextPath}/admin/orders" aria-current="${currentPage == 'orders' ? 'page' : 'false'}">
              <ui:adminMenuIcon name="clipboard-list"/><span>Đơn hàng</span>
            </a>
            <a href="${pageContext.request.contextPath}/admin/users" aria-current="${currentPage == 'users' ? 'page' : 'false'}">
              <ui:adminMenuIcon name="users"/><span>Người dùng</span>
            </a>
            <a href="${pageContext.request.contextPath}/admin/promotions" aria-current="${currentPage == 'promotions' ? 'page' : 'false'}">
              <ui:adminMenuIcon name="tags"/><span>Khuyến mãi &amp; giao hàng</span>
            </a>
            <a href="${pageContext.request.contextPath}/admin/support" aria-current="${currentPage == 'support' ? 'page' : 'false'}">
              <ui:adminMenuIcon name="shield-check"/><span>Đổi trả &amp; bảo hành</span>
            </a>
            <a href="${pageContext.request.contextPath}/admin/experience" aria-current="${currentPage == 'experience' ? 'page' : 'false'}">
              <ui:adminMenuIcon name="sparkles"/><span>Nội dung &amp; điểm thưởng</span>
            </a>
            <a href="${pageContext.request.contextPath}/admin/consultations" aria-current="${currentPage == 'consultations' ? 'page' : 'false'}">
              <ui:adminMenuIcon name="handshake"/><span>Tư vấn trả góp &amp; thu cũ</span>
            </a>
            <a href="${pageContext.request.contextPath}/admin/banners" aria-current="${currentPage == 'banners' ? 'page' : 'false'}">
              <ui:adminMenuIcon name="megaphone"/><span>Banner &amp; chiến dịch</span>
            </a>
          </nav>
          <form class="admin-sidebar-footer" action="${pageContext.request.contextPath}/logout" method="post">
            <ui:csrf/>
            <button class="admin-sidebar-signout" type="submit">
              <ui:adminMenuIcon name="log-out"/><span>Đăng xuất</span>
            </button>
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
