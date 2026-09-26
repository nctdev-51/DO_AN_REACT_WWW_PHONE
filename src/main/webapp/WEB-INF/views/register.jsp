<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<jsp:include page="components/header.jsp" />

<div class="container my-5">
    <div class="row justify-content-center">
        <div class="col-md-6">
            <div class="card shadow">
                <div class="card-header bg-primary text-white">
                    <h3 class="card-title mb-0">Đăng ký tài khoản</h3>
                </div>
                <div class="card-body">
                    <c:if test="${param.success != null}">
                        <div class="alert alert-success">Đăng ký thành công! Bạn có thể <a href="${pageContext.request.contextPath}/login">đăng nhập</a> ngay.</div>
                    </c:if>
                    
                    <form:form action="${pageContext.request.contextPath}/register" method="post" modelAttribute="user">
                        <div class="mb-3">
                            <label for="fullName" class="form-label">Họ và tên</label>
                            <form:input path="fullName" id="fullName" class="form-control" />
                            <form:errors path="fullName" cssClass="text-danger" />
                        </div>
                        
                        <div class="mb-3">
                            <label for="email" class="form-label">Email</label>
                            <form:input path="email" type="email" id="email" class="form-control" />
                            <form:errors path="email" cssClass="text-danger" />
                        </div>
                        
                        <div class="mb-3">
                            <label for="password" class="form-label">Mật khẩu</label>
                            <form:password path="password" id="password" class="form-control" />
                            <form:errors path="password" cssClass="text-danger" />
                        </div>
                        
                        <div class="mb-3">
                            <label for="confirmPassword" class="form-label">Xác nhận mật khẩu</label>
                            <form:password path="confirmPassword" id="confirmPassword" class="form-control" />
                            <form:errors path="confirmPassword" cssClass="text-danger" />
                        </div>
                        
                        <div class="mb-3">
                            <label for="phone" class="form-label">Số điện thoại</label>
                            <form:input path="phone" id="phone" class="form-control" />
                        </div>
                        
                        <div class="mb-3">
                            <label for="address" class="form-label">Địa chỉ</label>
                            <form:input path="address" id="address" class="form-control" />
                        </div>
                        
                        <div class="d-grid gap-2">
                            <button type="submit" class="btn btn-primary">Đăng ký</button>
                        </div>
                    </form:form>
                    
                    <div class="mt-3 text-center">
                        <p>Đã có tài khoản? <a href="${pageContext.request.contextPath}/login">Đăng nhập</a></p>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="components/footer.jsp" />
