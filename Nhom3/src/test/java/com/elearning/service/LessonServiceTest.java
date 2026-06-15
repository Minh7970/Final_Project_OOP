package com.elearning.service;

import com.elearning.dto.LessonDto;
import com.elearning.exception.AccessDeniedException;
import com.elearning.exception.ResourceNotFoundException;
import com.elearning.model.Course;
import com.elearning.model.Lesson;
import com.elearning.model.User;
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
class LessonServiceTest {

    @Mock private LessonRepository lessonRepository;
    @Mock private CourseService courseService;

    @InjectMocks
    private LessonService lessonService;

    private User teacher;
    private User otherTeacher;
    private Course course;
    private Lesson lesson;

    @BeforeEach
    void setUp() {
        teacher = User.builder().id(1L).username("teacher01").role(User.Role.TEACHER).build();
        otherTeacher = User.builder().id(2L).username("teacher02").role(User.Role.TEACHER).build();
        course = Course.builder().id(10L).title("Java Basics").teacher(teacher).build();
        lesson = Lesson.builder().id(100L).title("Lesson 1").content("Content")
                .orderIndex(1).status(Lesson.Status.PUBLISHED).course(course).build();
    }

    // ==================== createLesson ====================

    @Test
    void createLesson_success() {
        LessonDto.CreateRequest req = LessonDto.CreateRequest.builder()
                .title("New Lesson").content("New Content").durationMinutes(30).build();

        when(courseService.getCourseById(10L)).thenReturn(course);
        when(lessonRepository.findMaxOrderIndexByCourseId(10L)).thenReturn(1);
        when(lessonRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Lesson result = lessonService.createLesson(10L, req, "teacher01");
        assertThat(result.getTitle()).isEqualTo("New Lesson");
        assertThat(result.getOrderIndex()).isEqualTo(2);
        assertThat(result.getStatus()).isEqualTo(Lesson.Status.PUBLISHED);
    }

    @Test
    void createLesson_wrongTeacher_throws() {
        LessonDto.CreateRequest req = LessonDto.CreateRequest.builder()
                .title("New Lesson").build();

        when(courseService.getCourseById(10L)).thenReturn(course);

        assertThatThrownBy(() -> lessonService.createLesson(10L, req, "teacher02"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("không có quyền");
    }

    @Test
    void createLesson_withCustomOrderIndex() {
        LessonDto.CreateRequest req = LessonDto.CreateRequest.builder()
                .title("New Lesson").orderIndex(5).build();

        when(courseService.getCourseById(10L)).thenReturn(course);
        when(lessonRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Lesson result = lessonService.createLesson(10L, req, "teacher01");
        assertThat(result.getOrderIndex()).isEqualTo(5);
    }

    // ==================== getLessonsByCourse ====================

    @Test
    void getLessonsByCourse_returnsOrderedList() {
        when(lessonRepository.findByCourseIdOrderByOrderIndexAsc(10L))
                .thenReturn(List.of(lesson));
        assertThat(lessonService.getLessonsByCourse(10L)).hasSize(1);
    }

    // ==================== getLessonById ====================

    @Test
    void getLessonById_success() {
        when(lessonRepository.findById(100L)).thenReturn(Optional.of(lesson));
        assertThat(lessonService.getLessonById(100L)).isEqualTo(lesson);
    }

    @Test
    void getLessonById_notFound_throws() {
        when(lessonRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> lessonService.getLessonById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ==================== updateLesson ====================

    @Test
    void updateLesson_success() {
        LessonDto.UpdateRequest req = LessonDto.UpdateRequest.builder()
                .title("Updated Title").content("Updated Content")
                .durationMinutes(45).status(Lesson.Status.DRAFT).build();

        when(courseService.getCourseById(10L)).thenReturn(course);
        when(lessonRepository.findByIdAndCourseId(100L, 10L)).thenReturn(Optional.of(lesson));
        when(lessonRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Lesson result = lessonService.updateLesson(10L, 100L, req, "teacher01");
        assertThat(result.getTitle()).isEqualTo("Updated Title");
        assertThat(result.getDurationMinutes()).isEqualTo(45);
        assertThat(result.getStatus()).isEqualTo(Lesson.Status.DRAFT);
    }

    @Test
    void updateLesson_wrongTeacher_throws() {
        LessonDto.UpdateRequest req = LessonDto.UpdateRequest.builder()
                .title("Updated").build();

        when(courseService.getCourseById(10L)).thenReturn(course);

        assertThatThrownBy(() -> lessonService.updateLesson(10L, 100L, req, "teacher02"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("không có quyền");
    }

    @Test
    void updateLesson_notFound_throws() {
        LessonDto.UpdateRequest req = LessonDto.UpdateRequest.builder()
                .title("Updated").build();

        when(courseService.getCourseById(10L)).thenReturn(course);
        when(lessonRepository.findByIdAndCourseId(99L, 10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lessonService.updateLesson(10L, 99L, req, "teacher01"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ==================== deleteLesson ====================

    @Test
    void deleteLesson_success() {
        when(courseService.getCourseById(10L)).thenReturn(course);
        when(lessonRepository.findByIdAndCourseId(100L, 10L)).thenReturn(Optional.of(lesson));
        doNothing().when(lessonRepository).deleteLessonById(100L);

        lessonService.deleteLesson(10L, 100L, "teacher01");
        verify(lessonRepository).deleteLessonById(100L);
    }

    @Test
    void deleteLesson_wrongTeacher_throws() {
        when(courseService.getCourseById(10L)).thenReturn(course);

        assertThatThrownBy(() -> lessonService.deleteLesson(10L, 100L, "teacher02"))
                .isInstanceOf(AccessDeniedException.class);
    }
}
