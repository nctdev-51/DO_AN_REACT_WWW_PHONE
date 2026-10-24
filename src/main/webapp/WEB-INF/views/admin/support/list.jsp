<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<jsp:include page="/WEB-INF/views/shared/admin/header.jsp" />
<p>
  Ghi lại kết quả kiểm tra, phương án đổi/trả hoặc bảo hành và thông tin bàn giao trong phản hồi cho khách.
</p>
<c:forEach var="ticket" items="${requestPage.content}">
  <article class="panel mb-4">
    <div class="section-heading">
      <h2>
        #${ticket.id} ·
        <c:out value="${ticket.typeLabel}"/>
      </h2>
      <span class="status-badge">
        <c:out value="${ticket.statusLabel}"/>
      </span>
    </div>
    <p>
      <strong>
        <c:out value="${ticket.orderDetail.order.user.fullName}"/>
      </strong>
      ·
      <c:out value="${ticket.orderDetail.order.phone}"/>
      ·
      <a href="${pageContext.request.contextPath}/admin/orders/${ticket.orderDetail.order.id}">
        Đơn #${ticket.orderDetail.order.id}
      </a>
    </p>
    <p>
      <c:out value="${ticket.orderDetail.displayName}"/>
      /
      <c:out value="${ticket.orderDetail.variantLabel}"/>
      × ${ticket.quantity}
    </p>
    <p class="description">
      <c:out value="${ticket.reason}"/>
    </p>
    <c:if test="${not empty ticket.response}">
      <p class="alert alert-info description">
        <c:out value="${ticket.response}"/>
      </p>
    </c:if>
    <c:if test="${ticket.status == 'REQUESTED' or ticket.status == 'APPROVED' or ticket.status == 'RECEIVED'}">
      <form action="${pageContext.request.contextPath}/admin/support/${ticket.id}/status" method="post">
        <ui:csrf/>
        <div class="field">
          <label for="status-${ticket.id}">
            Bước xử lý tiếp theo
          </label>
          <select id="status-${ticket.id}" name="status" class="form-select">
            <c:if test="${ticket.status == 'REQUESTED'}">
              <option value="APPROVED">
                Duyệt tiếp nhận
              </option>
              <option value="REJECTED">
                Từ chối
              </option>
            </c:if>
            <c:if test="${ticket.status == 'APPROVED'}">
              <option value="RECEIVED">
                Đã nhận thiết bị
              </option>
              <option value="REJECTED">
                Từ chối sau kiểm tra
              </option>
            </c:if>
            <c:if test="${ticket.status == 'RECEIVED'}">
              <option value="COMPLETED">
                Đã xử lý và bàn giao xong
              </option>
            </c:if>
          </select>
        </div>
        <div class="field">
          <label for="note-${ticket.id}">
            Phản hồi / kết quả xử lý
          </label>
          <textarea id="note-${ticket.id}" name="note" class="form-control" minlength="10" maxlength="2000" rows="3" required>
          </textarea>
        </div>
        <button class="btn btn-brand">
          Cập nhật hồ sơ
        </button>
      </form>
    </c:if>
    <c:if test="${ticket.inspected}"><p class="alert alert-info">Đã kiểm tra: <c:out value="${ticket.inspectionNote}"/> · ${ticket.restocked ? 'Đã nhập lại tồn' : 'Không nhập tồn bán mới'}</p></c:if>
    <c:if test="${ticket.type == 'RETURN' and (ticket.status == 'RECEIVED' or ticket.status == 'COMPLETED') and not ticket.inspected}"><details class="mt-4"><summary>Kiểm tra và xử lý tồn hàng trả</summary><form class="mt-3" method="post" action="${pageContext.request.contextPath}/admin/support/${ticket.id}/inspection"><ui:csrf/><div class="field"><label for="inspect-${ticket.id}">Kết quả kiểm tra thiết bị / phụ kiện</label><textarea id="inspect-${ticket.id}" name="note" class="form-control" minlength="10" maxlength="500" required></textarea></div><label class="mb-3"><input type="checkbox" name="resellable" value="true"> Đã kiểm tra đủ điều kiện nhập lại tồn bán mới (${ticket.quantity} sản phẩm)</label><p class="muted small">Không chọn nếu cần sửa chữa hoặc phân loại hàng đã dùng. Thao tác chỉ thực hiện một lần; hoàn tiền được đối soát riêng tại đơn hàng.</p><button class="btn btn-outline-dark">Lưu kết quả kiểm tra #${ticket.id}</button></form></details></c:if>
  </article>
</c:forEach>
<c:if test="${empty requestPage.content}">
  <div class="panel empty-state">
    Chưa có yêu cầu hỗ trợ.
  </div>
</c:if>
<nav class="pagination-row">
  <c:if test="${requestPage.hasPrevious()}">
    <a class="btn btn-light" href="?page=${requestPage.number-1}">
      Trang trước
    </a>
  </c:if>
  <c:if test="${requestPage.hasNext()}">
    <a class="btn btn-light" href="?page=${requestPage.number+1}">
      Trang sau
    </a>
  </c:if>
</nav>
<jsp:include page="/WEB-INF/views/shared/admin/footer.jsp" />
