package iuh.fit.www_phone_ver1.service.payment;

public enum QrPlatform {
  VIETQR("VietQR", "#0066B3", "Vietcombank", "970436", "123456789", "PTV MOBILE"),
  MOMO("MoMo", "#A50064", "Ví MoMo", "0901234567", "0901234567", "Ví MoMo PTV MOBILE"),
  ZALOPAY("ZaloPay", "#0068FF", "Ví ZaloPay", "0901234567", "0901234567", "Ví ZaloPay PTV MOBILE"),
  VNPAY_QR("VNPay-QR", "#0F6BFF", "VNPay", "VNPAYPTV", "VNPAYPTV", "VNPay PTV MOBILE");

  private final String displayName;
  private final String color;
  private final String accountLabel;
  private final String accountNumber;
  private final String accountOwner;
  private final String contentPrefix;

  QrPlatform(String displayName, String color, String accountLabel, String accountNumber,
      String accountOwner, String contentPrefix) {
    this.displayName = displayName;
    this.color = color;
    this.accountLabel = accountLabel;
    this.accountNumber = accountNumber;
    this.accountOwner = accountOwner;
    this.contentPrefix = contentPrefix;
  }

  public String getDisplayName() { return displayName; }
  public String getColor() { return color; }
  public String getAccountLabel() { return accountLabel; }
  public String getAccountNumber() { return accountNumber; }
  public String getAccountOwner() { return accountOwner; }
  public String getContentPrefix() { return contentPrefix; }

  public static QrPlatform from(String value) {
    try {
      return value == null ? VIETQR : valueOf(value.trim().toUpperCase());
    } catch (IllegalArgumentException error) {
      return VIETQR;
    }
  }
}
