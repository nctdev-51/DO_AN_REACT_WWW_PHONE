package iuh.fit.www_phone_ver1.controller.common;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class UploadExceptionHandler {
  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public String tooLarge(HttpServletRequest request, RedirectAttributes flash) {
    flash.addFlashAttribute("error", "Ảnh tải lên vượt quá dung lượng cho phép (tối đa 30 MB).");
    String referer = request.getHeader("Referer");
    return (referer != null && !referer.isBlank()) ? "redirect:" + referer : "redirect:/admin/banners";
  }
}
