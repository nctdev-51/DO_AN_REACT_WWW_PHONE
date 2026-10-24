<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<!doctype html>
<html lang="vi">
  <head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width,initial-scale=1">
    <title>PTV Mobile — Điện thoại & công nghệ</title>
    <meta name="theme-color" content="#ffffff">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/vendor/bootstrap/bootstrap.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/store.css">
    <c:if test="${comparison != null}"><link rel="stylesheet" href="${pageContext.request.contextPath}/css/comparison.css"></c:if>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/tokens.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/theme.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/showcase.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/motion.css"><link rel="stylesheet" href="${pageContext.request.contextPath}/css/retail.css">
  </head>
  <body class="storefront">
    <a class="skip-link" href="#main-content">Đến nội dung chính</a>
    <div class="announcement"><div class="container announcement-inner"><span>Điện thoại & công nghệ</span><a href="${pageContext.request.contextPath}/orders">Theo dõi đơn hàng <span aria-hidden="true">↗</span></a></div></div>
    <header class="store-header">
      <div class="container header-inner">
        <a class="brand" href="${pageContext.request.contextPath}/">PTV <span>Mobile</span>
        </a>
        <form action="${pageContext.request.contextPath}/products" method="get" class="header-search">
          <input name="keyword" data-search-input autocomplete="off" placeholder="Tìm điện thoại, thương hiệu..." aria-label="Tìm kiếm" value="<c:out value='${keyword}'/>">
          <div class="search-suggestions" data-search-suggestions hidden></div>
          <button type="submit" aria-label="Tìm kiếm"><ui:icon name="search"/></button>
        </form>
        <nav class="store-nav" aria-label="Điều hướng chính">
          <a href="${pageContext.request.contextPath}/products">Sản phẩm</a>
          <a href="${pageContext.request.contextPath}/offers">Ưu đãi</a>
          <a href="${pageContext.request.contextPath}/compare"><ui:icon name="compare"/> So sánh <span class="count-badge">${empty sessionScope.comparisonIds ? 0 : sessionScope.comparisonIds.size()}</span></a>
          <a href="${pageContext.request.contextPath}/cart"><ui:icon name="cart"/> Giỏ hàng <span class="count-badge">${empty sessionScope.cart ? 0 : sessionScope.cart.totalQuantity}</span>
          </a>
          <c:choose>
            <c:when test="${pageContext.request.userPrincipal != null}">
              <div class="dropdown">
                <button class="nav-account dropdown-toggle" data-bs-toggle="dropdown" type="button" aria-expanded="false"><ui:icon name="user"/> Tài khoản</button>
                <ul class="dropdown-menu dropdown-menu-end">
                  <li>
                    <a class="dropdown-item" href="${pageContext.request.contextPath}/account">Thông tin tài khoản</a>
                  </li>
                  <li>
                    <a class="dropdown-item" href="${pageContext.request.contextPath}/account/addresses">Sổ địa chỉ</a>
                  </li>
                  <li><a class="dropdown-item" href="${pageContext.request.contextPath}/account/wishlist">Sản phẩm yêu thích</a></li>
                  <li><a class="dropdown-item" href="${pageContext.request.contextPath}/account/points">Điểm thưởng</a></li><li><a class="dropdown-item" href="${pageContext.request.contextPath}/account/consultations">Trả góp & thu cũ</a></li><li><a class="dropdown-item" href="${pageContext.request.contextPath}/recently-viewed">Đã xem gần đây</a></li><li><a class="dropdown-item" href="${pageContext.request.contextPath}/account/support">Đổi trả & bảo hành</a></li>
                  <li>
                    <a class="dropdown-item" href="${pageContext.request.contextPath}/orders">Đơn hàng của tôi</a>
                  </li>
                  <c:if test="${pageContext.request.isUserInRole('ADMIN')}">
                    <li>
                      <a class="dropdown-item" href="${pageContext.request.contextPath}/admin">Quản trị cửa hàng</a>
                    </li>
                  </c:if>
                  <li>
                    <hr class="dropdown-divider">
                  </li>
                  <li>
                    <form action="${pageContext.request.contextPath}/logout" method="post">
                      <ui:csrf/>
                      <button class="dropdown-item text-danger">Đăng xuất</button>
                    </form>
                  </li>
                </ul>
              </div>
            </c:when>
            <c:otherwise>
              <a href="${pageContext.request.contextPath}/login"><ui:icon name="user"/> Đăng nhập</a>
            </c:otherwise>
          </c:choose>
        </nav>
      </div>
    </header>
    <main class="container store-main" id="main-content">
      <jsp:include page="/WEB-INF/views/shared/fragments/alerts.jsp" />
