package iuh.fit.www_phone_ver1.controller.admin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import org.springframework.dao.DataAccessException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

final class AdminBatch {
  private AdminBatch() {}

  @FunctionalInterface
  interface Operation {
    String apply(Long id) throws IOException;
  }

  static void run(List<Long> ids, Operation operation, RedirectAttributes flash) {
    if (ids == null || ids.isEmpty()) {
      flash.addFlashAttribute("errorMsg", "Hãy chọn ít nhất một dòng.");
      return;
    }
    int completed = 0;
    var details = new ArrayList<String>();
    var failures = new ArrayList<String>();
    for (Long id : new LinkedHashSet<>(ids)) {
      try {
        String note = operation.apply(id);
        completed++;
        if (note != null) details.add("#" + id + " " + note);
      } catch (IllegalArgumentException ex) {
        failures.add("#" + id + " " + ex.getMessage());
      } catch (DataAccessException | IOException ex) {
        failures.add("#" + id + " không thể lưu thay đổi. Hãy tải lại và kiểm tra dữ liệu.");
      }
    }
    if (completed > 0) {
      flash.addFlashAttribute("successMsg", "Đã xử lý " + completed + " mục."
          + (details.isEmpty() ? "" : " " + String.join(" · ", details)));
    }
    if (!failures.isEmpty()) flash.addFlashAttribute("errorMsg", String.join(" · ", failures));
  }
}
