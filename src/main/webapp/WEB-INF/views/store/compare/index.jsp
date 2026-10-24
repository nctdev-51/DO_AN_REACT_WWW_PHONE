<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<jsp:include page="/WEB-INF/views/shared/store/header.jsp" />
<div class="section-heading">
  <div>
    <span class="eyebrow">
      CHỌN ĐIỆN THOẠI PHÙ HỢP
    </span>
    <h1>
      So sánh sản phẩm
    </h1>
    <p class="muted">
      Đối chiếu tối đa 3 máy theo đúng phiên bản. Giá và tình trạng hàng được cập nhật từ cửa hàng.
    </p>
  </div>
</div>
<c:if test="${comparison.variants.size() == 1}"><div class="alert alert-info">Thêm ít nhất một sản phẩm khác để xem điểm khác nhau.</div></c:if>
<c:if test="${not empty comparison.variants}">
  <div class="comparison-tools panel">
    <label>
      <input type="checkbox" id="compare-differences">
      Chỉ hiện điểm khác nhau
    </label>
    <a class="btn btn-outline-dark" href="${pageContext.request.contextPath}${comparison.sharePath}">
      Liên kết chia sẻ
    </a>
    <c:if test="${not sharedSelection}">
      <form action="${pageContext.request.contextPath}/compare/clear" method="post">
        <ui:csrf/>
        <button class="btn btn-outline-danger">
          Bỏ tất cả
        </button>
      </form>
    </c:if>
  </div>
  <div class="comparison-scroll panel">
    <table class="comparison-table" id="comparison-table">
      <caption class="visually-hidden">
        Bảng so sánh các phiên bản điện thoại đã chọn
      </caption>
      <thead>
        <tr>
          <th scope="col">
            Thông tin
          </th>
          <c:forEach var="variant" items="${comparison.variants}">
            <th scope="col">
              <a href="${pageContext.request.contextPath}/products/${variant.product.id}">
                <img class="compare-image" src="${pageContext.request.contextPath}${variant.product.image}" alt="<c:out value='${variant.product.name}'/>">
                <span>
                  <c:out value="${variant.product.name}"/>
                </span>
              </a>
              <form action="${pageContext.request.contextPath}/compare/add" method="post" class="mt-3">
                <ui:csrf/>
                <input type="hidden" name="productId" value="${variant.product.id}">
                <c:if test="${sharedSelection}">
                  <input type="hidden" name="baseIds" value="<c:out value='${selectionIds}'/>">
                </c:if>
                <select name="variantId" class="form-select" aria-label="Phiên bản của <c:out value='${variant.product.name}'/>">
                  <c:forEach var="option" items="${variant.product.variants}">
                    <c:if test="${option.active}">
                      <option value="${option.id}" ${option.id == variant.id ? 'selected' : ''}>
                        <c:out value="${option.label}"/>
                      </option>
                    </c:if>
                  </c:forEach>
                </select>
                <button class="btn btn-sm btn-outline-dark mt-2">
                  Đổi phiên bản
                </button>
              </form>
              <c:if test="${not sharedSelection}">
                <form action="${pageContext.request.contextPath}/compare/remove" method="post" class="mt-2">
                  <ui:csrf/>
                  <input type="hidden" name="variantId" value="${variant.id}">
                  <button class="btn btn-sm btn-link text-danger">
                    Bỏ sản phẩm
                  </button>
                </form>
              </c:if>
            </th>
          </c:forEach>
        </tr>
      </thead>
      <tbody>
        <c:forEach var="row" items="${comparison.rows}">
          <tr data-different="${row.different}" class="${row.different ? 'comparison-different' : ''}">
            <th scope="row">
              <c:out value="${row.label}"/>
            </th>
            <c:forEach var="value" items="${row.values}">
              <td>
                <c:out value="${value}"/>
              </td>
            </c:forEach>
          </tr>
        </c:forEach>
      </tbody>
      <tfoot>
        <tr>
          <th scope="row">
            Mua sản phẩm
          </th>
          <c:forEach var="variant" items="${comparison.variants}">
            <td>
              <form action="${pageContext.request.contextPath}/cart/add" method="post">
                <ui:csrf/>
                <input type="hidden" name="variantId" value="${variant.id}">
                <input type="hidden" name="quantity" value="1">
                <button class="btn btn-brand" ${variant.stock == 0 ? 'disabled' : ''}>
                  ${variant.stock == 0 ? 'Tạm hết hàng' : 'Thêm vào giỏ'}
                </button>
              </form>
            </td>
          </c:forEach>
        </tr>
      </tfoot>
    </table>
  </div>
</c:if>
<c:if test="${empty comparison.variants}">
  <div class="panel empty-state">
    <h2>
      Bạn chưa chọn sản phẩm
    </h2>
    <p>
      Thêm điện thoại bên dưới hoặc bấm “So sánh” trên trang sản phẩm.
    </p>
  </div>
</c:if>
<section class="panel mt-4">
  <h2>
    Thêm sản phẩm để so sánh
  </h2>
  <form action="${pageContext.request.contextPath}/compare" method="get" class="comparison-search">
    <c:if test="${sharedSelection}">
      <input type="hidden" name="ids" value="<c:out value='${selectionIds}'/>">
    </c:if>
    <input type="search" name="keyword" value="<c:out value='${keyword}'/>" class="form-control" placeholder="Tìm tên điện thoại..." aria-label="Tìm sản phẩm để so sánh" maxlength="100">
    <button class="btn btn-outline-dark">
      Tìm kiếm
    </button>
  </form>
  <div class="comparison-suggestions">
    <c:forEach var="product" items="${suggestions}">
      <article>
        <img src="${pageContext.request.contextPath}${product.image}" alt="" loading="lazy">
        <div>
          <a href="${pageContext.request.contextPath}/products/${product.id}">
            <c:out value="${product.name}"/>
          </a>
          <p>
            <ui:money value="${product.price}"/>
          </p>
        </div>
        <form action="${pageContext.request.contextPath}/compare/add" method="post">
          <ui:csrf/>
          <input type="hidden" name="productId" value="${product.id}">
          <c:if test="${sharedSelection}">
            <input type="hidden" name="baseIds" value="<c:out value='${selectionIds}'/>">
          </c:if>
          <button class="btn btn-sm btn-outline-dark">
            + So sánh
          </button>
        </form>
      </article>
    </c:forEach>
  </div>
  <c:if test="${empty suggestions}">
    <p class="muted mt-3">
      Không tìm thấy sản phẩm phù hợp.
    </p>
  </c:if>
</section>
<script src="${pageContext.request.contextPath}/js/comparison.js" defer>
</script>
<jsp:include page="/WEB-INF/views/shared/store/footer.jsp" />
