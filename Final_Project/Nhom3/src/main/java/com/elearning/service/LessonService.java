package com.elearning.service;

import com.elearning.dto.LessonDto;
import com.elearning.exception.AccessDeniedException;
import com.elearning.exception.ResourceNotFoundException;
import com.elearning.model.Course;
import com.elearning.model.Lesson;
import com.elearning.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LessonService {

    private final LessonRepository lessonRepository;
    private final CourseService courseService;

    public Lesson createLesson(Long courseId, LessonDto.CreateRequest request, String teacherUsername) {
        Course course = courseService.getCourseById(courseId);
        verifyTeacherOwnership(course, teacherUsername);

        // Tự động đặt thứ tự bài học
        int nextOrder = lessonRepository.findMaxOrderIndexByCourseId(courseId) + 1;

        Lesson lesson = Lesson.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .videoUrl(request.getVideoUrl())
                .orderIndex(request.getOrderIndex() != null ? request.getOrderIndex() : nextOrder)
                .durationMinutes(request.getDurationMinutes())
                .status(Lesson.Status.PUBLISHED)
                .course(course)
                .build();

        Lesson saved = lessonRepository.save(lesson);
        log.info("Teacher {} đã thêm bài học '{}' vào khóa học '{}'",
                teacherUsername, saved.getTitle(), course.getTitle());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Lesson> getLessonsByCourse(Long courseId) {
        return lessonRepository.findByCourseIdOrderByOrderIndexAsc(courseId);
    }

    @Transactional(readOnly = true)
    public Lesson getLessonById(Long lessonId) {
        return lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", "id", lessonId));
    }

    public Lesson updateLesson(Long courseId, Long lessonId, LessonDto.UpdateRequest request, String teacherUsername) {
        Course course = courseService.getCourseById(courseId);
        verifyTeacherOwnership(course, teacherUsername);

        Lesson lesson = lessonRepository.findByIdAndCourseId(lessonId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", "id", lessonId));

        lesson.setTitle(request.getTitle());
        lesson.setContent(request.getContent());
        lesson.setVideoUrl(request.getVideoUrl());
        if (request.getOrderIndex() != null) lesson.setOrderIndex(request.getOrderIndex());
        if (request.getDurationMinutes() != null) lesson.setDurationMinutes(request.getDurationMinutes());
        if (request.getStatus() != null) lesson.setStatus(request.getStatus());

        log.info("Teacher {} cập nhật bài học: {}", teacherUsername, lesson.getTitle());
        return lessonRepository.save(lesson);
    }

    public void deleteLesson(Long courseId, Long lessonId, String teacherUsername) {
        Course course = courseService.getCourseById(courseId);
        verifyTeacherOwnership(course, teacherUsername);

        Lesson lesson = lessonRepository.findByIdAndCourseId(lessonId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", "id", lessonId));

        lessonRepository.delete(lesson);
        log.info("Teacher {} đã xóa bài học: {}", teacherUsername, lesson.getTitle());
    }

    private void verifyTeacherOwnership(Course course, String username) {
        if (!course.getTeacher().getUsername().equals(username)) {
            throw new AccessDeniedException("Bạn không có quyền quản lý khóa học này!");
        }
    }
}
