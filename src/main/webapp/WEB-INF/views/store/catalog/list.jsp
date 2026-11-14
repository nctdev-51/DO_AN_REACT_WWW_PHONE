<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<jsp:include page="/WEB-INF/views/shared/store/header.jsp" />
<ui:breadcrumb currentLabel="${kind == 'ACCESSORY' ? 'Phụ kiện' : kind == 'PHONE' ? 'Điện thoại' : 'Sản phẩm'}"/>
<div class="page-heading">
  <div>
    <span class="eyebrow">BỘ SƯU TẬP PTV MOBILE</span>
    <h1>${kind == 'ACCESSORY' ? 'Phụ kiện' : kind == 'PHONE' ? 'Điện thoại' : 'Sản phẩm'}</h1>
    <p>Tìm thấy <strong>${products.totalElements}</strong> sản phẩm phù hợp để bạn lựa chọn</p>
  </div>
  <a class="btn btn-outline-dark" href="${pageContext.request.contextPath}/choose">Tư vấn chọn máy 4 bước →</a>
</div>
<nav class="quick-links" aria-label="Loại sản phẩm">
  <a href="${pageContext.request.contextPath}/products">Tất cả sản phẩm</a>
  <a href="${pageContext.request.contextPath}/products?kind=PHONE">Điện thoại</a>
  <a href="${pageContext.request.contextPath}/products?kind=ACCESSORY">Phụ kiện</a>
  <a href="${pageContext.request.contextPath}/products?sort=newest">Sản phẩm mới</a>
  <a href="${pageContext.request.contextPath}/products?sort=bestselling">Bán chạy</a>
</nav>
<jsp:include page="/WEB-INF/views/shared/store/brand-nav.jsp" />
<div class="catalog-mobile-bar d-lg-none">
  <button class="btn btn-outline-dark btn-sm" type="button" data-bs-toggle="offcanvas" data-bs-target="#catalogFilterDrawer" aria-controls="catalogFilterDrawer">
    <ui:icon name="filter"/> Bộ lọc sản phẩm
  </button>
  <span class="muted small">${products.totalElements} kết quả</span>
