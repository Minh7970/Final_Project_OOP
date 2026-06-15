package com.elearning.service;

import com.elearning.dto.CourseDto;
import com.elearning.exception.AccessDeniedException;
import com.elearning.exception.ResourceNotFoundException;
import com.elearning.model.Course;
import com.elearning.model.User;
import com.elearning.repository.CourseRepository;
import com.elearning.repository.EnrollmentRepository;
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
public class CourseService {

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LessonRepository lessonRepository;
    private final UserService userService;

    // ==================== TẠO KHÓA HỌC (Teacher) ====================

    public Course createCourse(CourseDto.CreateRequest request, String teacherUsername) {
        User teacher = userService.getUserByUsername(teacherUsername);

        Course course = Course.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .thumbnailUrl(request.getThumbnailUrl())
                .maxStudents(request.getMaxStudents() != null ? request.getMaxStudents() : 100)
                .status(Course.Status.ACTIVE)
                .teacher(teacher)
                .build();

        Course saved = courseRepository.save(course);
        log.info("Teacher {} đã tạo khóa học: {}", teacherUsername, saved.getTitle());
        return saved;
    }

    // ==================== XEM KHÓA HỌC ====================

    @Transactional(readOnly = true)
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Course> getActiveCourses() {
        return courseRepository.findAllActiveCoursesWithTeacher();
    }

    @Transactional(readOnly = true)
    public List<Course> getCoursesByTeacher(String teacherUsername) {
        User teacher = userService.getUserByUsername(teacherUsername);
        return courseRepository.findByTeacher(teacher);
    }

    @Transactional(readOnly = true)
    public Course getCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));
    }

    // ==================== CẬP NHẬT KHÓA HỌC (Teacher) ====================

    public Course updateCourse(Long id, CourseDto.UpdateRequest request, String username) {
        Course course = getCourseById(id);
        verifyTeacherOwnership(course, username);

        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        course.setThumbnailUrl(request.getThumbnailUrl());
        if (request.getMaxStudents() != null) course.setMaxStudents(request.getMaxStudents());
        if (request.getStatus() != null) course.setStatus(request.getStatus());

        log.info("Teacher {} cập nhật khóa học: {}", username, course.getTitle());
        return courseRepository.save(course);
    }

    // Admin cập nhật trạng thái khóa học
    public Course updateCourseStatus(Long id, Course.Status status) {
        Course course = getCourseById(id);
        course.setStatus(status);
        return courseRepository.save(course);
    }

    // ==================== XÓA KHÓA HỌC ====================

    public void deleteCourse(Long id, String username, boolean isAdmin) {
        Course course = getCourseById(id);
        if (!isAdmin) {
            verifyTeacherOwnership(course, username);
        }
        enrollmentRepository.deleteByCourseId(id);
        lessonRepository.deleteByCourseId(id);
        courseRepository.deleteCourseById(id);
        log.info("Đã xóa khóa học ID={}: {}", id, course.getTitle());
    }

    // ==================== TÌM KIẾM ====================

    @Transactional(readOnly = true)
    public List<Course> searchCourses(String keyword, boolean adminView) {
        if (keyword == null || keyword.isBlank()) {
            return adminView ? courseRepository.findAll() : courseRepository.findAllActiveCoursesWithTeacher();
        }
        return adminView
                ? courseRepository.searchAllByKeyword(keyword.trim())
                : courseRepository.searchByTitleOrTeacherName(keyword.trim());
    }

    @Transactional(readOnly = true)
    public List<Course> searchTeacherCourses(String teacherUsername, String keyword) {
        User teacher = userService.getUserByUsername(teacherUsername);
        if (keyword == null || keyword.isBlank()) {
            return courseRepository.findByTeacher(teacher);
        }
        return courseRepository.searchByTeacherAndKeyword(teacher, keyword.trim());
    }

    @Transactional(readOnly = true)
    public List<Course> getAvailableCoursesForStudent(Long studentId, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return courseRepository.findAvailableCoursesForStudent(studentId);
        }
        return courseRepository.searchByTitleOrTeacherName(keyword.trim());
    }

    // ==================== HELPER ====================

    private void verifyTeacherOwnership(Course course, String username) {
        if (!course.getTeacher().getUsername().equals(username)) {
            throw new AccessDeniedException("Bạn không có quyền chỉnh sửa khóa học này!");
        }
    }
}
