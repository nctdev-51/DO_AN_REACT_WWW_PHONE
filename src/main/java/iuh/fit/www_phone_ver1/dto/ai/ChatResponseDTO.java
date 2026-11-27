package iuh.fit.www_phone_ver1.dto.ai;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponseDTO {
    private String reply;
    private List<Long> suggestedProducts;
    private List<ProductCardDTO> productCards;

    public ChatResponseDTO(String reply, List<Long> suggestedProducts) {
        this.reply = reply;
        this.suggestedProducts = suggestedProducts;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductCardDTO {
        private Long id;
        private String name;
        private String price;
        private String brand;
        private String imageUrl;

        public ProductCardDTO(Long id, String name, String price, String brand) {
            this(id, name, price, brand, null);
        }
    }
}
