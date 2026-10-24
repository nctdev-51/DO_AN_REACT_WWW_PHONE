<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<jsp:include page="/WEB-INF/views/shared/admin/header.jsp" />
<div class="section-heading">
  <p class="muted">Quản lý thông tin sản phẩm; giá từng phiên bản và tồn kho nằm trong mục Phiên bản.</p>
  <a class="btn btn-brand" href="${pageContext.request.contextPath}/admin/products/new">+ Thêm sản phẩm</a>
</div>
<section class="panel">
  <div class="table-responsive">
    <table class="table align-middle">
      <thead>
        <tr>
          <th>Sản phẩm</th>
          <th>Thương hiệu</th>
          <th>Giá từ</th>
          <th>Tồn kho</th>
          <th>Trạng thái</th>
          <th>Thao tác</th>
        </tr>
      </thead>
      <tbody>
        <c:forEach var="product" items="${products}">
          <tr>
            <td>
              <div class="table-product">
                <img src="${pageContext.request.contextPath}${product.image}" alt="">
                <div>
                  <strong>
                    <c:out value="${product.name}"/>
                  </strong>
                  <small>
                    <c:out value="${product.sku}"/>
                  </small>
                </div>
              </div>
            </td>
            <td>
              <c:out value="${product.brand}"/>
            </td>
            <td>
              <ui:money value="${product.price}"/>
            </td>
            <td>${product.stock}</td>
            <td>${product.status == 1 ? 'Đang bán' : 'Ngừng bán'}</td>
            <td>
              <div class="table-actions">
                <a class="btn btn-light btn-sm" href="${pageContext.request.contextPath}/admin/products/${product.id}/edit">Sửa</a>
                <a class="btn btn-light btn-sm" href="${pageContext.request.contextPath}/admin/products/${product.id}/variants">Phiên bản</a>
                <form action="${pageContext.request.contextPath}/admin/products/${product.id}/delete" method="post" data-confirm="Xóa hoặc ngừng kinh doanh sản phẩm này?">
                  <ui:csrf/>
                  <button class="btn btn-outline-danger btn-sm">Xóa</button>
                </form>
              </div>
            </td>
          </tr>
        </c:forEach>
      </tbody>
    </table>
  </div>
</section>
<jsp:include page="/WEB-INF/views/shared/admin/footer.jsp" />
