<%@ tag pageEncoding="UTF-8" %>
<%@ attribute name="banner" required="true" type="iuh.fit.www_phone_ver1.entity.CampaignBanner" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<section class="campaign-banner ${banner.theme == 'DARK' ? 'campaign-dark' : ''} ${banner.placement == 'TILE' ? 'campaign-tile' : ''} ${empty banner.imagePath ? 'campaign-banner-text-only' : ''}" data-reveal>
  <div>
    <span class="eyebrow">PTV MOBILE</span>
    <h2><c:out value="${banner.title}"/></h2>
    <c:if test="${not empty banner.subtitle}"><p><c:out value="${banner.subtitle}"/></p></c:if>
    <c:url var="destination" value="${banner.targetPath}"/>
    <a class="btn btn-brand" href="<c:out value='${destination}'/>"><c:out value="${banner.actionLabel}"/></a>
  </div>
  <c:if test="${not empty banner.imagePath}">
    <c:url var="imageUrl" value="${banner.imagePath}"/>
    <div class="campaign-banner-media">
      <img src="<c:out value='${imageUrl}'/>" alt="<c:out value='${banner.imageLabel}'/>" loading="lazy">
    </div>
  </c:if>
</section>
