package com.elearning.service;

import com.elearning.dto.CourseDto;
import com.elearning.exception.AccessDeniedException;
import com.elearning.exception.ResourceNotFoundException;
import com.elearning.model.Course;
import com.elearning.model.User;
import com.elearning.repository.CourseRepository;
import com.elearning.repository.EnrollmentRepository;
import com.elearning.repository.LessonRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock private CourseRepository courseRepository;
    @Mock private EnrollmentRepository enrollmentRepository;
    @Mock private LessonRepository lessonRepository;
    @Mock private UserService userService;

    @InjectMocks
    private CourseService courseService;

    private User teacher;
    private User otherTeacher;
    private Course course;

    @BeforeEach
    void setUp() {
        teacher = User.builder().id(1L).username("teacher01").role(User.Role.TEACHER).build();
        otherTeacher = User.builder().id(2L).username("teacher02").role(User.Role.TEACHER).build();
        course = Course.builder().id(10L).title("Java Basics").description("Learn Java")
                .status(Course.Status.ACTIVE).teacher(teacher).maxStudents(100).build();
    }

    // ==================== createCourse ====================

    @Test
    void createCourse_success() {
        CourseDto.CreateRequest req = CourseDto.CreateRequest.builder()
                .title("New Course").description("Desc").maxStudents(50).build();

        when(userService.getUserByUsername("teacher01")).thenReturn(teacher);
        when(courseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Course result = courseService.createCourse(req, "teacher01");
        assertThat(result.getTitle()).isEqualTo("New Course");
        assertThat(result.getTeacher()).isEqualTo(teacher);
        assertThat(result.getStatus()).isEqualTo(Course.Status.ACTIVE);
    }

    // ==================== getActiveCourses ====================

    @Test
    void getActiveCourses_returnsOnlyActive() {
        when(courseRepository.findAllActiveCoursesWithTeacher()).thenReturn(List.of(course));
        assertThat(courseService.getActiveCourses()).hasSize(1);
    }

    // ==================== getCoursesByTeacher ====================

    @Test
    void getCoursesByTeacher_returnsList() {
        when(userService.getUserByUsername("teacher01")).thenReturn(teacher);
        when(courseRepository.findByTeacher(teacher)).thenReturn(List.of(course));

        assertThat(courseService.getCoursesByTeacher("teacher01")).hasSize(1);
    }

    // ==================== getCourseById ====================

    @Test
    void getCourseById_success() {
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));
        assertThat(courseService.getCourseById(10L)).isEqualTo(course);
    }

    @Test
    void getCourseById_notFound_throws() {
        when(courseRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> courseService.getCourseById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ==================== updateCourse ====================

    @Test
    void updateCourse_success() {
        CourseDto.UpdateRequest req = CourseDto.UpdateRequest.builder()
                .title("Updated Title").description("Updated Desc")
                .maxStudents(200).status(Course.Status.INACTIVE).build();

        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));
        when(courseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Course result = courseService.updateCourse(10L, req, "teacher01");
        assertThat(result.getTitle()).isEqualTo("Updated Title");
        assertThat(result.getMaxStudents()).isEqualTo(200);
        assertThat(result.getStatus()).isEqualTo(Course.Status.INACTIVE);
    }

    @Test
    void updateCourse_wrongTeacher_throws() {
        CourseDto.UpdateRequest req = CourseDto.UpdateRequest.builder()
                .title("Updated").build();

        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));

        assertThatThrownBy(() -> courseService.updateCourse(10L, req, "teacher02"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("không có quyền");
    }

    // ==================== updateCourseStatus ====================

    @Test
    void updateCourseStatus_success() {
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));
        when(courseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Course result = courseService.updateCourseStatus(10L, Course.Status.INACTIVE);
        assertThat(result.getStatus()).isEqualTo(Course.Status.INACTIVE);
    }

    // ==================== deleteCourse ====================

    @Test
    void deleteCourse_asAdmin_success() {
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));
        doNothing().when(enrollmentRepository).deleteByCourseId(10L);
        doNothing().when(lessonRepository).deleteByCourseId(10L);
        doNothing().when(courseRepository).deleteCourseById(10L);

        courseService.deleteCourse(10L, "admin", true);
        verify(enrollmentRepository).deleteByCourseId(10L);
        verify(lessonRepository).deleteByCourseId(10L);
        verify(courseRepository).deleteCourseById(10L);
    }

    @Test
    void deleteCourse_asTeacherOwner_success() {
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));
        doNothing().when(enrollmentRepository).deleteByCourseId(10L);
        doNothing().when(lessonRepository).deleteByCourseId(10L);
        doNothing().when(courseRepository).deleteCourseById(10L);

        courseService.deleteCourse(10L, "teacher01", false);
        verify(courseRepository).deleteCourseById(10L);
    }

    @Test
    void deleteCourse_asTeacherNonOwner_throws() {
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));

        assertThatThrownBy(() -> courseService.deleteCourse(10L, "teacher02", false))
                .isInstanceOf(AccessDeniedException.class);
    }

    // ==================== searchCourses ====================

    @Test
    void searchCourses_admin_all() {
        when(courseRepository.findAll()).thenReturn(List.of(course));
        assertThat(courseService.searchCourses(null, true)).hasSize(1);
    }

    @Test
    void searchCourses_admin_withKeyword() {
        when(courseRepository.searchAllByKeyword("Java")).thenReturn(List.of(course));
        assertThat(courseService.searchCourses("Java", true)).hasSize(1);
    }

    @Test
    void searchCourses_student_all() {
        when(courseRepository.findAllActiveCoursesWithTeacher()).thenReturn(List.of(course));
        assertThat(courseService.searchCourses(null, false)).hasSize(1);
    }

    // ==================== searchTeacherCourses ====================

    @Test
    void searchTeacherCourses_withKeyword() {
        when(userService.getUserByUsername("teacher01")).thenReturn(teacher);
        when(courseRepository.searchByTeacherAndKeyword(teacher, "Java")).thenReturn(List.of(course));

        assertThat(courseService.searchTeacherCourses("teacher01", "Java")).hasSize(1);
    }

    @Test
    void searchTeacherCourses_blankKeyword() {
        when(userService.getUserByUsername("teacher01")).thenReturn(teacher);
        when(courseRepository.findByTeacher(teacher)).thenReturn(List.of(course));

        assertThat(courseService.searchTeacherCourses("teacher01", "")).hasSize(1);
    }

    // ==================== getAvailableCoursesForStudent ====================

    @Test
    void getAvailableCoursesForStudent_withKeyword() {
        when(courseRepository.searchByTitleOrTeacherName("Java")).thenReturn(List.of(course));
        assertThat(courseService.getAvailableCoursesForStudent(1L, "Java")).hasSize(1);
    }

    @Test
    void getAvailableCoursesForStudent_blankKeyword() {
        when(courseRepository.findAvailableCoursesForStudent(1L)).thenReturn(List.of(course));
        assertThat(courseService.getAvailableCoursesForStudent(1L, "")).hasSize(1);
    }
}
