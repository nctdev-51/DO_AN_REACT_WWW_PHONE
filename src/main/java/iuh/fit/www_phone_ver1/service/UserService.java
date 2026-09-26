package iuh.fit.www_phone_ver1.service;

import iuh.fit.www_phone_ver1.dto.UserRegistrationDto;
import iuh.fit.www_phone_ver1.entity.Role;
import iuh.fit.www_phone_ver1.entity.User;
import iuh.fit.www_phone_ver1.repository.RoleRepository;
import iuh.fit.www_phone_ver1.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void registerUser(UserRegistrationDto registrationDto) {
        if (!registrationDto.getPassword().equals(registrationDto.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu xác nhận không khớp");
        }

        if (userRepository.findByEmail(registrationDto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email đã được sử dụng");
        }

        User user = new User();
        user.setFullName(registrationDto.getFullName());
        user.setEmail(registrationDto.getEmail());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        user.setPhone(registrationDto.getPhone());
        user.setAddress(registrationDto.getAddress());
        user.setStatus(1);

        Optional<Role> roleOpt = roleRepository.findByName("USER");
        Role role = roleOpt.orElseGet(() -> {
            Role newRole = new Role();
            newRole.setName("USER");
            return roleRepository.save(newRole);
        });

        user.getRoles().add(role);

        userRepository.save(user);
    }
}
