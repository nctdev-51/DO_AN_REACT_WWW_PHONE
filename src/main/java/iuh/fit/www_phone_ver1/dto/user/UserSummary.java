package iuh.fit.www_phone_ver1.dto.user;

import iuh.fit.www_phone_ver1.entity.User;
import lombok.Getter;

@Getter
public class UserSummary {
  private final Long id;
  private final String fullName;
  private final String email;
  private final String phone;
  private final Integer status;
  private final long orderCount;
  private final boolean admin;

  public UserSummary(User user, long orderCount) {
    this.id = user.getId();
    this.fullName = user.getFullName();
    this.email = user.getEmail();
    this.phone = user.getPhone();
    this.status = user.getStatus();
    this.orderCount = orderCount;
    this.admin = user.isAdmin();
  }
}
