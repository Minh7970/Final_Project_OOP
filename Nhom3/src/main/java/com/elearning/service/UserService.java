package com.elearning.service;

import com.elearning.dto.UserDto;
import com.elearning.exception.ResourceNotFoundException;
import com.elearning.model.User;
import com.elearning.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PersistenceContext
    private EntityManager entityManager;

    // ==================== TẠO USER ====================

    public User createUser(UserDto.CreateRequest request) {
        // Kiểm tra trùng username
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username '" + request.getUsername() + "' đã tồn tại!");
        }
        // Kiểm tra trùng email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email '" + request.getEmail() + "' đã được sử dụng!");
        }
        // Không cho phép tạo tài khoản Admin từ form
        if (request.getRole() == User.Role.ADMIN) {
            throw new IllegalArgumentException("Không thể tạo tài khoản Admin!");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .address(request.getAddress())
                .role(request.getRole())
                .enabled(true)
                .build();

        User saved = userRepository.save(user);
        log.info("Đã tạo user mới: {} - Role: {}", saved.getUsername(), saved.getRole());
        return saved;
    }

    // ==================== XEM DANH SÁCH ====================

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<User> getUsersByRole(User.Role role) {
        return userRepository.findByRole(role);
    }

    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    @Transactional(readOnly = true)
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }

    // ==================== CẬP NHẬT ====================

    public User updateUser(Long id, UserDto.UpdateRequest request) {
        User user = getUserById(id);

        // Kiểm tra email trùng (nếu thay đổi email)
        if (!user.getEmail().equals(request.getEmail()) &&
                userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email '" + request.getEmail() + "' đã được sử dụng!");
        }

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());

        // Chỉ cập nhật password nếu có nhập mới
        if (request.getNewPassword() != null && !request.getNewPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }

        log.info("Đã cập nhật user: {}", user.getUsername());
        return userRepository.save(user);
    }

    // Admin cập nhật role và trạng thái
    public User updateUserRoleAndStatus(Long id, User.Role role, Boolean enabled) {
        User user = getUserById(id);
        if (role != null) {
            if (role == User.Role.ADMIN) {
                throw new IllegalArgumentException("Không thể thay đổi vai trò thành Admin!");
            }
            user.setRole(role);
        }
        if (enabled != null) user.setEnabled(enabled);
        log.info("Admin cập nhật role/status user: {}", user.getUsername());
        return userRepository.save(user);
    }

    // ==================== XÓA ====================

    public void deleteUser(Long id) {
        log.info("=== deleteUser START id={} ===", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        log.info("Found user: enabled={}, role={}, username={}",
                user.getEnabled(), user.getRole(), user.getUsername());

        if (Boolean.TRUE.equals(user.getEnabled())) {
            throw new IllegalStateException("Vui lòng vô hiệu hóa tài khoản trước khi xóa!");
        }

        if (user.getRole() == User.Role.ADMIN) {
            long adminCount = userRepository.countByRole(User.Role.ADMIN);
            if (adminCount <= 1) {
                throw new IllegalStateException("Không thể xóa quản trị viên cuối cùng!");
            }
        }

        try {
            int affected = 0;

            if (user.getRole() == User.Role.TEACHER) {
                int r1 = entityManager.createNativeQuery("DELETE FROM enrollments WHERE course_id IN (SELECT id FROM courses WHERE teacher_id = ?)")
                        .setParameter(1, id).executeUpdate();
                log.info("Deleted {} enrollments for teacher courses", r1);

                int r2 = entityManager.createNativeQuery("DELETE FROM lessons WHERE course_id IN (SELECT id FROM courses WHERE teacher_id = ?)")
                        .setParameter(1, id).executeUpdate();
                log.info("Deleted {} lessons for teacher courses", r2);

                int r3 = entityManager.createNativeQuery("DELETE FROM courses WHERE teacher_id = ?")
                        .setParameter(1, id).executeUpdate();
                log.info("Deleted {} courses for teacher", r3);

            } else if (user.getRole() == User.Role.STUDENT) {
                int r1 = entityManager.createNativeQuery("DELETE FROM enrollments WHERE student_id = ?")
                        .setParameter(1, id).executeUpdate();
                log.info("Deleted {} enrollments for student", r1);
            }

            affected = entityManager.createNativeQuery("DELETE FROM users WHERE id = ?")
                    .setParameter(1, id).executeUpdate();
            log.info("DELETE users affected {} rows", affected);

            entityManager.flush();
            log.info("Flush completed");

            log.info("=== deleteUser SUCCESS id={} ===", id);
        } catch (Exception e) {
            log.error("=== deleteUser ERROR id={} ===", id, e);
            throw e;
        }
    }

    // ==================== TÌM KIẾM ====================

    @Transactional(readOnly = true)
    public List<User> searchUsers(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return userRepository.findAll();
        }
        return userRepository.searchByKeyword(keyword.trim());
    }

    @Transactional(readOnly = true)
    public List<User> searchUsersByRole(User.Role role, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return userRepository.findByRole(role);
        }
        return userRepository.searchByRoleAndKeyword(role, keyword.trim());
    }

    // ==================== THỐNG KÊ ====================

    @Transactional(readOnly = true)
    public UserDto.Statistics getStatistics() {
        return UserDto.Statistics.builder()
                .totalStudents(userRepository.countByRole(User.Role.STUDENT))
                .totalTeachers(userRepository.countByRole(User.Role.TEACHER))
                .totalAdmins(userRepository.countByRole(User.Role.ADMIN))
                .totalUsers(userRepository.count())
                .build();
    }

    // ==================== ĐỔI MẬT KHẨU (tự đổi) ====================

    public void changePassword(String username, String oldPassword, String newPassword) {
        User user = getUserByUsername(username);
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu cũ không đúng!");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("User {} đã đổi mật khẩu thành công", username);
    }
}
