package com.elearning.controller;

import com.elearning.model.Course;
import com.elearning.model.Enrollment;
import com.elearning.model.Lesson;
import com.elearning.model.User;
import com.elearning.service.CourseService;
import com.elearning.service.EnrollmentService;
import com.elearning.service.LessonService;
import com.elearning.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/student")
@PreAuthorize("hasRole('STUDENT')")
@RequiredArgsConstructor
public class StudentController {

    private final CourseService courseService;
    private final EnrollmentService enrollmentService;
    private final LessonService lessonService;
    private final UserService userService;

    // ==================== DASHBOARD ====================

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails ud, Model model) {
        List<Enrollment> enrollments = enrollmentService.getStudentEnrollments(ud.getUsername());
        model.addAttribute("enrollments", enrollments);
        model.addAttribute("totalEnrolled", enrollments.size());
        long completed = enrollments.stream()
                .filter(e -> e.getStatus() == Enrollment.Status.COMPLETED).count();
        model.addAttribute("totalCompleted", completed);
        return "student/dashboard";
    }

    // ==================== TÌM KIẾM & XEM KHÓA HỌC ====================

    @GetMapping("/courses")
    public String exploreCourses(@AuthenticationPrincipal UserDetails ud,
                                 @RequestParam(required = false) String keyword,
                                 Model model) {
        User student = userService.getUserByUsername(ud.getUsername());
        List<Course> courses = courseService.getAvailableCoursesForStudent(student.getId(), keyword);
        model.addAttribute("courses", courses);
        model.addAttribute("keyword", keyword);
        return "student/courses/explore";
    }

    @GetMapping("/courses/{id}")
    public String viewCourse(@PathVariable Long id,
                             @AuthenticationPrincipal UserDetails ud,
                             Model model) {
        Course course = courseService.getCourseById(id);
        User student = userService.getUserByUsername(ud.getUsername());
        boolean isEnrolled = enrollmentService.isEnrolled(student.getId(), id);
        List<Lesson> lessons = isEnrolled
                ? lessonService.getLessonsByCourse(id)
                : List.of(); // Chưa đăng ký thì không xem nội dung
        model.addAttribute("course", course);
        model.addAttribute("lessons", lessons);
        model.addAttribute("isEnrolled", isEnrolled);
        return "student/courses/detail";
    }

    // ==================== ĐĂNG KÝ KHÓA HỌC ====================

    @PostMapping("/courses/{id}/enroll")
    public String enrollCourse(@PathVariable Long id,
                               @AuthenticationPrincipal UserDetails ud,
                               RedirectAttributes redirectAttr) {
        try {
            enrollmentService.enrollCourse(id, ud.getUsername());
            redirectAttr.addFlashAttribute("successMsg", "Đăng ký khóa học thành công!");
            return "redirect:/student/courses/" + id;
        } catch (IllegalArgumentException e) {
            redirectAttr.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/student/courses/" + id;
        }
    }

    @PostMapping("/courses/{id}/cancel")
    public String cancelEnrollment(@PathVariable Long id,
                                   @AuthenticationPrincipal UserDetails ud,
                                   RedirectAttributes redirectAttr) {
        try {
            enrollmentService.cancelEnrollment(id, ud.getUsername());
            redirectAttr.addFlashAttribute("successMsg", "Đã hủy đăng ký khóa học.");
        } catch (Exception e) {
            redirectAttr.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/student/my-courses";
    }

    // ==================== KHÓA HỌC CỦA TÔI ====================

    @GetMapping("/my-courses")
    public String myCourses(@AuthenticationPrincipal UserDetails ud, Model model) {
        List<Enrollment> enrollments = enrollmentService.getStudentEnrollments(ud.getUsername());
        model.addAttribute("enrollments", enrollments);
        return "student/courses/my-courses";
    }

    // ==================== XEM BÀI HỌC ====================

    @GetMapping("/courses/{courseId}/lessons/{lessonId}")
    public String viewLesson(@PathVariable Long courseId,
                             @PathVariable Long lessonId,
                             @AuthenticationPrincipal UserDetails ud,
                             Model model) {
        User student = userService.getUserByUsername(ud.getUsername());
        // Kiểm tra quyền xem (phải đăng ký)
        if (!enrollmentService.isEnrolled(student.getId(), courseId)) {
            return "redirect:/student/courses/" + courseId;
        }
        Course course = courseService.getCourseById(courseId);
        Lesson lesson = lessonService.getLessonById(lessonId);
        List<Lesson> allLessons = lessonService.getLessonsByCourse(courseId);
        model.addAttribute("course", course);
        model.addAttribute("lesson", lesson);
        model.addAttribute("allLessons", allLessons);
        return "student/lessons/view";
    }
}
