<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>PhoneStore - Cửa Hàng Điện Thoại Di Động Chính Hãng</title>

    <!-- Google Fonts -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap" rel="stylesheet">

    <!-- Bootstrap 5 CSS CDN -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH" crossorigin="anonymous">

    <!-- Bootstrap Icons CDN -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">

    <!-- Custom Global Styles -->
    <style>
        :root {
            --brand-primary: #0d6efd;
            --brand-hover: #0b5ed7;
            --font-family-base: 'Plus Jakarta Sans', -apple-system, BlinkMacSystemFont, sans-serif;
        }

        body {
            font-family: var(--font-family-base);
            color: #212529;
            background-color: #f8f9fa;
            min-height: 100vh;
            display: flex;
            flex-direction: column;
        }

        .navbar-brand-custom {
            font-weight: 800;
            font-size: 1.4rem;
            letter-spacing: -0.5px;
        }

        .navbar-brand-custom i {
            color: #0d6efd;
        }

        .nav-link {
            font-weight: 500;
            transition: color 0.2s ease-in-out;
        }

        .search-input {
            border-radius: 20px 0 0 20px;
        }

        .search-btn {
            border-radius: 0 20px 20px 0;
        }

        .badge-cart {
            font-size: 0.7rem;
            padding: 0.25em 0.5em;
        }
    </style>
</head>
<body>

    <!-- Header / Navbar Component -->
    <header class="sticky-top shadow-sm">
        <nav class="navbar navbar-expand-lg navbar-dark bg-dark py-3">
            <div class="container">
                <!-- Brand / Logo -->
                <a class="navbar-brand navbar-brand-custom text-white d-flex align-items-center" href="${pageContext.request.contextPath}/">
                    <i class="bi bi-phone me-2 text-primary fs-3"></i>
                    <span>Phone<span class="text-primary">Store</span></span>
                </a>

                <!-- Mobile Toggle Button -->
                <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarContent" aria-controls="navbarContent" aria-expanded="false" aria-label="Toggle navigation">
                    <span class="navbar-toggler-icon"></span>
                </button>

                <!-- Navigation Links & Search -->
                <div class="collapse navbar-collapse" id="navbarContent">
                    <!-- Search Bar -->
                    <form class="d-flex mx-lg-auto my-3 my-lg-0 w-100" style="max-width: 420px;" action="${pageContext.request.contextPath}/products" method="get">
                        <div class="input-group">
                            <input class="form-control search-input bg-light border-0 shadow-none px-3" type="search" name="keyword" placeholder="Tìm kiếm điện thoại, phụ kiện..." aria-label="Search">
                            <button class="btn btn-primary search-btn px-3" type="submit">
                                <i class="bi bi-search"></i>
                            </button>
                        </div>
                    </form>

                    <!-- Nav Items -->
                    <ul class="navbar-nav ms-auto align-items-lg-center gap-lg-2">
                        <li class="nav-item">
                            <a class="nav-link text-white-50" id="homeNavLink" href="${pageContext.request.contextPath}/">
                                <i class="bi bi-house-door me-1"></i> Trang chủ
                            </a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link text-white-50" id="productsNavLink" href="${pageContext.request.contextPath}/products">
                                <i class="bi bi-grid me-1"></i> Sản phẩm
                            </a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link position-relative text-white-50" id="cartNavLink" href="${pageContext.request.contextPath}/cart">
                                <i class="bi bi-cart3 me-1"></i> Giỏ hàng
                                <span class="badge bg-danger rounded-pill badge-cart ms-1">0</span>
                            </a>
                        </li>
                        <li class="nav-item ms-lg-2 mt-2 mt-lg-0">
                            <a class="btn btn-outline-light btn-sm px-3 rounded-pill" href="${pageContext.request.contextPath}/admin/login">
                                <i class="bi bi-person-circle me-1"></i> Đăng nhập
                            </a>
                        </li>
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
                    link.classList.remove('text-white-50');
                    link.classList.add('active', 'text-white');
                    link.setAttribute('aria-current', 'page');
                }
            });
        })();
    </script>