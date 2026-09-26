package iuh.fit.www_phone_ver1.config;

import iuh.fit.www_phone_ver1.entity.Category;
import iuh.fit.www_phone_ver1.entity.Product;
import iuh.fit.www_phone_ver1.repository.CategoryRepository;
import iuh.fit.www_phone_ver1.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@Profile("demo")
public class DemoDataInitializer implements CommandLineRunner {
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public DemoDataInitializer(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) {
        if (productRepository.count() > 0) {
            return;
        }

        Map<String, Category> categories = new LinkedHashMap<>();
        categories.put("Apple", createCategory("Apple", "Điện thoại Apple iPhone."));
        categories.put("Samsung", createCategory("Samsung", "Điện thoại Samsung Galaxy."));
        categories.put("Xiaomi", createCategory("Xiaomi", "Điện thoại Xiaomi."));
        categories.put("OPPO", createCategory("OPPO", "Điện thoại OPPO."));

        List<DemoProduct> samples = List.of(
                new DemoProduct("iPhone 16 Pro Max 256GB", "Apple", 34990000, "Màn hình Super Retina XDR, chip A18 Pro."),
                new DemoProduct("iPhone 16 Pro 128GB", "Apple", 28990000, "Thiết kế titan, camera chuyên nghiệp."),
                new DemoProduct("iPhone 16 128GB", "Apple", 22990000, "Hiệu năng mạnh mẽ cùng Apple Intelligence."),
                new DemoProduct("iPhone 15 128GB", "Apple", 17990000, "Camera 48MP, thiết kế Dynamic Island."),
                new DemoProduct("Galaxy S25 Ultra 256GB", "Samsung", 33990000, "Galaxy AI, camera 200MP, S Pen."),
                new DemoProduct("Galaxy S25+ 256GB", "Samsung", 26990000, "Màn hình Dynamic AMOLED 2X sắc nét."),
                new DemoProduct("Galaxy S25 256GB", "Samsung", 22990000, "Flagship nhỏ gọn, hiệu năng Galaxy AI."),
                new DemoProduct("Galaxy A56 5G 256GB", "Samsung", 10990000, "Màn hình lớn, pin bền bỉ, kết nối 5G."),
                new DemoProduct("Xiaomi 15 Ultra 512GB", "Xiaomi", 29990000, "Hệ thống camera Leica cao cấp."),
                new DemoProduct("Xiaomi 15 256GB", "Xiaomi", 19990000, "Hiệu năng flagship trong thiết kế nhỏ gọn."),
                new DemoProduct("Redmi Note 14 Pro+ 5G", "Xiaomi", 10990000, "Camera độ phân giải cao, sạc nhanh."),
                new DemoProduct("Redmi Note 14 256GB", "Xiaomi", 6490000, "Màn hình AMOLED, pin dung lượng lớn."),
                new DemoProduct("OPPO Find X8 Pro 512GB", "OPPO", 29990000, "Camera Hasselblad và sạc nhanh."),
                new DemoProduct("OPPO Reno13 Pro 5G", "OPPO", 18990000, "Thiết kế mỏng nhẹ, camera chân dung."),
                new DemoProduct("OPPO A5 Pro 5G", "OPPO", 7990000, "Điện thoại 5G bền bỉ cho nhu cầu hằng ngày.")
        );

        for (DemoProduct sample : samples) {
            Product product = new Product();
            product.setName(sample.name());
            product.setBrand(sample.brand());
            product.setPrice((double) sample.price());
            product.setDescription(sample.description());
            product.setStatus(1);
            product.setCategory(categories.get(sample.brand()));
            productRepository.save(product);
        }
    }

    private Category createCategory(String name, String description) {
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        return categoryRepository.save(category);
    }

    private record DemoProduct(String name, String brand, long price, String description) {
    }
}
