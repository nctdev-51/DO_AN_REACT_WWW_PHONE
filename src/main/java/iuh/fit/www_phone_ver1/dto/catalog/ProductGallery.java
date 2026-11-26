package iuh.fit.www_phone_ver1.dto.catalog;

import java.util.List;
import lombok.*;

@Getter
@AllArgsConstructor
public class ProductGallery {
  private final List<Photo> photos;
  private final List<String> frames;
  private final String modelUrl;
  private final String modelCredit;

  @Getter
  @AllArgsConstructor
  public static class Photo {
    private final String url;
    private final String caption;
    private final boolean colorSpecific;
  }
}
