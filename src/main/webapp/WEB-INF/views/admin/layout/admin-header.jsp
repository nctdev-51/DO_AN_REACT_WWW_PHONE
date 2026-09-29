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
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800;900&display=swap" rel="stylesheet">

    <!-- Bootstrap 5 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">

    <!-- Bootstrap Icons -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">

    <style>
        :root {
            --sidebar-width: 260px;
            --topbar-height: 64px;
            --font-base: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
            --sidebar-bg: linear-gradient(180deg, #0f0f23 0%, #1a1a3e 100%);
            --sidebar-hover: rgba(255, 255, 255, 0.06);
            --sidebar-active-bg: rgba(102, 126, 234, 0.15);
            --sidebar-active-border: #667eea;
            --brand-gradient: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            --brand-primary: #667eea;
            --text-primary: #1a1a2e;
            --text-secondary: #6b7280;
            --surface-bg: #f8f9fc;
            --shadow-sm: 0 1px 3px rgba(0,0,0,0.06);
            --shadow-md: 0 4px 12px rgba(0,0,0,0.06);
            --shadow-lg: 0 10px 30px rgba(0,0,0,0.08);
            --radius-md: 12px;
            --radius-lg: 16px;
            --radius-xl: 20px;
            --transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        }

        * { box-sizing: border-box; }

        body {
            font-family: var(--font-base);
            background-color: var(--surface-bg);
            margin: 0;
            padding: 0;
            -webkit-font-smoothing: antialiased;
            -moz-osx-font-smoothing: grayscale;
        }

        /* ===== SIDEBAR ===== */
        .admin-sidebar {
            position: fixed;
            top: 0;
            left: 0;
            width: var(--sidebar-width);
            height: 100vh;
            background: var(--sidebar-bg);
            display: flex;
            flex-direction: column;
            z-index: 1000;
            overflow-y: auto;
        }

        /* Gradient accent line at right edge */
        .admin-sidebar::after {
            content: '';
            position: absolute;
            top: 0;
            right: 0;
            width: 2px;
            height: 100%;
            background: linear-gradient(180deg, #667eea 0%, #764ba2 50%, transparent 100%);
            opacity: 0.3;
        }

        .sidebar-brand {
            display: flex;
            align-items: center;
            gap: 12px;
            padding: 20px 24px;
            border-bottom: 1px solid rgba(255,255,255,0.06);
            text-decoration: none;
        }

        .sidebar-brand .brand-icon {
            width: 36px;
            height: 36px;
            border-radius: 10px;
            background: var(--brand-gradient);
            display: flex;
            align-items: center;
            justify-content: center;
            box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
        }

        .sidebar-brand .brand-icon i {
            color: #fff;
            font-size: 1rem;
        }

        .sidebar-brand span {
            font-size: 1.1rem;
            font-weight: 800;
            color: #fff;
            letter-spacing: -0.3px;
        }

        .sidebar-brand span em {
            background: var(--brand-gradient);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
            font-style: normal;
        }

        .sidebar-brand .admin-tag {
            font-size: 0.6rem;
            font-weight: 700;
            text-transform: uppercase;
            letter-spacing: 1px;
            color: rgba(255,255,255,0.35);
            background: rgba(255,255,255,0.06);
            padding: 2px 8px;
            border-radius: 6px;
            margin-left: 4px;
        }

        .sidebar-menu {
            list-style: none;
            padding: 16px 12px;
            margin: 0;
            flex: 1;
        }

        .sidebar-menu .menu-label {
            font-size: 0.68rem;
            font-weight: 700;
            text-transform: uppercase;
            letter-spacing: 1.5px;
            color: rgba(255,255,255,0.25);
            padding: 20px 12px 8px;
        }

        .sidebar-menu a {
            display: flex;
            align-items: center;
            gap: 12px;
            padding: 11px 14px;
            color: rgba(255,255,255,0.5);
            text-decoration: none;
            font-size: 0.88rem;
            font-weight: 500;
            border-radius: var(--radius-md);
            border-left: 3px solid transparent;
            transition: var(--transition);
            margin-bottom: 2px;
        }

        .sidebar-menu a:hover {
            background-color: var(--sidebar-hover);
            color: rgba(255,255,255,0.85);
        }

        .sidebar-menu a.active {
            background: var(--sidebar-active-bg);
            color: #fff;
            border-left-color: var(--sidebar-active-border);
            font-weight: 600;
        }

        .sidebar-menu a i {
            font-size: 1.1rem;
            width: 22px;
            text-align: center;
            flex-shrink: 0;
        }

        .sidebar-footer {
            padding: 16px 24px;
            border-top: 1px solid rgba(255,255,255,0.06);
        }

        .sidebar-footer a {
            display: flex;
            align-items: center;
            gap: 10px;
            color: rgba(255,255,255,0.4);
            text-decoration: none;
            font-size: 0.85rem;
            font-weight: 500;
            padding: 8px 0;
            transition: var(--transition);
        }

        .sidebar-footer a:hover {
            color: #f5576c;
        }

        /* ===== TOPBAR ===== */
        .admin-topbar {
            position: fixed;
            top: 0;
            left: var(--sidebar-width);
            right: 0;
            height: var(--topbar-height);
            background: rgba(255, 255, 255, 0.85);
            backdrop-filter: blur(12px);
            -webkit-backdrop-filter: blur(12px);
            border-bottom: 1px solid rgba(0,0,0,0.06);
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 0 28px;
            z-index: 999;
        }

        .topbar-title {
            font-size: 0.95rem;
            font-weight: 700;
            color: var(--text-primary);
        }

        .topbar-title span {
            color: var(--text-secondary);
            font-weight: 400;
            font-size: 0.82rem;
            margin-left: 8px;
        }

        .topbar-right {
            display: flex;
            align-items: center;
            gap: 14px;
        }

        .topbar-bell {
            width: 38px;
            height: 38px;
            border-radius: var(--radius-md);
            border: 1px solid rgba(0,0,0,0.06);
            background: #fff;
            display: flex;
            align-items: center;
            justify-content: center;
            cursor: pointer;
            transition: var(--transition);
        }

        .topbar-bell:hover {
            background: var(--surface-bg);
            border-color: rgba(0,0,0,0.1);
        }

        .topbar-admin-badge {
            display: flex;
            align-items: center;
            gap: 10px;
            padding: 6px 14px 6px 6px;
            background: var(--surface-bg);
            border-radius: var(--radius-md);
            border: 1px solid rgba(0,0,0,0.06);
        }

        .topbar-admin-badge .avatar {
            width: 32px;
            height: 32px;
            border-radius: 10px;
            background: var(--brand-gradient);
            display: flex;
            align-items: center;
            justify-content: center;
            color: white;
            font-size: 0.78rem;
            font-weight: 700;
        }

        .topbar-admin-badge .admin-name {
            font-size: 0.85rem;
            font-weight: 600;
            color: var(--text-primary);
        }

        /* ===== MAIN CONTENT ===== */
        .admin-main {
            margin-left: var(--sidebar-width);
            padding-top: var(--topbar-height);
            min-height: 100vh;
        }

        .admin-content {
            padding: 28px;
        }

        /* ===== ADMIN SHARED UTILITIES ===== */
        .admin-card {
            background: #fff;
            border: 1px solid rgba(0,0,0,0.05);
            border-radius: var(--radius-xl);
            box-shadow: var(--shadow-sm);
            transition: var(--transition);
        }

        .admin-card:hover {
            box-shadow: var(--shadow-md);
        }

        .stat-icon {
            width: 48px;
            height: 48px;
            border-radius: 14px;
            display: flex;
            align-items: center;
            justify-content: center;
            flex-shrink: 0;
        }

        .admin-table {
            border-collapse: separate;
            border-spacing: 0;
        }

        .admin-table thead tr {
            background: #f8f9fc;
        }

        .admin-table th {
            font-size: 0.75rem;
            font-weight: 700;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            color: var(--text-secondary);
            border: none;
            padding: 14px 16px;
        }

        .admin-table td {
            border: none;
            border-bottom: 1px solid #f3f4f6;
            padding: 14px 16px;
            font-size: 0.88rem;
        }

        .admin-table tbody tr {
            transition: var(--transition);
        }

        .admin-table tbody tr:hover {
            background: #fafbfd;
        }

        .admin-btn-gradient {
            background: var(--brand-gradient);
            color: #fff;
            border: none;
            border-radius: 10px;
            font-weight: 600;
            padding: 8px 18px;
            font-size: 0.85rem;
            transition: var(--transition);
            box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
            display: inline-flex;
            align-items: center;
            gap: 6px;
            text-decoration: none;
        }

        .admin-btn-gradient:hover {
            box-shadow: 0 6px 20px rgba(102, 126, 234, 0.35);
            color: #fff;
            transform: translateY(-1px);
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
        <div class="brand-icon"><i class="bi bi-phone"></i></div>
        <span>Phone<em>Store</em></span>
        <span class="admin-tag">Admin</span>
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
        <!-- Notification bell -->
        <div class="topbar-bell">
            <i class="bi bi-bell" style="font-size:0.9rem;color:var(--text-secondary);"></i>
        </div>
        <!-- Admin badge -->
        <div class="topbar-admin-badge">
            <div class="avatar">A</div>
            <span class="admin-name">Admin</span>
        </div>
    </div>
</div>

<!-- ===== MAIN CONTENT START ===== -->
<div class="admin-main">
    <div class="admin-content">
