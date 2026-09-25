<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>WWW_PHONE_VER1 - Setup Môi Trường Thành Công</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <style>
        :root {
            --bg-color: #0d1117;
            --surface-color: #161b22;
            --surface-hover: #1f2937;
            --border-color: #30363d;
            --primary: #38bdf8;
            --primary-glow: rgba(56, 189, 248, 0.15);
            --success: #10b981;
            --success-glow: rgba(16, 185, 129, 0.15);
            --warning: #f59e0b;
            --text-main: #f0f6fc;
            --text-muted: #8b949e;
            --font-family: 'Plus Jakarta Sans', -apple-system, BlinkMacSystemFont, sans-serif;
        }

        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
        }

        body {
            font-family: var(--font-family);
            background: radial-gradient(circle at top right, #1e1e38 0%, var(--bg-color) 60%);
            color: var(--text-main);
            min-height: 100vh;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            padding: 2rem 1rem;
        }

        .container {
            max-width: 900px;
            width: 100%;
            background: rgba(22, 27, 34, 0.85);
            backdrop-filter: blur(16px);
            -webkit-backdrop-filter: blur(16px);
            border: 1px solid var(--border-color);
            border-radius: 20px;
            padding: 2.5rem;
            box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5), 0 0 40px var(--primary-glow);
        }

        .header-badge {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            background: var(--success-glow);
            color: var(--success);
            padding: 6px 14px;
            border-radius: 9999px;
            font-size: 0.875rem;
            font-weight: 600;
            border: 1px solid rgba(16, 185, 129, 0.3);
            margin-bottom: 1.25rem;
        }

        .status-dot {
            width: 8px;
            height: 8px;
            background-color: var(--success);
            border-radius: 50%;
            box-shadow: 0 0 10px var(--success);
            animation: pulse 2s infinite;
        }

        @keyframes pulse {
            0% { transform: scale(0.95); opacity: 0.8; }
            50% { transform: scale(1.2); opacity: 1; }
            100% { transform: scale(0.95); opacity: 0.8; }
        }

        h1 {
            font-size: 2.2rem;
            font-weight: 800;
            background: linear-gradient(135deg, #ffffff 30%, var(--primary) 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            margin-bottom: 0.75rem;
            letter-spacing: -0.02em;
        }

        .banner-message {
            background: linear-gradient(135deg, rgba(56, 189, 248, 0.1) 0%, rgba(99, 102, 241, 0.1) 100%);
            border: 1px solid rgba(56, 189, 248, 0.3);
            padding: 1.25rem 1.5rem;
            border-radius: 14px;
            margin-bottom: 2rem;
            display: flex;
            align-items: center;
            gap: 12px;
        }

        .banner-icon {
            font-size: 1.75rem;
        }

        .banner-text {
            font-size: 1.15rem;
            font-weight: 700;
            color: #38bdf8;
        }

        .grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
            gap: 1.25rem;
            margin-bottom: 2rem;
        }

        .card {
            background: var(--surface-color);
            border: 1px solid var(--border-color);
            border-radius: 14px;
            padding: 1.25rem;
            transition: all 0.25s ease;
        }

        .card:hover {
            transform: translateY(-3px);
            border-color: var(--primary);
            box-shadow: 0 8px 24px rgba(56, 189, 248, 0.12);
        }

        .card-title {
            font-size: 0.9rem;
            text-transform: uppercase;
            letter-spacing: 0.05em;
            color: var(--text-muted);
            margin-bottom: 0.5rem;
            display: flex;
            align-items: center;
            justify-content: space-between;
        }

        .card-value {
            font-size: 1.25rem;
            font-weight: 700;
            color: var(--text-main);
        }

        .card-desc {
            font-size: 0.85rem;
            color: var(--text-muted);
            margin-top: 0.35rem;
        }

        .roadmap {
            border-top: 1px solid var(--border-color);
            padding-top: 1.75rem;
        }

        .roadmap-title {
            font-size: 1.1rem;
            font-weight: 700;
            margin-bottom: 1rem;
            color: #ffffff;
        }

        .steps {
            display: flex;
            flex-direction: column;
            gap: 0.75rem;
        }

        .step-item {
            display: flex;
            align-items: flex-start;
            gap: 12px;
            padding: 0.875rem 1rem;
            border-radius: 10px;
            background: rgba(255, 255, 255, 0.02);
            border: 1px solid rgba(255, 255, 255, 0.05);
        }

        .step-item.active {
            border-color: rgba(16, 185, 129, 0.4);
            background: rgba(16, 185, 129, 0.05);
        }

        .step-badge {
            font-size: 0.75rem;
            padding: 2px 8px;
            border-radius: 6px;
            font-weight: 600;
        }

        .step-badge.done {
            background: #064e3b;
            color: #34d399;
        }

        .step-badge.pending {
            background: #1e293b;
            color: #94a3b8;
        }

        .footer {
            margin-top: 2rem;
            text-align: center;
            font-size: 0.85rem;
            color: var(--text-muted);
        }
    </style>
