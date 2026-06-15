package com.elearning.service;

import com.elearning.dto.UserDto;
import com.elearning.exception.ResourceNotFoundException;
import com.elearning.model.User;
import com.elearning.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private EntityManager entityManager;

    private UserService userService;

    private User student;
    private User teacher;
    private User admin;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder);
        ReflectionTestUtils.setField(userService, "entityManager", entityManager);
        student = User.builder().id(1L).username("student01").email("s@e.com")
                .fullName("Student One").role(User.Role.STUDENT).enabled(true).build();
        teacher = User.builder().id(2L).username("teacher01").email("t@e.com")
                .fullName("Teacher One").role(User.Role.TEACHER).enabled(true).build();
        admin = User.builder().id(3L).username("admin").email("a@e.com")
                .fullName("Admin").role(User.Role.ADMIN).enabled(true).build();
    }

    // ==================== createUser ====================

    @Test
    void createUser_success() {
        UserDto.CreateRequest req = UserDto.CreateRequest.builder()
                .username("newuser").password("pass123").email("n@e.com")
                .fullName("New User").role(User.Role.STUDENT).build();

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("n@e.com")).thenReturn(false);
        when(passwordEncoder.encode("pass123")).thenReturn("encoded");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.createUser(req);

        assertThat(result.getUsername()).isEqualTo("newuser");
        assertThat(result.getPassword()).isEqualTo("encoded");
        assertThat(result.getRole()).isEqualTo(User.Role.STUDENT);
        assertThat(result.getEnabled()).isTrue();
        verify(userRepository).save(any());
    }

    @Test
    void createUser_duplicateUsername_throws() {
        UserDto.CreateRequest req = UserDto.CreateRequest.builder()
                .username("student01").password("pass123").email("n@e.com")
                .fullName("New User").role(User.Role.STUDENT).build();

        when(userRepository.existsByUsername("student01")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("đã tồn tại");
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_duplicateEmail_throws() {
        UserDto.CreateRequest req = UserDto.CreateRequest.builder()
                .username("newuser").password("pass123").email("s@e.com")
                .fullName("New User").role(User.Role.STUDENT).build();

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("s@e.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("đã được sử dụng");
    }

    @Test
    void createUser_adminRole_throws() {
        UserDto.CreateRequest req = UserDto.CreateRequest.builder()
                .username("newuser").password("pass123").email("n@e.com")
                .fullName("New User").role(User.Role.ADMIN).build();

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("n@e.com")).thenReturn(false);

        assertThatThrownBy(() -> userService.createUser(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Admin");
    }

    // ==================== getUserById ====================

    @Test
    void getUserById_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(student));
        assertThat(userService.getUserById(1L)).isEqualTo(student);
    }

    @Test
    void getUserById_notFound_throws() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ==================== getUserByUsername ====================

    @Test
    void getUserByUsername_success() {
        when(userRepository.findByUsername("student01")).thenReturn(Optional.of(student));
        assertThat(userService.getUserByUsername("student01")).isEqualTo(student);
    }

    @Test
    void getUserByUsername_notFound_throws() {
        when(userRepository.findByUsername("nobody")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.getUserByUsername("nobody"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ==================== getAllUsers ====================

    @Test
    void getAllUsers_returnsList() {
        when(userRepository.findAll()).thenReturn(List.of(student, teacher, admin));
        assertThat(userService.getAllUsers()).hasSize(3);
    }

    // ==================== getUsersByRole ====================

    @Test
    void getUsersByRole_returnsFiltered() {
        when(userRepository.findByRole(User.Role.STUDENT)).thenReturn(List.of(student));
        assertThat(userService.getUsersByRole(User.Role.STUDENT)).hasSize(1);
    }

    // ==================== updateUser ====================

    @Test
    void updateUser_success() {
        UserDto.UpdateRequest req = UserDto.UpdateRequest.builder()
                .fullName("Updated Name").email("new@e.com")
                .phone("0912345678").address("HN").build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(student));
        when(userRepository.existsByEmail("new@e.com")).thenReturn(false);
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.updateUser(1L, req);
        assertThat(result.getFullName()).isEqualTo("Updated Name");
        assertThat(result.getEmail()).isEqualTo("new@e.com");
    }

    @Test
    void updateUser_duplicateEmail_throws() {
        UserDto.UpdateRequest req = UserDto.UpdateRequest.builder()
                .fullName("Updated Name").email("t@e.com")
                .phone("0912345678").address("HN").build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(student));
        when(userRepository.existsByEmail("t@e.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.updateUser(1L, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("đã được sử dụng");
    }

    @Test
    void updateUser_withPassword_encodes() {
        UserDto.UpdateRequest req = UserDto.UpdateRequest.builder()
                .fullName("Updated Name").email("s@e.com")
                .newPassword("newPass123").build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(student));
        when(passwordEncoder.encode("newPass123")).thenReturn("newEncoded");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.updateUser(1L, req);
        assertThat(result.getPassword()).isEqualTo("newEncoded");
    }

    // ==================== updateUserRoleAndStatus ====================

    @Test
    void updateUserRoleAndStatus_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(student));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.updateUserRoleAndStatus(1L, User.Role.TEACHER, false);
        assertThat(result.getRole()).isEqualTo(User.Role.TEACHER);
        assertThat(result.getEnabled()).isFalse();
    }

    @Test
    void updateUserRoleAndStatus_setAdmin_throws() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(student));
        assertThatThrownBy(() -> userService.updateUserRoleAndStatus(1L, User.Role.ADMIN, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Admin");
    }

    // ==================== deleteUser ====================

    @Test
    void deleteUser_enabledUser_throws() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(student));
        assertThatThrownBy(() -> userService.deleteUser(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("vô hiệu hóa");
    }

    @Test
    void deleteUser_lastAdmin_throws() {
        admin.setEnabled(false);
        when(userRepository.findById(3L)).thenReturn(Optional.of(admin));
        when(userRepository.countByRole(User.Role.ADMIN)).thenReturn(1L);

        assertThatThrownBy(() -> userService.deleteUser(3L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cuối cùng");
    }

    @Test
    void deleteUser_student_success() {
        student.setEnabled(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(student));
        jakarta.persistence.Query mockQuery = mock();
        when(entityManager.createNativeQuery(anyString())).thenReturn(mockQuery);
        when(mockQuery.setParameter(anyInt(), any())).thenReturn(mockQuery);
        when(mockQuery.executeUpdate()).thenReturn(1);

        userService.deleteUser(1L);
        verify(entityManager).flush();
    }

    @Test
    void deleteUser_teacher_success() {
        teacher.setEnabled(false);
        when(userRepository.findById(2L)).thenReturn(Optional.of(teacher));
        jakarta.persistence.Query mockQuery = mock();
        when(entityManager.createNativeQuery(anyString())).thenReturn(mockQuery);
        when(mockQuery.setParameter(anyInt(), any())).thenReturn(mockQuery);
        when(mockQuery.executeUpdate()).thenReturn(1);

        userService.deleteUser(2L);
        verify(entityManager).flush();
    }

    // ==================== searchUsers ====================

    @Test
    void searchUsers_withKeyword() {
        when(userRepository.searchByKeyword("stu")).thenReturn(List.of(student));
        assertThat(userService.searchUsers("stu")).hasSize(1);
    }

    @Test
    void searchUsers_blankKeyword_returnsAll() {
        when(userRepository.findAll()).thenReturn(List.of(student, teacher));
        assertThat(userService.searchUsers("")).hasSize(2);
    }

    @Test
    void searchUsersByRole_withKeyword() {
        when(userRepository.searchByRoleAndKeyword(User.Role.STUDENT, "stu"))
                .thenReturn(List.of(student));
        assertThat(userService.searchUsersByRole(User.Role.STUDENT, "stu")).hasSize(1);
    }

    @Test
    void searchUsersByRole_blankKeyword_returnsByRole() {
        when(userRepository.findByRole(User.Role.TEACHER)).thenReturn(List.of(teacher));
        assertThat(userService.searchUsersByRole(User.Role.TEACHER, "")).hasSize(1);
    }

    // ==================== getStatistics ====================

    @Test
    void getStatistics_returnsCounts() {
        when(userRepository.countByRole(User.Role.STUDENT)).thenReturn(10L);
        when(userRepository.countByRole(User.Role.TEACHER)).thenReturn(5L);
        when(userRepository.countByRole(User.Role.ADMIN)).thenReturn(2L);
        when(userRepository.count()).thenReturn(17L);

        UserDto.Statistics stats = userService.getStatistics();
        assertThat(stats.getTotalStudents()).isEqualTo(10);
        assertThat(stats.getTotalTeachers()).isEqualTo(5);
        assertThat(stats.getTotalAdmins()).isEqualTo(2);
        assertThat(stats.getTotalUsers()).isEqualTo(17);
    }

    // ==================== changePassword ====================

    @Test
    void changePassword_success() {
        when(userRepository.findByUsername("student01")).thenReturn(Optional.of(student));
        when(passwordEncoder.matches("oldPass", student.getPassword())).thenReturn(true);
        when(passwordEncoder.encode("newPass")).thenReturn("newEncoded");

        userService.changePassword("student01", "oldPass", "newPass");
        verify(userRepository).save(argThat(u -> u.getPassword().equals("newEncoded")));
    }

    @Test
    void changePassword_wrongOldPassword_throws() {
        when(userRepository.findByUsername("student01")).thenReturn(Optional.of(student));
        when(passwordEncoder.matches("wrong", student.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword("student01", "wrong", "newPass"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("không đúng");
    }
}
