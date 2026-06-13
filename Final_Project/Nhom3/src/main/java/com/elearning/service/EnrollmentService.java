package com.elearning.service;

import com.elearning.exception.ResourceNotFoundException;
import com.elearning.model.Course;
import com.elearning.model.Enrollment;
import com.elearning.model.User;
import com.elearning.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseService courseService;
    private final UserService userService;

    // ==================== ĐĂNG KÝ KHÓA HỌC ====================

    public Enrollment enrollCourse(Long courseId, String studentUsername) {
        User student = userService.getUserByUsername(studentUsername);
        Course course = courseService.getCourseById(courseId);

        // Kiểm tra đã đăng ký chưa
        boolean alreadyEnrolled = enrollmentRepository
                .existsByStudentIdAndCourseIdAndStatusNot(student.getId(), courseId, Enrollment.Status.CANCELLED);
        if (alreadyEnrolled) {
            throw new IllegalArgumentException("Bạn đã đăng ký khóa học này rồi!");
        }

        // Kiểm tra số lượng tối đa
        long currentCount = enrollmentRepository.countByCourseIdAndStatus(courseId, Enrollment.Status.ACTIVE);
        if (currentCount >= course.getMaxStudents()) {
            throw new IllegalArgumentException("Khóa học đã đạt số lượng học sinh tối đa!");
        }

        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .course(course)
                .status(Enrollment.Status.ACTIVE)
                .progressPercent(0)
                .build();

        Enrollment saved = enrollmentRepository.save(enrollment);
        log.info("Học sinh {} đã đăng ký khóa học: {}", studentUsername, course.getTitle());
        return saved;
    }

    // ==================== HỦY ĐĂNG KÝ ====================

    public void cancelEnrollment(Long courseId, String studentUsername) {
        User student = userService.getUserByUsername(studentUsername);
        Enrollment enrollment = enrollmentRepository
                .findByStudentIdAndCourseId(student.getId(), courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", "courseId", courseId));

        enrollment.setStatus(Enrollment.Status.CANCELLED);
        enrollmentRepository.save(enrollment);
        log.info("Học sinh {} đã hủy đăng ký khóa học ID={}", studentUsername, courseId);
    }

    // ==================== XEM DANH SÁCH ====================

    @Transactional(readOnly = true)
    public List<Enrollment> getStudentEnrollments(String studentUsername) {
        User student = userService.getUserByUsername(studentUsername);
        return enrollmentRepository.findActiveEnrollmentsByStudentId(student.getId());
    }

    @Transactional(readOnly = true)
    public List<Enrollment> getEnrollmentsByCourse(Long courseId) {
        return enrollmentRepository.findActiveEnrollmentsByCourseId(courseId);
    }

    @Transactional(readOnly = true)
    public boolean isEnrolled(Long studentId, Long courseId) {
        return enrollmentRepository
                .existsByStudentIdAndCourseIdAndStatusNot(studentId, courseId, Enrollment.Status.CANCELLED);
    }

    // ==================== CẬP NHẬT TIẾN ĐỘ ====================

    public Enrollment updateProgress(Long courseId, String studentUsername, int progressPercent) {
        User student = userService.getUserByUsername(studentUsername);
        Enrollment enrollment = enrollmentRepository
                .findByStudentIdAndCourseId(student.getId(), courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", "courseId", courseId));

        enrollment.setProgressPercent(Math.min(100, Math.max(0, progressPercent)));
        if (enrollment.getProgressPercent() == 100) {
            enrollment.setStatus(Enrollment.Status.COMPLETED);
            enrollment.setCompletedAt(java.time.LocalDateTime.now());
        }
        return enrollmentRepository.save(enrollment);
    }
}
