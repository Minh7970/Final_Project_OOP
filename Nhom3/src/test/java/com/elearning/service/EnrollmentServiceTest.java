package com.elearning.service;

import com.elearning.exception.AccessDeniedException;
import com.elearning.exception.ResourceNotFoundException;
import com.elearning.model.Course;
import com.elearning.model.Enrollment;
import com.elearning.model.User;
import com.elearning.repository.EnrollmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {

    @Mock private EnrollmentRepository enrollmentRepository;
    @Mock private CourseService courseService;
    @Mock private UserService userService;

    @InjectMocks
    private EnrollmentService enrollmentService;

    private User teacher;
    private User student;
    private Course course;
    private Enrollment enrollment;

    @BeforeEach
    void setUp() {
        teacher = User.builder().id(2L).username("teacher01").role(User.Role.TEACHER).build();
        student = User.builder().id(1L).username("student01").role(User.Role.STUDENT).build();
        course = Course.builder().id(10L).title("Java Basics").maxStudents(100)
                .status(Course.Status.ACTIVE).teacher(teacher).build();
        enrollment = Enrollment.builder().id(100L).student(student).course(course)
                .status(Enrollment.Status.ACTIVE).progressPercent(0).build();
    }

    // ==================== enrollCourse ====================

    @Test
    void enrollCourse_success() {
        when(userService.getUserByUsername("student01")).thenReturn(student);
        when(courseService.getCourseById(10L)).thenReturn(course);
        when(enrollmentRepository.existsByStudentIdAndCourseIdAndStatusNot(
                1L, 10L, Enrollment.Status.CANCELLED)).thenReturn(false);
        when(enrollmentRepository.countByCourseIdAndStatus(10L, Enrollment.Status.ACTIVE)).thenReturn(5L);
        when(enrollmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Enrollment result = enrollmentService.enrollCourse(10L, "student01");
        assertThat(result.getStudent()).isEqualTo(student);
        assertThat(result.getCourse()).isEqualTo(course);
        assertThat(result.getStatus()).isEqualTo(Enrollment.Status.ACTIVE);
        assertThat(result.getProgressPercent()).isZero();
    }

    @Test
    void enrollCourse_alreadyEnrolled_throws() {
        when(userService.getUserByUsername("student01")).thenReturn(student);
        when(courseService.getCourseById(10L)).thenReturn(course);
        when(enrollmentRepository.existsByStudentIdAndCourseIdAndStatusNot(
                1L, 10L, Enrollment.Status.CANCELLED)).thenReturn(true);

        assertThatThrownBy(() -> enrollmentService.enrollCourse(10L, "student01"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("đã đăng ký");
    }

    @Test
    void enrollCourse_full_throws() {
        when(userService.getUserByUsername("student01")).thenReturn(student);
        when(courseService.getCourseById(10L)).thenReturn(course);
        when(enrollmentRepository.existsByStudentIdAndCourseIdAndStatusNot(
                1L, 10L, Enrollment.Status.CANCELLED)).thenReturn(false);
        when(enrollmentRepository.countByCourseIdAndStatus(10L, Enrollment.Status.ACTIVE))
                .thenReturn(100L);

        assertThatThrownBy(() -> enrollmentService.enrollCourse(10L, "student01"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tối đa");
    }

    // ==================== cancelEnrollment ====================

    @Test
    void cancelEnrollment_success() {
        when(userService.getUserByUsername("student01")).thenReturn(student);
        when(enrollmentRepository.findByStudentIdAndCourseId(1L, 10L))
                .thenReturn(Optional.of(enrollment));
        when(enrollmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        enrollmentService.cancelEnrollment(10L, "student01");
        assertThat(enrollment.getStatus()).isEqualTo(Enrollment.Status.CANCELLED);
    }

    @Test
    void cancelEnrollment_notFound_throws() {
        when(userService.getUserByUsername("student01")).thenReturn(student);
        when(enrollmentRepository.findByStudentIdAndCourseId(1L, 99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> enrollmentService.cancelEnrollment(99L, "student01"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ==================== getStudentEnrollments ====================

    @Test
    void getStudentEnrollments_returnsList() {
        when(userService.getUserByUsername("student01")).thenReturn(student);
        when(enrollmentRepository.findActiveEnrollmentsByStudentId(1L))
                .thenReturn(List.of(enrollment));

        assertThat(enrollmentService.getStudentEnrollments("student01")).hasSize(1);
    }

    // ==================== getEnrollmentsByCourse ====================

    @Test
    void getEnrollmentsByCourse_returnsList() {
        when(enrollmentRepository.findActiveEnrollmentsByCourseId(10L))
                .thenReturn(List.of(enrollment));

        assertThat(enrollmentService.getEnrollmentsByCourse(10L)).hasSize(1);
    }

    // ==================== isEnrolled ====================

    @Test
    void isEnrolled_returnsTrueWhenExists() {
        when(enrollmentRepository.existsByStudentIdAndCourseIdAndStatusNot(
                1L, 10L, Enrollment.Status.CANCELLED)).thenReturn(true);
        assertThat(enrollmentService.isEnrolled(1L, 10L)).isTrue();
    }

    @Test
    void isEnrolled_returnsFalseWhenNotExists() {
        when(enrollmentRepository.existsByStudentIdAndCourseIdAndStatusNot(
                1L, 10L, Enrollment.Status.CANCELLED)).thenReturn(false);
        assertThat(enrollmentService.isEnrolled(1L, 10L)).isFalse();
    }

    // ==================== updateProgress ====================

    @Test
    void updateProgress_success() {
        when(userService.getUserByUsername("student01")).thenReturn(student);
        when(enrollmentRepository.findByStudentIdAndCourseId(1L, 10L))
                .thenReturn(Optional.of(enrollment));
        when(enrollmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Enrollment result = enrollmentService.updateProgress(10L, "student01", 50);
        assertThat(result.getProgressPercent()).isEqualTo(50);
        assertThat(result.getStatus()).isEqualTo(Enrollment.Status.ACTIVE);
    }

    @Test
    void updateProgress_100percent_completes() {
        when(userService.getUserByUsername("student01")).thenReturn(student);
        when(enrollmentRepository.findByStudentIdAndCourseId(1L, 10L))
                .thenReturn(Optional.of(enrollment));
        when(enrollmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Enrollment result = enrollmentService.updateProgress(10L, "student01", 100);
        assertThat(result.getProgressPercent()).isEqualTo(100);
        assertThat(result.getStatus()).isEqualTo(Enrollment.Status.COMPLETED);
        assertThat(result.getCompletedAt()).isNotNull();
    }

    @Test
    void updateProgress_clampsValue() {
        when(userService.getUserByUsername("student01")).thenReturn(student);
        when(enrollmentRepository.findByStudentIdAndCourseId(1L, 10L))
                .thenReturn(Optional.of(enrollment));
        when(enrollmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Enrollment result = enrollmentService.updateProgress(10L, "student01", 150);
        assertThat(result.getProgressPercent()).isEqualTo(100);
    }

    // ==================== addStudentByTeacher ====================

    @Test
    void addStudentByTeacher_success() {
        User newStudent = User.builder().id(3L).username("student02").role(User.Role.STUDENT).build();
        when(courseService.getCourseById(10L)).thenReturn(course);
        when(userService.getUserById(3L)).thenReturn(newStudent);
        when(enrollmentRepository.existsByStudentIdAndCourseIdAndStatusNot(
                3L, 10L, Enrollment.Status.CANCELLED)).thenReturn(false);
        when(enrollmentRepository.countByCourseIdAndStatus(10L, Enrollment.Status.ACTIVE)).thenReturn(5L);
        when(enrollmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Enrollment result = enrollmentService.addStudentByTeacher(10L, 3L, "teacher01");
        assertThat(result.getStudent().getUsername()).isEqualTo("student02");
        assertThat(result.getStatus()).isEqualTo(Enrollment.Status.ACTIVE);
    }

    @Test
    void addStudentByTeacher_wrongTeacher_throws() {
        when(courseService.getCourseById(10L)).thenReturn(course);
        assertThatThrownBy(() -> enrollmentService.addStudentByTeacher(10L, 3L, "other_teacher"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("không có quyền");
    }

    @Test
    void addStudentByTeacher_notStudent_throws() {
        User admin = User.builder().id(3L).username("admin").role(User.Role.ADMIN).build();
        when(courseService.getCourseById(10L)).thenReturn(course);
        when(userService.getUserById(3L)).thenReturn(admin);
        assertThatThrownBy(() -> enrollmentService.addStudentByTeacher(10L, 3L, "teacher01"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("không phải là học sinh");
    }

    @Test
    void addStudentByTeacher_alreadyEnrolled_throws() {
        when(courseService.getCourseById(10L)).thenReturn(course);
        when(userService.getUserById(1L)).thenReturn(student);
        when(enrollmentRepository.existsByStudentIdAndCourseIdAndStatusNot(
                1L, 10L, Enrollment.Status.CANCELLED)).thenReturn(true);
        assertThatThrownBy(() -> enrollmentService.addStudentByTeacher(10L, 1L, "teacher01"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("đã đăng ký");
    }

    @Test
    void addStudentByTeacher_courseFull_throws() {
        User newStudent = User.builder().id(3L).username("student02").role(User.Role.STUDENT).build();
        when(courseService.getCourseById(10L)).thenReturn(course);
        when(userService.getUserById(3L)).thenReturn(newStudent);
        when(enrollmentRepository.existsByStudentIdAndCourseIdAndStatusNot(
                3L, 10L, Enrollment.Status.CANCELLED)).thenReturn(false);
        when(enrollmentRepository.countByCourseIdAndStatus(10L, Enrollment.Status.ACTIVE)).thenReturn(100L);
        assertThatThrownBy(() -> enrollmentService.addStudentByTeacher(10L, 3L, "teacher01"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tối đa");
    }
}
