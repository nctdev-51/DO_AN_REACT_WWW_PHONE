package iuh.fit.www_phone_ver1.service.ai;

import java.util.Arrays;
import java.util.Locale;

public enum ChatTopic {
  WARRANTY("Bảo hành", "bảo hành|warranty"),
  INSTALLMENTS("Trả góp", "trả góp|0%|installment"),
  ACCESSORIES("Phụ kiện đi kèm", "củ sạc|hộp có|đi kèm|tặng sạc|phụ kiện"),
  RETURNS("Đổi trả", "đổi trả|đổi máy|hoàn tiền"),
  SHIPPING("Giao hàng", "giao hàng|vận chuyển|ship"),
  PRICE("Giá và khuyến mãi", "giá|triệu|bao nhiêu tiền|khuyến mãi|ưu đãi"),
  BATTERY("Pin và sạc", "pin|sạc|mah|watt"),
  PERFORMANCE("Hiệu năng và game", "game|chip|snapdragon|ram|fps"),
  CAMERA("Camera", "camera|chụp|selfie|zoom"),
  OTHER("Tư vấn chung", "");

  private final String label;
  private final String terms;

  ChatTopic(String label, String terms) {
    this.label = label;
    this.terms = terms;
  }

  public String label() {
    return label;
  }

  public static ChatTopic classify(String question) {
    if (question == null) return OTHER;
    String lower = question.toLowerCase(Locale.ROOT);
    return Arrays.stream(values())
        .filter(topic -> topic != OTHER && Arrays.stream(topic.terms.split("\\|"))
            .anyMatch(lower::contains))
        .findFirst().orElse(OTHER);
  }
}
