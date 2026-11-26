package iuh.fit.www_phone_ver1.service.order;

import java.text.Normalizer;
import java.util.*;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

/** Public administrative codes; these are not carrier-specific internal IDs. */
@Service
public class AddressDirectory {
  public record Ward(String code, String name) {
    public String getCode() {
      return code;
    }

    public String getName() {
      return name;
    }
  }

  public record Province(String code, String name, List<Ward> wards) {
    public String getCode() {
      return code;
    }

    public String getName() {
      return name;
    }

    public List<Ward> getWards() {
      return wards;
    }
  }

  public record Address(String provinceCode, String province, String wardCode, String ward) {}

  private final List<Province> provinces;

  public AddressDirectory() throws java.io.IOException {
    try (var input = getClass().getResourceAsStream("/data/vietnam-addresses.json")) {
      if (input == null) throw new IllegalStateException("Missing address directory");
      var root = new JsonMapper().readTree(input);
      var all = new ArrayList<Province>();
      for (var p : root.path("provinces")) {
        var wards = new ArrayList<Ward>();
        for (var w : p.path("wards"))
          wards.add(new Ward(w.path("code").asString(), w.path("name").asString()));
        all.add(
            new Province(p.path("code").asString(), p.path("name").asString(), List.copyOf(wards)));
      }
      provinces = List.copyOf(all);
    }
  }

  public List<Province> provinces() {
    return provinces;
  }

  public List<Ward> wards(String code) {
    return provinces.stream()
        .filter(p -> p.code().equals(code))
        .findFirst()
        .map(Province::wards)
        .orElse(List.of());
  }

  private static String normalize(String s) {
    return Normalizer.normalize(
            Objects.toString(s, "").toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
        .replaceAll("\\p{M}", "")
        .replace('đ', 'd')
        .trim()
        .replaceAll("\\s+", " ");
  }

  public Address resolve(String pCode, String wCode, String pName, String wName, boolean required) {
    boolean noCodes = pCode == null || pCode.isBlank();
    if (!required && noCodes && (wCode == null || wCode.isBlank())) return null;
    if (noCodes && (pName == null || pName.isBlank()) && (wCode == null || wCode.isBlank())) {
      if (required) throw new IllegalArgumentException("Vui lòng chọn tỉnh/thành và phường/xã.");
      return null;
    }
    var p =
        provinces.stream()
            .filter(
                v ->
                    noCodes ? normalize(v.name()).equals(normalize(pName)) : v.code().equals(pCode))
            .findFirst()
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "Tỉnh/thành chưa hợp lệ. Hãy chọn lại từ danh sách."));
    var w =
        p.wards().stream()
            .filter(
                v ->
                    wCode == null || wCode.isBlank()
                        ? normalize(v.name()).equals(normalize(wName))
                        : v.code().equals(wCode))
            .findFirst()
            .orElseThrow(
                () -> new IllegalArgumentException("Phường/xã không thuộc tỉnh/thành đã chọn."));
    return new Address(p.code(), p.name(), w.code(), w.name());
  }
}
