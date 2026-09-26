<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>

    </div><!-- /.admin-content -->
</div><!-- /.admin-main -->

<!-- ===== FOOTER BAR ===== -->
<div style="
    margin-left: var(--sidebar-width);
    background-color: #fff;
    border-top: 1px solid #e9ecef;
    padding: 12px 28px;
    font-size: 0.8rem;
    color: #adb5bd;
    display: flex;
    justify-content: space-between;
    align-items: center;
">
    <span>&copy; 2025 <strong style="color:#495057;">PhoneStore Admin</strong> &mdash; Đồ án Lập trình WWW - FIT IUH</span>
    <span>Spring Boot 4 &amp; Bootstrap 5</span>
</div>

<!-- Bootstrap 5 JS Bundle -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>

<!-- Chart.js (sẵn sàng cho Task 2+3) -->
<script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.3/dist/chart.umd.min.js"></script>

<!-- Responsive sidebar toggle (mobile) -->
<script>
    // Highlight active sidebar link dựa vào URL hiện tại
    document.addEventListener('DOMContentLoaded', function () {
        const links = document.querySelectorAll('.sidebar-menu a');
        const currentPath = window.location.pathname;
        links.forEach(link => {
            const href = link.getAttribute('href');
            if (href && currentPath.startsWith(href) && href !== '/') {
                link.classList.add('active');
            }
        });
    });
</script>

</body>
</html>
