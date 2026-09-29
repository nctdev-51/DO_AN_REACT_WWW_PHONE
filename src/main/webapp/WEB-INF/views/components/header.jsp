<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>PhoneStore - Cửa Hàng Điện Thoại Di Động Chính Hãng</title>
    <meta name="description" content="PhoneStore - Hệ thống bán lẻ điện thoại di động chính hãng. iPhone, Samsung, Xiaomi với giá tốt nhất, bảo hành 12 tháng.">

    <!-- Google Fonts -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800;900&display=swap" rel="stylesheet">

    <!-- Bootstrap 5 CSS CDN -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH" crossorigin="anonymous">

    <!-- Bootstrap Icons CDN -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">

    <!-- Custom Global Styles - Binance Light Theme -->
    <style>
        :root {
            /* === Binance-inspired Color Palette === */
            --brand-yellow: #FCD535;
            --brand-yellow-hover: #F0B90B;
            --brand-yellow-glow: rgba(252, 213, 53, 0.25);
            --brand-yellow-muted: rgba(252, 213, 53, 0.15);

            /* Surfaces */
            --surface-bg: #FAFAFA;
            --surface-primary: #FFFFFF;
            --surface-secondary: #F5F5F5;
            --surface-card: #FFFFFF;
            --surface-card-hover: #F5F5F5;
            --surface-elevated: #FFFFFF;

            /* Text */
            --text-primary: #1E2329;
            --text-secondary: #474D57;
            --text-tertiary: #707A8A;
            --text-dark: #1E2329;

            /* Borders */
            --border-primary: #EAECEF;
            --border-secondary: #D4D6D9;
            --border-focus: #FCD535;

            /* Semantic */
            --color-success: #0ECB81;
            --color-danger: #F6465D;
            --color-warning: #F0B90B;
            --color-info: #1E9CEA;

            /* Glass */
            --glass-bg: rgba(255, 255, 255, 0.85);
            --glass-border: rgba(0, 0, 0, 0.08);

            /* Typography */
            --font-family-base: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;

            /* Radius */
            --radius-sm: 8px;
            --radius-md: 12px;
            --radius-lg: 16px;
            --radius-xl: 20px;
            --radius-full: 9999px;

            /* Shadows */
            --shadow-sm: 0 1px 3px rgba(0,0,0,0.3);
            --shadow-md: 0 4px 12px rgba(0,0,0,0.4);
            --shadow-lg: 0 10px 30px rgba(0,0,0,0.5);
            --shadow-xl: 0 20px 50px rgba(0,0,0,0.6);
            --shadow-yellow: 0 4px 20px rgba(252, 213, 53, 0.25);

            /* Transitions */
            --transition-base: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
            --transition-fast: all 0.15s ease;
        }

        * {
            box-sizing: border-box;
        }

        body {
            font-family: var(--font-family-base);
            color: var(--text-primary);
            background-color: var(--surface-bg);
            min-height: 100vh;
            display: flex;
            flex-direction: column;
            -webkit-font-smoothing: antialiased;
            -moz-osx-font-smoothing: grayscale;
        }

        /* ===== SCROLLBAR ===== */
        ::-webkit-scrollbar { width: 8px; }
        ::-webkit-scrollbar-track { background: var(--surface-primary); }
        ::-webkit-scrollbar-thumb { background: var(--surface-elevated); border-radius: 4px; }
        ::-webkit-scrollbar-thumb:hover { background: var(--border-secondary); }

        /* ===== NAVBAR ===== */
        .ps-navbar {
            background: var(--surface-primary);
            padding: 0;
            border: none;
            position: relative;
            z-index: 1000;
            border-bottom: 1px solid var(--border-primary);
        }

        .ps-navbar .container {
            padding-top: 12px;
            padding-bottom: 12px;
        }

        /* Brand */
        .ps-brand {
            font-weight: 800;
            font-size: 1.5rem;
            letter-spacing: -0.5px;
            text-decoration: none;
            display: flex;
            align-items: center;
            gap: 10px;
            transition: var(--transition-base);
        }

        .ps-brand:hover {
            transform: scale(1.03);
        }

        .ps-brand .brand-icon {
            width: 38px;
            height: 38px;
            border-radius: var(--radius-md);
            background: var(--brand-yellow);
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 1.15rem;
            color: var(--text-dark);
            box-shadow: var(--shadow-yellow);
        }

        .ps-brand .brand-text {
            color: var(--text-primary);
        }

        .ps-brand .brand-text span {
            color: var(--brand-yellow);
        }

        /* Search Bar */
        .ps-search {
            position: relative;
            max-width: 420px;
            width: 100%;
        }

        .ps-search .search-wrapper {
            position: relative;
            display: flex;
            align-items: center;
            background: var(--surface-secondary);
            border: 1px solid var(--border-primary);
            border-radius: var(--radius-full);
            transition: var(--transition-base);
            overflow: hidden;
        }

        .ps-search .search-wrapper:focus-within {
            border-color: var(--brand-yellow);
            box-shadow: 0 0 0 3px var(--brand-yellow-muted);
            background: var(--surface-elevated);
        }

        .ps-search input {
            background: transparent;
            border: none;
            color: var(--text-primary);
            padding: 10px 16px 10px 44px;
            font-size: 0.88rem;
            font-weight: 400;
            width: 100%;
            outline: none;
        }

        .ps-search input::placeholder {
            color: var(--text-tertiary);
        }

        .ps-search .search-icon {
            position: absolute;
            left: 16px;
            color: var(--text-tertiary);
            font-size: 0.9rem;
            pointer-events: none;
        }

        .ps-search button {
            background: var(--brand-yellow);
            border: none;
            color: var(--text-dark);
            padding: 10px 18px;
            font-size: 0.85rem;
            cursor: pointer;
            transition: var(--transition-base);
            font-weight: 600;
        }

        .ps-search button:hover {
            background: var(--brand-yellow-hover);
        }

        /* Nav Links */
        .ps-nav-link {
            color: var(--text-secondary);
            font-weight: 500;
            font-size: 0.9rem;
            text-decoration: none;
            padding: 8px 16px;
            border-radius: var(--radius-sm);
            transition: var(--transition-base);
            display: flex;
            align-items: center;
            gap: 6px;
            position: relative;
        }

        .ps-nav-link:hover {
            color: var(--text-primary);
            background: var(--surface-secondary);
        }

        .ps-nav-link.active {
            color: var(--brand-yellow);
            background: var(--brand-yellow-muted);
        }

        .ps-nav-link .badge-cart {
            font-size: 0.65rem;
            padding: 2px 6px;
            border-radius: var(--radius-full);
            background: var(--brand-yellow);
            color: var(--text-dark);
            font-weight: 700;
            line-height: 1;
            min-width: 18px;
            text-align: center;
        }

        /* Auth buttons */
        .ps-btn-auth {
            padding: 8px 20px;
            border-radius: var(--radius-sm);
            font-weight: 600;
            font-size: 0.85rem;
            transition: var(--transition-base);
            display: inline-flex;
            align-items: center;
            gap: 6px;
            text-decoration: none;
        }

        .ps-btn-login {
            background: var(--brand-yellow);
            color: var(--text-dark);
            border: none;
            box-shadow: var(--shadow-yellow);
        }

        .ps-btn-login:hover {
            transform: translateY(-1px);
            box-shadow: 0 6px 25px rgba(252, 213, 53, 0.35);
            color: var(--text-dark);
            background: var(--brand-yellow-hover);
        }

        .ps-btn-logout {
            background: transparent;
            color: var(--text-secondary);
            border: 1px solid var(--border-primary);
        }

        .ps-btn-logout:hover {
            color: var(--color-danger);
            border-color: var(--color-danger);
            background: rgba(246, 70, 93, 0.1);
        }

        /* ===== UTILITY CLASSES ===== */
        .section-badge {
            display: inline-flex;
            align-items: center;
            gap: 6px;
            padding: 6px 16px;
            border-radius: var(--radius-full);
            font-size: 0.78rem;
            font-weight: 600;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }

        .section-badge-primary {
            background: var(--brand-yellow-muted);
            color: var(--brand-yellow);
        }

        .card-modern {
            background: var(--surface-card);
            border: 1px solid var(--border-primary);
            border-radius: var(--radius-lg);
            transition: var(--transition-base);
            overflow: hidden;
        }

        .card-modern:hover {
            transform: translateY(-4px);
            box-shadow: var(--shadow-lg);
            border-color: var(--brand-yellow);
        }

        .btn-gradient {
            background: var(--brand-yellow);
            color: var(--text-dark);
            border: none;
            border-radius: var(--radius-sm);
            font-weight: 700;
            padding: 10px 24px;
            transition: var(--transition-base);
            box-shadow: var(--shadow-yellow);
            text-decoration: none;
            cursor: pointer;
        }

        .btn-gradient:hover {
            transform: translateY(-2px);
            box-shadow: 0 8px 30px rgba(252, 213, 53, 0.35);
            color: var(--text-dark);
            background: var(--brand-yellow-hover);
        }

        .btn-ghost {
            background: transparent;
            color: var(--text-secondary);
            border: 1.5px solid var(--border-primary);
            border-radius: var(--radius-sm);
            font-weight: 600;
            padding: 10px 24px;
            transition: var(--transition-base);
            text-decoration: none;
        }

        .btn-ghost:hover {
            border-color: var(--brand-yellow);
            color: var(--brand-yellow);
            background: var(--brand-yellow-muted);
        }

        .price-tag {
            color: var(--brand-yellow);
            font-weight: 800;
        }

        /* Animations */
        @keyframes fadeInUp {
            from {
                opacity: 0;
                transform: translateY(20px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }

        .animate-fadeInUp {
            animation: fadeInUp 0.6s ease-out forwards;
        }

        @keyframes pulse-glow {
            0%, 100% { box-shadow: 0 0 0 0 rgba(252, 213, 53, 0.4); }
            50% { box-shadow: 0 0 0 8px rgba(252, 213, 53, 0); }
        }

        @keyframes shimmer {
            0% { background-position: -200% 0; }
            100% { background-position: 200% 0; }
        }

        /* Responsive */
        @media (max-width: 991.98px) {
            .ps-search {
                max-width: 100%;
                margin: 12px 0;
            }
        }
    </style>
</head>
<body>

    <!-- Header / Navbar Component -->
    <header class="sticky-top" style="z-index:1050;">
        <nav class="navbar navbar-expand-lg ps-navbar">
            <div class="container d-flex flex-wrap align-items-center justify-content-between">
                <!-- Brand / Logo -->
                <a class="ps-brand" href="${pageContext.request.contextPath}/">
                    <div class="brand-icon">
                        <i class="bi bi-phone"></i>
                    </div>
                    <div class="brand-text">Phone<span>Store</span></div>
                </a>

                <!-- Mobile Toggle Button -->
                <button class="navbar-toggler border-0 d-lg-none" type="button" data-bs-toggle="collapse" data-bs-target="#navbarContent" aria-controls="navbarContent" aria-expanded="false" aria-label="Toggle navigation" style="color:var(--text-secondary);font-size:1.4rem;">
                    <i class="bi bi-list"></i>
                </button>

                <!-- Navigation Links & Search -->
                <div class="collapse navbar-collapse" id="navbarContent">
                    <!-- Search Bar -->
                    <form class="ps-search mx-lg-auto my-3 my-lg-0" action="${pageContext.request.contextPath}/products" method="get">
                        <div class="search-wrapper">
                            <i class="bi bi-search search-icon"></i>
                            <input type="search" name="keyword" placeholder="Tìm kiếm điện thoại, phụ kiện..." aria-label="Search">
                            <button type="submit"><i class="bi bi-arrow-right"></i></button>
                        </div>
                    </form>

                    <!-- Nav Items & Actions -->
                    <ul class="navbar-nav ms-auto align-items-lg-center gap-2">
                        <li class="nav-item">
                            <a class="ps-nav-link" id="homeNavLink" href="${pageContext.request.contextPath}/" style="font-weight:600;">
                                Trang chủ
                            </a>
                        </li>
                        <li class="nav-item">
                            <a class="ps-nav-link" id="productsNavLink" href="${pageContext.request.contextPath}/products" style="font-weight:600;">
                                Sản phẩm
                            </a>
                        </li>

                        <!-- Vertical Divider -->
                        <li class="nav-item d-none d-lg-block mx-2">
                            <div style="width:1px;height:24px;background:var(--border-secondary);"></div>
                        </li>

                        <!-- Cart Icon -->
                        <li class="nav-item ms-lg-1 d-flex align-items-center">
                            <a class="position-relative d-inline-flex align-items-center justify-content-center" href="${pageContext.request.contextPath}/cart" style="width:40px;height:40px;color:var(--text-primary);font-size:1.3rem;text-decoration:none;transition:var(--transition-base);border-radius:12px;" onmouseover="this.style.background='var(--surface-secondary)';this.style.color='var(--brand-yellow)'" onmouseout="this.style.background='transparent';this.style.color='var(--text-primary)'">
                                <i class="bi bi-cart3"></i>
                                <span class="position-absolute top-0 start-100 translate-middle badge rounded-pill" style="background:var(--brand-yellow);color:var(--text-dark);font-size:0.65rem;padding:3px 6px;margin-top:6px;margin-left:-6px;">0</span>
                            </a>
                        </li>

                        <c:choose>
                            <c:when test="${pageContext.request.userPrincipal != null}">
                                <!-- Account Dropdown -->
                                <li class="nav-item dropdown ms-lg-2">
                                    <a class="nav-link d-flex align-items-center gap-2" href="#" id="accountDropdown" role="button" data-bs-toggle="dropdown" aria-expanded="false" style="color:var(--text-primary);font-weight:600;padding:6px 12px;border-radius:12px;transition:var(--transition-base);" onmouseover="this.style.background='var(--surface-secondary)'" onmouseout="this.style.background='transparent'">
                                        <div style="width:32px;height:32px;border-radius:50%;background:var(--brand-yellow-muted);display:flex;align-items:center;justify-content:center;color:var(--brand-yellow);">
                                            <i class="bi bi-person-fill"></i>
                                        </div>
                                        <span>Tài khoản</span>
                                        <i class="bi bi-chevron-down" style="font-size:0.8rem;color:var(--text-tertiary);"></i>
                                    </a>
                                    <ul class="dropdown-menu dropdown-menu-end shadow-md" aria-labelledby="accountDropdown" style="border:1px solid var(--border-primary);border-radius:16px;padding:12px;min-width:200px;">
                                        <li>
                                            <div class="px-3 py-2 mb-2" style="background:var(--surface-secondary);border-radius:10px;">
                                                <small style="color:var(--text-tertiary);font-weight:600;">Xin chào,</small>
                                                <div style="font-weight:700;color:var(--text-primary);">${pageContext.request.userPrincipal.name}</div>
                                            </div>
                                        </li>
                                        <li><a class="dropdown-item" href="${pageContext.request.contextPath}/admin/orders" style="border-radius:10px;padding:8px 16px;font-size:0.95rem;font-weight:500;transition:var(--transition-base);"><i class="bi bi-bag-check me-2" style="color:var(--text-secondary);"></i>Đơn hàng của tôi</a></li>
                                        <li><hr class="dropdown-divider" style="border-color:var(--border-primary);margin:8px 0;"></li>
                                        <li>
                                            <form action="${pageContext.request.contextPath}/logout" method="post" style="margin:0;">
                                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                                <button class="dropdown-item text-danger" type="submit" style="border-radius:10px;padding:8px 16px;font-size:0.95rem;font-weight:500;transition:var(--transition-base);">
                                                    <i class="bi bi-box-arrow-right me-2"></i>Đăng xuất
                                                </button>
                                            </form>
                                        </li>
                                    </ul>
                                </li>
                            </c:when>
                            <c:otherwise>
                                <!-- Sign In / Sign Up -->
                                <li class="nav-item ms-lg-2 mt-2 mt-lg-0">
                                    <a href="${pageContext.request.contextPath}/login" style="display:inline-block;padding:10px 18px;border-radius:12px;font-size:0.95rem;font-weight:600;color:var(--text-primary);text-decoration:none;transition:var(--transition-base);" onmouseover="this.style.background='var(--surface-secondary)'" onmouseout="this.style.background='transparent'">
                                        Đăng nhập
                                    </a>
                                </li>
                                <li class="nav-item ms-lg-2 mt-2 mt-lg-0">
                                    <a class="btn-gradient" href="${pageContext.request.contextPath}/register" style="padding:10px 20px;border-radius:12px;font-size:0.95rem;display:inline-block;">
                                        Đăng ký
                                    </a>
                                </li>
                            </c:otherwise>
                        </c:choose>
                    </ul>
                </div>
            </div>
        </nav>
    </header>

    <!-- Main Content Container Start -->
    <main class="flex-grow-1">
    </main>
    <script>
        (() => {
            const currentPath = window.location.pathname.replace(/\/+$/, '') || '/';
            const links = [
                { id: 'homeNavLink', matches: path => currentPath === (path.replace(/\/+$/, '') || '/') },
                { id: 'productsNavLink', matches: path => currentPath === path || currentPath.startsWith(path + '/') },
                { id: 'cartNavLink', matches: path => currentPath === path || currentPath.startsWith(path + '/') }
            ];

            links.forEach(({ id, matches }) => {
                const link = document.getElementById(id);
                const path = new URL(link.href).pathname.replace(/\/+$/, '') || '/';
                if (matches(path)) {
                    link.classList.add('active');
                    link.setAttribute('aria-current', 'page');
                }
            });
        })();
    </script>