package com.elearning.config;

import com.elearning.model.User;
import com.elearning.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Tạo tài khoản Admin mặc định nếu chưa có
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("Admin@123"))
                    .email("admin@elearning.com")
                    .fullName("Quản Trị Viên")
                    .role(User.Role.ADMIN)
                    .enabled(true)
                    .build();
            userRepository.save(admin);
            log.info("=== Đã tạo tài khoản Admin mặc định: admin / Admin@123 ===");
        }

        // Tạo giáo viên demo
        if (!userRepository.existsByUsername("teacher01")) {
            User teacher = User.builder()
                    .username("teacher01")
                    .password(passwordEncoder.encode("Teacher@123"))
                    .email("teacher01@elearning.com")
                    .fullName("Nguyễn Văn Thầy")
                    .phone("0901234567")
                    .role(User.Role.TEACHER)
                    .enabled(true)
                    .build();
            userRepository.save(teacher);
            log.info("=== Đã tạo tài khoản Giáo viên demo: teacher01 / Teacher@123 ===");
        }

        // Tạo học sinh demo
        if (!userRepository.existsByUsername("student01")) {
            User student = User.builder()
                    .username("student01")
                    .password(passwordEncoder.encode("Student@123"))
                    .email("student01@elearning.com")
                    .fullName("Trần Thị Học")
                    .phone("0912345678")
                    .role(User.Role.STUDENT)
                    .enabled(true)
                    .build();
            userRepository.save(student);
            log.info("=== Đã tạo tài khoản Học sinh demo: student01 / Student@123 ===");
        }
    }
}
