<%@ tag pageEncoding="UTF-8" %>
<%@ attribute name="value" required="true" type="java.lang.Number" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<fmt:setLocale value="vi_VN"/>
<fmt:formatNumber value="${value}" pattern="#,##0" var="formattedMoney"/>
<span class="money">${fn:replace(formattedMoney, ',', '.')}&nbsp;₫</span>
