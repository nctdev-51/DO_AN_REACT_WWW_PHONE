package iuh.fit.www_phone_ver1.service.order;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import iuh.fit.www_phone_ver1.entity.Order;
import iuh.fit.www_phone_ver1.entity.OrderDetail;
import iuh.fit.www_phone_ver1.repository.OrderRepository;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class InvoicePdfService {
  private static final Logger log = LoggerFactory.getLogger(InvoicePdfService.class);
  private static final DateTimeFormatter DATE_FORMAT =
      DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
  private static final Color BRAND_DARK = new Color(15, 23, 42);
  private static final Color BRAND_ACCENT = new Color(37, 99, 235);
  private static final Color BORDER_LIGHT = new Color(226, 232, 240);
  private static final Color BG_HEADER = new Color(241, 245, 249);
  private static final Color TEXT_MUTED = new Color(100, 116, 139);

  private final OrderRepository orders;

  public InvoicePdfService(OrderRepository orders) {
    this.orders = orders;
  }

  @Transactional(readOnly = true)
  public byte[] generateCustomerInvoice(Long orderId, String email) {
    Order order =
        orders
            .findByIdAndUser_Email(orderId, email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    return validateAndRender(order);
  }

  @Transactional(readOnly = true)
  public byte[] generateAdminInvoice(Long orderId) {
    Order order =
        orders
            .findById(orderId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    return validateAndRender(order);
  }

  private byte[] validateAndRender(Order order) {
    if (OrderStatuses.CANCELLED.equals(order.getStatus())) {
      throw new IllegalStateException("Đơn hàng đã hủy không thể xuất hóa đơn.");
    }
    // Khởi tạo các quan hệ lazy bên trong transaction để tránh LazyInitializationException
    if (order.getOrderDetails() != null) {
      order.getOrderDetails().forEach(
          d -> {
            if (d.getProduct() != null) d.getProduct().getName();
            if (d.getVariant() != null) d.getVariant().getLabel();
          });
    }
    if (order.getUser() != null) {
      order.getUser().getFullName();
    }
    return renderPdf(order);
  }

  public byte[] renderPdf(Order order) {
    if (order == null) {
      throw new IllegalArgumentException("Không có dữ liệu đơn hàng để xuất hóa đơn.");
    }

    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      Document document = new Document(PageSize.A4, 36, 36, 36, 36);
      PdfWriter.getInstance(document, out);
      document.open();

      BaseFont regularBase = loadFont("fonts/arial.ttf", BaseFont.HELVETICA);
      BaseFont boldBase = loadFont("fonts/arialbd.ttf", BaseFont.HELVETICA_BOLD);

      Font titleFont = new Font(boldBase, 18, Font.NORMAL, BRAND_DARK);
      Font brandFont = new Font(boldBase, 14, Font.NORMAL, BRAND_ACCENT);
      Font sectionFont = new Font(boldBase, 11, Font.NORMAL, BRAND_DARK);
      Font normalFont = new Font(regularBase, 9.5f, Font.NORMAL, BRAND_DARK);
      Font boldFont = new Font(boldBase, 9.5f, Font.NORMAL, BRAND_DARK);
      Font mutedFont = new Font(regularBase, 8.5f, Font.NORMAL, TEXT_MUTED);
      Font totalFont = new Font(boldBase, 12, Font.NORMAL, BRAND_ACCENT);

      // 1. Header: Thông tin cửa hàng & Tiêu đề hóa đơn
      PdfPTable headerTable = new PdfPTable(new float[] {1.3f, 1f});
      headerTable.setWidthPercentage(100);
      headerTable.setSpacingAfter(14);

      PdfPCell storeCell = new PdfPCell();
      storeCell.setBorder(Rectangle.NO_BORDER);
      storeCell.addElement(new Paragraph("PTV MOBILE", brandFont));
      storeCell.addElement(new Paragraph("Hệ thống bán lẻ điện thoại & phụ kiện chính hãng", normalFont));
      storeCell.addElement(new Paragraph("12 Nguyễn Văn Bảo, P.4, Q.Gò Vấp, TP.HCM", mutedFont));
      storeCell.addElement(new Paragraph("Hotline: 1900 1234 · support@ptvmobile.vn", mutedFont));
      headerTable.addCell(storeCell);

      boolean isOfficialInvoice =
          "PAID".equals(order.getPaymentStatus())
              || OrderStatuses.DELIVERED.equals(order.getStatus());
      String docTitle = isOfficialInvoice ? "HÓA ĐƠN BÁN HÀNG" : "PHIẾU XÁC NHẬN ĐƠN HÀNG";
      LocalDateTime orderTime =
          order.getOrderDate() != null ? order.getOrderDate() : LocalDateTime.now();

      PdfPCell metaCell = new PdfPCell();
      metaCell.setBorder(Rectangle.NO_BORDER);
      metaCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
      Paragraph pTitle = new Paragraph(docTitle, titleFont);
      pTitle.setAlignment(Element.ALIGN_RIGHT);
      metaCell.addElement(pTitle);

      Paragraph pCode = new Paragraph("Mã đơn hàng: #" + Objects.toString(order.getId(), "---"), boldFont);
      pCode.setAlignment(Element.ALIGN_RIGHT);
      metaCell.addElement(pCode);

      Paragraph pDate = new Paragraph("Ngày đặt: " + orderTime.format(DATE_FORMAT), mutedFont);
      pDate.setAlignment(Element.ALIGN_RIGHT);
      metaCell.addElement(pDate);

      Paragraph pStatus =
          new Paragraph("Trạng thái đơn: " + Objects.toString(order.getStatus(), "---"), mutedFont);
      pStatus.setAlignment(Element.ALIGN_RIGHT);
      metaCell.addElement(pStatus);

      headerTable.addCell(metaCell);
      document.add(headerTable);

      // 2. Thông tin khách hàng & Thanh toán
      PdfPTable infoTable = new PdfPTable(new float[] {1.2f, 1f});
      infoTable.setWidthPercentage(100);
      infoTable.setSpacingAfter(14);

      String customerName =
          firstNonBlank(
              order.getRecipientName(),
              order.getUser() != null ? order.getUser().getFullName() : null,
              "Khách hàng");
      String customerPhone =
          firstNonBlank(
              order.getPhone(),
              order.getUser() != null ? order.getUser().getPhone() : null,
              "---");
      String customerEmail = order.getUser() != null ? Objects.toString(order.getUser().getEmail(), "---") : "---";
      String shippingAddr = firstNonBlank(order.getFullShippingAddress(), order.getShippingAddress(), "---");

      PdfPCell customerCell = createBoxCell();
      customerCell.addElement(new Paragraph("THÔNG TIN NHẬN HÀNG", sectionFont));
      customerCell.addElement(new Paragraph("Người nhận: " + customerName, boldFont));
      customerCell.addElement(new Paragraph("Điện thoại: " + customerPhone, normalFont));
      customerCell.addElement(new Paragraph("Email: " + customerEmail, normalFont));
      customerCell.addElement(new Paragraph("Địa chỉ: " + shippingAddr, normalFont));
      infoTable.addCell(customerCell);

      PdfPCell paymentCell = createBoxCell();
      paymentCell.addElement(new Paragraph("THÔNG TIN THANH TOÁN", sectionFont));
      paymentCell.addElement(
          new Paragraph(
              "Phương thức: " + Objects.toString(order.getPaymentMethod(), "COD"), boldFont));
      paymentCell.addElement(
          new Paragraph(
              "Trạng thái: " + translatePaymentStatus(order.getPaymentStatus()), normalFont));
      if (order.getPaymentReference() != null && !order.getPaymentReference().isBlank()) {
        paymentCell.addElement(
            new Paragraph("Mã tham chiếu: " + order.getPaymentReference(), mutedFont));
      }
      if (order.getGatewayTransaction() != null && !order.getGatewayTransaction().isBlank()) {
        paymentCell.addElement(
            new Paragraph("Mã giao dịch: " + order.getGatewayTransaction(), mutedFont));
      }
      if (order.getTrackingCode() != null && !order.getTrackingCode().isBlank()) {
        paymentCell.addElement(
            new Paragraph(
                "Vận chuyển: "
                    + Objects.toString(order.getCarrier(), "")
                    + " ("
                    + order.getTrackingCode()
                    + ")",
                mutedFont));
      }
      infoTable.addCell(paymentCell);
      document.add(infoTable);

      // 3. Bảng chi tiết sản phẩm
      PdfPTable itemsTable = new PdfPTable(new float[] {0.5f, 3.2f, 1.2f, 0.6f, 1.3f, 1.4f});
      itemsTable.setWidthPercentage(100);
      itemsTable.setHeaderRows(1);
      itemsTable.setSpacingAfter(12);

      addHeaderCell(itemsTable, "STT", boldFont, Element.ALIGN_CENTER);
      addHeaderCell(itemsTable, "Sản phẩm & Phân loại", boldFont, Element.ALIGN_LEFT);
      addHeaderCell(itemsTable, "SKU", boldFont, Element.ALIGN_LEFT);
      addHeaderCell(itemsTable, "SL", boldFont, Element.ALIGN_CENTER);
      addHeaderCell(itemsTable, "Đơn giá", boldFont, Element.ALIGN_RIGHT);
      addHeaderCell(itemsTable, "Thành tiền", boldFont, Element.ALIGN_RIGHT);

      List<OrderDetail> details =
          order.getOrderDetails() != null ? order.getOrderDetails() : List.of();
      int index = 1;
      for (OrderDetail detail : details) {
        int qty = detail.getQuantity() != null ? detail.getQuantity() : 0;
        BigDecimal unitPrice = nz(detail.getPrice());
        BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(qty));

        addBodyCell(itemsTable, String.valueOf(index++), normalFont, Element.ALIGN_CENTER);

        String itemName = safeDisplayName(detail);
        String variantLabel = Objects.toString(detail.getVariantLabel(), "");
        String fullItemText = variantLabel.isBlank() ? itemName : itemName + "\n(" + variantLabel + ")";
        addBodyCell(itemsTable, fullItemText, normalFont, Element.ALIGN_LEFT);

        addBodyCell(itemsTable, firstNonBlank(detail.getSku(), "---"), mutedFont, Element.ALIGN_LEFT);
        addBodyCell(itemsTable, String.valueOf(qty), normalFont, Element.ALIGN_CENTER);
        addBodyCell(itemsTable, formatMoney(unitPrice), normalFont, Element.ALIGN_RIGHT);
        addBodyCell(itemsTable, formatMoney(lineTotal), boldFont, Element.ALIGN_RIGHT);
      }

      if (details.isEmpty()) {
        PdfPCell emptyCell = new PdfPCell(new Phrase("Không có sản phẩm nào trong đơn hàng.", mutedFont));
        emptyCell.setColspan(6);
        emptyCell.setPadding(10);
        emptyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        itemsTable.addCell(emptyCell);
      }
      document.add(itemsTable);

      // 4. Bảng tổng kết tài chính
      PdfPTable summaryTable = new PdfPTable(new float[] {1.4f, 1f});
      summaryTable.setWidthPercentage(50);
      summaryTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
      summaryTable.setSpacingAfter(20);

      BigDecimal subtotal = nz(order.getSubtotal());
      BigDecimal discount = nz(order.getDiscountAmount());
      BigDecimal pointsDiscount = nz(order.getPointsDiscount());
      BigDecimal shippingFee = nz(order.getShippingFee());
      BigDecimal totalAmount = nz(order.getTotalAmount());

      addSummaryRow(summaryTable, "Tạm tính:", formatMoney(subtotal), normalFont, normalFont);
      if (discount.compareTo(BigDecimal.ZERO) > 0) {
        String promoLabel =
            order.getPromotionCode() != null && !order.getPromotionCode().isBlank()
                ? "Giảm giá (" + order.getPromotionCode() + "):"
                : "Giảm giá khuyến mãi:";
        addSummaryRow(summaryTable, promoLabel, "- " + formatMoney(discount), normalFont, normalFont);
      }
      if (pointsDiscount.compareTo(BigDecimal.ZERO) > 0) {
        addSummaryRow(
            summaryTable,
            "Đổi " + order.getPointsSpent() + " điểm thưởng:",
            "- " + formatMoney(pointsDiscount),
            normalFont,
            normalFont);
      }
      addSummaryRow(summaryTable, "Phí vận chuyển:", formatMoney(shippingFee), normalFont, normalFont);

      PdfPCell totalLabelCell = new PdfPCell(new Phrase("TỔNG THANH TOÁN:", boldFont));
      totalLabelCell.setBorder(Rectangle.TOP);
      totalLabelCell.setBorderColor(BRAND_DARK);
      totalLabelCell.setPaddingTop(8);
      totalLabelCell.setPaddingBottom(4);
      summaryTable.addCell(totalLabelCell);

      PdfPCell totalValCell = new PdfPCell(new Phrase(formatMoney(totalAmount), totalFont));
      totalValCell.setBorder(Rectangle.TOP);
      totalValCell.setBorderColor(BRAND_DARK);
      totalValCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
      totalValCell.setPaddingTop(8);
      totalValCell.setPaddingBottom(4);
      summaryTable.addCell(totalValCell);

      document.add(summaryTable);

      // 5. Ghi chú chân trang
      Paragraph footer =
          new Paragraph(
              "Cảm ơn quý khách đã mua sắm tại PTV Mobile! Vui lòng giữ lại hóa đơn để được hỗ trợ bảo hành chính hãng.",
              mutedFont);
      footer.setAlignment(Element.ALIGN_CENTER);
      document.add(footer);

      document.close();
      return out.toByteArray();
    } catch (DocumentException | IOException e) {
      log.error("Lỗi tạo PDF hóa đơn cho đơn #{}: {}", order.getId(), e.getMessage(), e);
      throw new IllegalStateException("Không thể tạo file PDF hóa đơn lúc này.", e);
    }
  }

  private BaseFont loadFont(String classpathLocation, String fallbackFont) {
    try {
      ClassPathResource resource = new ClassPathResource(classpathLocation);
      if (resource.exists()) {
        try (InputStream is = resource.getInputStream()) {
          byte[] bytes = is.readAllBytes();
          return BaseFont.createFont(
              classpathLocation, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true, bytes, null);
        }
      }
    } catch (Exception ex) {
      log.warn("Không nạp được font classpath {}: {}", classpathLocation, ex.getMessage());
    }
    try {
      return BaseFont.createFont(fallbackFont, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
    } catch (Exception ex) {
      throw new IllegalStateException("Không khởi tạo được font PDF", ex);
    }
  }

  private PdfPCell createBoxCell() {
    PdfPCell cell = new PdfPCell();
    cell.setBorderColor(BORDER_LIGHT);
    cell.setBorderWidth(1f);
    cell.setPadding(8f);
    return cell;
  }

  private void addHeaderCell(PdfPTable table, String text, Font font, int align) {
    PdfPCell cell = new PdfPCell(new Phrase(text, font));
    cell.setBackgroundColor(BG_HEADER);
    cell.setBorderColor(BORDER_LIGHT);
    cell.setPadding(7f);
    cell.setHorizontalAlignment(align);
    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
    table.addCell(cell);
  }

  private void addBodyCell(PdfPTable table, String text, Font font, int align) {
    PdfPCell cell = new PdfPCell(new Phrase(text, font));
    cell.setBorderColor(BORDER_LIGHT);
    cell.setPadding(6f);
    cell.setHorizontalAlignment(align);
    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
    table.addCell(cell);
  }

  private void addSummaryRow(
      PdfPTable table, String label, String value, Font labelFont, Font valFont) {
    PdfPCell left = new PdfPCell(new Phrase(label, labelFont));
    left.setBorder(Rectangle.NO_BORDER);
    left.setPadding(4f);
    table.addCell(left);

    PdfPCell right = new PdfPCell(new Phrase(value, valFont));
    right.setBorder(Rectangle.NO_BORDER);
    right.setHorizontalAlignment(Element.ALIGN_RIGHT);
    right.setPadding(4f);
    table.addCell(right);
  }

  private String safeDisplayName(OrderDetail detail) {
    if (detail.getProductName() != null && !detail.getProductName().isBlank()) {
      return detail.getProductName();
    }
    if (detail.getProduct() != null && detail.getProduct().getName() != null) {
      return detail.getProduct().getName();
    }
    return "Sản phẩm";
  }

  private String translatePaymentStatus(String status) {
    if (status == null) return "Chưa thanh toán";
    return switch (status) {
      case "PAID" -> "Đã thanh toán";
      case "UNPAID" -> "Chưa thanh toán";
      case "FAILED" -> "Thanh toán thất bại";
      case "REFUND_REQUIRED" -> "Cần hoàn tiền";
      case "PARTIALLY_REFUNDED" -> "Hoàn tiền một phần";
      case "REFUNDED" -> "Đã hoàn tiền";
      default -> status;
    };
  }

  private String formatMoney(BigDecimal amount) {
    NumberFormat nf = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));
    nf.setMaximumFractionDigits(0);
    return nf.format(nz(amount)) + " đ";
  }

  private BigDecimal nz(BigDecimal value) {
    return value == null ? BigDecimal.ZERO : value;
  }

  private String firstNonBlank(String... candidates) {
    for (String c : candidates) {
      if (c != null && !c.isBlank()) return c.trim();
    }
    return "---";
  }
}
