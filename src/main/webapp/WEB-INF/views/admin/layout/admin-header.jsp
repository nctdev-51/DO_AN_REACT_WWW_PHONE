<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Panel - PhoneStore</title>

    <!-- Google Fonts -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap" rel="stylesheet">

    <!-- Bootstrap 5 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">

    <!-- Bootstrap Icons -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">

    <style>
        :root {
            --sidebar-width: 240px;
            --topbar-height: 60px;
            --font-base: 'Plus Jakarta Sans', -apple-system, sans-serif;
            --sidebar-bg: #1a1d23;
            --sidebar-hover: #2c3040;
            --sidebar-active: #0d6efd;
        }

        * { box-sizing: border-box; }

        body {
            font-family: var(--font-base);
            background-color: #f0f2f5;
            margin: 0;
            padding: 0;
        }

        /* ===== SIDEBAR ===== */
        .admin-sidebar {
            position: fixed;
            top: 0;
            left: 0;
            width: var(--sidebar-width);
            height: 100vh;
            background-color: var(--sidebar-bg);
            display: flex;
            flex-direction: column;
            z-index: 1000;
            overflow-y: auto;
        }

        .sidebar-brand {
            display: flex;
            align-items: center;
            gap: 10px;
            padding: 18px 20px;
            border-bottom: 1px solid rgba(255,255,255,0.08);
            text-decoration: none;
        }

        .sidebar-brand span {
            font-size: 1.1rem;
            font-weight: 800;
            color: #fff;
            letter-spacing: -0.3px;
        }

        .sidebar-brand span em {
            color: #0d6efd;
            font-style: normal;
        }

        .sidebar-menu {
            list-style: none;
            padding: 12px 0;
            margin: 0;
            flex: 1;
        }

        .sidebar-menu .menu-label {
            font-size: 0.7rem;
            font-weight: 700;
            text-transform: uppercase;
            letter-spacing: 1px;
            color: rgba(255,255,255,0.3);
            padding: 16px 20px 6px;
        }

        .sidebar-menu a {
            display: flex;
            align-items: center;
            gap: 10px;
            padding: 10px 20px;
            color: rgba(255,255,255,0.65);
            text-decoration: none;
            font-size: 0.9rem;
            font-weight: 500;
            border-left: 3px solid transparent;
            transition: all 0.2s ease;
        }

        .sidebar-menu a:hover {
            background-color: var(--sidebar-hover);
            color: #fff;
            border-left-color: rgba(255,255,255,0.2);
        }

        .sidebar-menu a.active {
            background-color: rgba(13, 110, 253, 0.15);
            color: #fff;
            border-left-color: var(--sidebar-active);
            font-weight: 600;
        }

        .sidebar-menu a i {
            font-size: 1.1rem;
            width: 20px;
            text-align: center;
        }

        .sidebar-footer {
            padding: 16px 20px;
            border-top: 1px solid rgba(255,255,255,0.08);
        }

        .sidebar-footer a {
            display: flex;
            align-items: center;
            gap: 10px;
            color: rgba(255,255,255,0.5);
            text-decoration: none;
            font-size: 0.85rem;
            font-weight: 500;
            transition: color 0.2s ease;
        }

        .sidebar-footer a:hover {
            color: #ff6b6b;
        }

        /* ===== TOPBAR ===== */
        .admin-topbar {
            position: fixed;
            top: 0;
            left: var(--sidebar-width);
            right: 0;
            height: var(--topbar-height);
            background-color: #fff;
            border-bottom: 1px solid #e9ecef;
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 0 24px;
            z-index: 999;
            box-shadow: 0 1px 4px rgba(0,0,0,0.06);
        }

        .topbar-title {
            font-size: 1rem;
            font-weight: 700;
            color: #212529;
        }

        .topbar-title span {
            color: #6c757d;
            font-weight: 400;
            font-size: 0.85rem;
            margin-left: 8px;
        }

        .topbar-right {
            display: flex;
            align-items: center;
            gap: 16px;
        }

        .topbar-admin-badge {
            display: flex;
            align-items: center;
            gap: 8px;
            font-size: 0.85rem;
            font-weight: 600;
            color: #495057;
        }

        .topbar-admin-badge .avatar {
            width: 34px;
            height: 34px;
            border-radius: 50%;
            background: linear-gradient(135deg, #0d6efd, #6610f2);
            display: flex;
            align-items: center;
            justify-content: center;
            color: white;
            font-size: 0.8rem;
            font-weight: 700;
        }

        /* ===== MAIN CONTENT ===== */
        .admin-main {
            margin-left: var(--sidebar-width);
            padding-top: var(--topbar-height);
            min-height: 100vh;
        }

        .admin-content {
            padding: 28px 28px;
        }

        /* ===== RESPONSIVE ===== */
        @media (max-width: 768px) {
            .admin-sidebar {
                transform: translateX(-100%);
                transition: transform 0.3s ease;
            }
            .admin-sidebar.show {
                transform: translateX(0);
            }
            .admin-topbar,
            .admin-main {
                left: 0;
                margin-left: 0;
            }
        }
    </style>
</head>
<body>

<!-- ===== SIDEBAR ===== -->
<aside class="admin-sidebar">
    <!-- Brand -->
    <a class="sidebar-brand" href="${pageContext.request.contextPath}/admin/">
        <i class="bi bi-phone text-primary fs-4"></i>
        <span>Phone<em>Store</em> <small class="text-white-50 fw-normal" style="font-size:0.7rem;">Admin</small></span>
    </a>

    <!-- Menu -->
    <ul class="sidebar-menu">
        <li class="menu-label">Tổng quan</li>
        <li>
            <a href="${pageContext.request.contextPath}/admin/" class="${currentPage == 'dashboard' ? 'active' : ''}">
                <i class="bi bi-speedometer2"></i>
                Dashboard
            </a>
        </li>

        <li class="menu-label">Quản lý</li>
        <li>
            <a href="${pageContext.request.contextPath}/admin/orders" class="${currentPage == 'orders' ? 'active' : ''}">
                <i class="bi bi-bag-check"></i>
                Đơn hàng
            </a>
        </li>
        <li>
            <a href="${pageContext.request.contextPath}/admin/users" class="${currentPage == 'users' ? 'active' : ''}">
                <i class="bi bi-people"></i>
                Người dùng
            </a>
        </li>
        <li>
            <a href="${pageContext.request.contextPath}/admin/products" class="${currentPage == 'products' ? 'active' : ''}">
                <i class="bi bi-phone"></i>
                Sản phẩm
            </a>
        </li>

        <li class="menu-label">Khác</li>
        <li>
            <a href="${pageContext.request.contextPath}/" target="_blank">
                <i class="bi bi-box-arrow-up-right"></i>
                Xem website
            </a>
        </li>
    </ul>

    <!-- Sidebar Footer -->
    <div class="sidebar-footer">
        <a href="${pageContext.request.contextPath}/logout">
            <i class="bi bi-box-arrow-left"></i>
            Đăng xuất
        </a>
    </div>
</aside>

<!-- ===== TOPBAR ===== -->
<div class="admin-topbar">
    <div class="topbar-title">
        ${pageTitle != null ? pageTitle : 'Admin Panel'}
        <span>/ PhoneStore</span>
    </div>
    <div class="topbar-right">
        <!-- Notification bell (placeholder) -->
        <button class="btn btn-light btn-sm position-relative rounded-circle p-2" style="width:36px;height:36px;">
            <i class="bi bi-bell" style="font-size:0.9rem;"></i>
        </button>
        <!-- Admin badge -->
        <div class="topbar-admin-badge">
            <div class="avatar">A</div>
            <span>Admin</span>
        </div>
    </div>
</div>

<!-- ===== MAIN CONTENT START ===== -->
<div class="admin-main">
    <div class="admin-content">
