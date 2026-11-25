package iuh.fit.www_phone_ver1.service.promotion;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("app.promotion.inventory")
public record InventoryPromotionRules(
    @Min(1) int minimumStock,
    @Min(1) int salesLimit,
    @Min(1) int salesDays,
    @Min(1) int mediumStock,
    @Min(1) int highStock,
    @Min(1) int mediumDays,
    @Min(1) int highDays,
    @Min(1) @Max(100) int baseDiscount,
    @Min(1) @Max(100) int mediumDiscount,
    @Min(1) @Max(100) int highDiscount) {}