</div>
<div class="catalog-layout" data-catalog-view>
  <aside class="catalog-sidebar offcanvas-lg offcanvas-start" tabindex="-1" id="catalogFilterDrawer" aria-labelledby="catalogFilterDrawerLabel" aria-label="Bộ lọc tìm kiếm">
    <div class="offcanvas-header d-lg-none">
      <h2 class="offcanvas-title fs-5 m-0" id="catalogFilterDrawerLabel">Bộ lọc sản phẩm</h2>
      <button type="button" class="btn-close" data-bs-dismiss="offcanvas" data-bs-target="#catalogFilterDrawer" aria-label="Đóng bộ lọc"></button>
    </div>
    <div class="offcanvas-body p-0 d-block">
      <div class="accordion catalog-filter-accordion" id="filterAccordion">
        <div class="accordion-item">
          <h2 class="accordion-header" id="filterHeading">
            <button class="accordion-button" type="button" data-bs-toggle="collapse" data-bs-target="#filterPanel" aria-expanded="true" aria-controls="filterPanel">
              <ui:icon name="filter"/> Bộ lọc sản phẩm
            </button>
          </h2>
          <div id="filterPanel" class="accordion-collapse collapse show" aria-labelledby="filterHeading">
            <div class="accordion-body">
              <form class="panel filter-sidebar-form" action="${pageContext.request.contextPath}/products" method="get">
                <input type="hidden" name="kind" value="<c:out value='${kind}'/>">
                <input type="hidden" name="sort" value="<c:out value='${sort}'/>">
                <div class="filter-group">
                  <label for="keyword">Tìm kiếm</label>
                  <input id="keyword" class="form-control" name="keyword" value="<c:out value='${keyword}'/>" placeholder="Tên sản phẩm...">
                </div>
                <div class="filter-group">
                  <label for="brand">Thương hiệu</label>
                  <select id="brand" class="form-select" name="brand">
                    <option value="">Tất cả</option>
                    <c:forEach var="item" items="${brands}">
                      <option value="<c:out value='${item}'/>" ${brand == item ? 'selected' : ''}>
                        <c:out value="${item}"/>
                      </option>
                    </c:forEach>
                  </select>
                </div>
                <div class="filter-group">
                  <label for="categoryId">Danh mục</label>
                  <select id="categoryId" class="form-select" name="categoryId">
                    <option value="">Tất cả</option>
                    <c:forEach var="item" items="${categories}">
                      <option value="${item.id}" ${categoryId == item.id ? 'selected' : ''}>
                        <c:out value="${item.name}"/>
                      </option>
                    </c:forEach>
                  </select>
                </div>
                <div class="filter-group">
                  <label>Khoảng giá nhanh</label>
                  <div class="price-presets">
                    <button type="button" class="price-preset-btn" data-price-preset data-min="" data-max="10000000">Dưới 10tr</button>
                    <button type="button" class="price-preset-btn" data-price-preset data-min="10000000" data-max="20000000">10 – 20tr</button>
                    <button type="button" class="price-preset-btn" data-price-preset data-min="20000000" data-max="">Trên 20tr</button>
                  </div>
                  <div class="price-range-inputs">
                    <div>
                      <label for="minPrice">Giá từ</label>
                      <input id="minPrice" class="form-control" name="minPrice" type="number" min="0" value="${minPrice}" placeholder="0">
                    </div>
                    <div>
                      <label for="maxPrice">Giá đến</label>
                      <input id="maxPrice" class="form-control" name="maxPrice" type="number" min="0" value="${maxPrice}" placeholder="50000000">
                    </div>
                  </div>
                </div>
                <div class="filter-group">
                  <label for="storage">Bộ nhớ trong</label>
                  <select id="storage" name="storage" class="form-select">
                    <option value="">Tất cả</option>
                    <c:forEach var="item" items="${storageOptions}"><option value="<c:out value='${item}'/>" ${storage == item ? 'selected' : ''}><c:out value="${item}"/></option></c:forEach>
                  </select>
                </div>
                <div class="filter-group">
                  <label for="ram">RAM</label>
                  <select id="ram" name="ram" class="form-select">
                    <option value="">Tất cả</option>
                    <c:forEach var="item" items="${ramOptions}"><option value="<c:out value='${item}'/>" ${ram == item ? 'selected' : ''}><c:out value="${item}"/></option></c:forEach>
                  </select>
                </div>
                <div class="filter-group">
                  <label for="color">Màu sắc</label>
                  <select id="color" name="color" class="form-select">
                    <option value="">Tất cả màu</option>
                    <c:forEach var="item" items="${colorOptions}"><option value="<c:out value='${item}'/>" ${color == item ? 'selected' : ''}><c:out value="${item}"/></option></c:forEach>
                  </select>
                </div>
                <div class="filter-group">
                  <label for="purpose">Nhu cầu</label>
                  <select id="purpose" name="purpose" class="form-select">
                    <option value="">Tất cả nhu cầu</option>
                    <option value="EVERYDAY" ${purpose == 'EVERYDAY' ? 'selected' : ''}>Sử dụng hằng ngày</option>
                    <option value="CAMERA" ${purpose == 'CAMERA' ? 'selected' : ''}>Chụp ảnh</option>
                    <option value="GAMING" ${purpose == 'GAMING' ? 'selected' : ''}>Chơi game</option>
                    <option value="BATTERY" ${purpose == 'BATTERY' ? 'selected' : ''}>Ưu tiên pin</option>
                    <option value="COMPACT" ${purpose == 'COMPACT' ? 'selected' : ''}>Thiết kế gọn</option>
                  </select>
                </div>
                <div class="filter-group">
                  <label for="inStock">Tình trạng hàng</label>
                  <select id="inStock" name="inStock" class="form-select">
                    <option value="false" ${not inStock ? 'selected' : ''}>Tất cả</option>
                    <option value="true" ${inStock ? 'selected' : ''}>Chỉ còn hàng</option>
                  </select>
                </div>
                <div class="filter-actions">
                  <button class="btn btn-brand w-100">Áp dụng bộ lọc</button>
                  <a class="btn btn-light w-100" href="${pageContext.request.contextPath}/products">Đặt lại</a>
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>
    </div>
  </aside>
  <div class="catalog-main">
    <div class="catalog-toolbar">
      <nav class="catalog-sort" aria-label="Sắp xếp sản phẩm"><span>Sắp xếp theo:</span>
        <c:forTokens items="newest,price-asc,price-desc,bestselling,rating" delims="," var="ordering">
          <c:url var="sortUrl" value="/products"><c:param name="color" value="${color}"/><c:param name="purpose" value="${purpose}"/><c:param name="kind" value="${kind}"/><c:param name="keyword" value="${keyword}"/><c:param name="brand" value="${brand}"/><c:param name="categoryId" value="${categoryId}"/><c:param name="minPrice" value="${minPrice}"/><c:param name="maxPrice" value="${maxPrice}"/><c:param name="storage" value="${storage}"/><c:param name="ram" value="${ram}"/><c:param name="inStock" value="${inStock}"/><c:param name="sort" value="${ordering}"/></c:url>
          <a href="${sortUrl}" class="${sort == ordering ? 'is-active' : ''}">${ordering == 'newest' ? 'Mới nhất' : ordering == 'price-asc' ? 'Giá thấp → cao' : ordering == 'price-desc' ? 'Giá cao → thấp' : ordering == 'rating' ? 'Đánh giá cao' : 'Bán chạy'}</a>
        </c:forTokens>
      </nav>
      <span class="muted small d-none d-lg-inline">Hiển thị ${products.numberOfElements} / ${products.totalElements} sản phẩm</span>
    </div>
    <c:if test="${not empty keyword or not empty brand or not empty categoryId or not empty minPrice or not empty maxPrice or not empty storage or not empty ram or inStock or not empty color or not empty purpose}">
      <div class="applied-filters" aria-label="Bộ lọc đang áp dụng">
        <strong>Đang lọc:</strong>
        <c:if test="${not empty color}"><ui:filterChip name="color" label="Màu ${color}"/></c:if>
        <c:if test="${not empty purpose}"><ui:filterChip name="purpose" label="Nhu cầu: ${purpose}"/></c:if>
        <c:if test="${not empty keyword}"><ui:filterChip name="keyword" label="Từ khóa: ${keyword}"/></c:if>
        <c:if test="${not empty brand}"><ui:filterChip name="brand" label="${brand}"/></c:if>
        <c:if test="${not empty categoryId}"><ui:filterChip name="categoryId" label="Danh mục đã chọn"/></c:if>
        <c:if test="${not empty storage}"><ui:filterChip name="storage" label="${storage}"/></c:if>
        <c:if test="${not empty ram}"><ui:filterChip name="ram" label="RAM ${ram}"/></c:if>
        <c:if test="${not empty minPrice}"><ui:filterChip name="minPrice" label="Giá từ ${minPrice}đ"/></c:if>
        <c:if test="${not empty maxPrice}"><ui:filterChip name="maxPrice" label="Giá đến ${maxPrice}đ"/></c:if>
        <c:if test="${inStock}"><ui:filterChip name="inStock" label="Còn hàng"/></c:if>
        <a class="btn btn-sm btn-light" href="${pageContext.request.contextPath}/products">Xóa tất cả</a>
      </div>
    </c:if>
    <div class="product-grid catalog-grid">
      <c:forEach var="product" items="${products.content}">
        <ui:productCard product="${product}" selectedVariant="${selectedVariants[product.id]}"/>
      </c:forEach>
    </div>
    <c:if test="${empty products.content}">
      <div class="empty-state">
        <h2>Chưa tìm thấy sản phẩm phù hợp</h2>
        <p>Thử thay đổi từ khóa, khoảng giá hoặc xóa bớt tiêu chí lọc.</p>
        <a class="btn btn-brand" href="${pageContext.request.contextPath}/products">Xem tất cả sản phẩm</a>
      </div>
    </c:if>
    <c:if test="${products.totalPages > 1}">
      <nav class="pagination-row" aria-label="Phân trang">
        <c:forEach begin="0" end="${products.totalPages-1}" var="number">
          <c:url var="pageUrl" value="/products">
            <c:param name="color" value="${color}"/><c:param name="purpose" value="${purpose}"/><c:param name="kind" value="${kind}"/><c:param name="keyword" value="${keyword}"/>
            <c:param name="brand" value="${brand}"/>
            <c:param name="categoryId" value="${categoryId}"/>
            <c:param name="minPrice" value="${minPrice}"/>
            <c:param name="maxPrice" value="${maxPrice}"/>
            <c:param name="sort" value="${sort}"/>
            <c:param name="storage" value="${storage}"/>
            <c:param name="ram" value="${ram}"/>
            <c:param name="inStock" value="${inStock}"/>
            <c:param name="page" value="${number}"/>
          </c:url>
          <a class="btn ${number == products.number ? 'btn-brand' : 'btn-light'}" href="${pageUrl}">${number+1}</a>
        </c:forEach>
      </nav>
    </c:if>
  </div>
</div>
<c:if test="${not empty recentProducts}">
  <section class="collection-section">
    <div class="section-heading">
      <div>
        <span class="eyebrow">TIẾP TỤC XEM LẠI</span>
        <h2>Sản phẩm đã xem gần đây</h2>
      </div>
      <a href="${pageContext.request.contextPath}/recently-viewed">Xem toàn bộ →</a>
    </div>
    <div class="product-grid">
      <c:forEach var="recent" items="${recentProducts}">
        <ui:productCard product="${recent}"/>
      </c:forEach>
    </div>
  </section>
</c:if>
<jsp:include page="/WEB-INF/views/shared/store/footer.jsp" />
