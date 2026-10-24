package iuh.fit.www_phone_ver1.controller.common;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class UploadExceptionHandler {
  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public String tooLarge(RedirectAttributes flash) {
    flash.addFlashAttribute("error", "Ảnh tải lên vượt quá dung lượng cho phép (5 MB).");
    return "redirect:/admin/products";
  }
}