</head>
<body>

    <div class="container">
        <div class="header-badge">
            <span class="status-dot"></span>
            Môi trường Spring Boot + JSP đã sẵn sàng
        </div>

        <h1>Đồ Án Web Bán Điện Thoại (WWW_PHONE_VER1)</h1>

        <!-- Dòng thông báo chính từ Controller (Đạt yêu cầu đề bài: ${message}) -->
        <div class="banner-message">
            <div class="banner-icon">🚀</div>
            <div>
                <div style="font-size: 0.8rem; text-transform: uppercase; color: var(--text-muted); letter-spacing: 0.05em;">Thông điệp từ HomeController:</div>
                <div class="banner-text" id="controller-message">${message}</div>
            </div>
        </div>

        <div class="grid">
            <div class="card">
                <div class="card-title">
                    <span>View Engine</span>
                    <span>JSP / JSTL</span>
                </div>
                <div class="card-value">Jakarta JSP</div>
                <div class="card-desc">Prefix: <code>/WEB-INF/views/</code><br>Suffix: <code>.jsp</code></div>
            </div>

            <div class="card">
                <div class="card-title">
                    <span>Cơ sở dữ liệu</span>
                    <span>MariaDB 12.2</span>
                </div>
                <div class="card-value">webshop_db</div>
                <div class="card-desc">Port: <code>3306</code> | User: <code>root</code><br>Hibernate DDL: <code>update</code></div>
            </div>

            <div class="card">
                <div class="card-title">
                    <span>Deploy</span>
                    <span>Tomcat 10+</span>
                </div>
                <div class="card-value">WAR Mode</div>
                <div class="card-desc">Kế thừa <code>SpringBootServletInitializer</code> sẵn sàng nộp bài</div>
            </div>
        </div>

        <div class="roadmap">
            <div class="roadmap-title">Tiến Độ Dự Án Nhóm</div>
            <div class="steps">
                <div class="step-item active">
                    <span class="step-badge done">Giai đoạn 1 - HOÀN THÀNH</span>
                    <div>
                        <strong style="color: #f0f6fc;">Khởi tạo kiến trúc dự án & View Engine</strong>
                        <p style="font-size: 0.85rem; color: var(--text-muted); margin-top: 2px;">Cấu hình pom.xml, SpringBootServletInitializer, HomeController, MariaDB datasource, Spring Security bypass ban đầu.</p>
                    </div>
                </div>

                <div class="step-item">
                    <span class="step-badge pending">Giai đoạn 2 - TIẾP THEO</span>
                    <div>
                        <strong style="color: #f0f6fc;">Spring Data JPA & MVC Catalog</strong>
                        <p style="font-size: 0.85rem; color: var(--text-muted); margin-top: 2px;">Xây dựng Entity (Product, Category, Order, User), Repository, Service và Controller hiển thị sản phẩm điện thoại.</p>
                    </div>
                </div>

                <div class="step-item">
                    <span class="step-badge pending">Giai đoạn 3 - TIẾP THEO</span>
                    <div>
                        <strong style="color: #f0f6fc;">Giỏ hàng Session & Spring Security</strong>
                        <p style="font-size: 0.85rem; color: var(--text-muted); margin-top: 2px;">Quản lý giỏ hàng bằng HttpSession và phân quyền đăng nhập khách hàng / quản trị viên (Admin).</p>
                    </div>
                </div>
            </div>
        </div>

        <div class="footer">
            FIT - Trường Đại học Công nghiệp TP.HCM (IUH) &bull; Khoa Công nghệ Thông tin
        </div>
    </div>

</body>
</html>
