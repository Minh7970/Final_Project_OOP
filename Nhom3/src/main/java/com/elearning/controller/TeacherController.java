package com.elearning.controller;

import com.elearning.dto.CourseDto;
import com.elearning.dto.LessonDto;
import com.elearning.dto.UserDto;
import com.elearning.model.Course;
import com.elearning.model.Enrollment;
import com.elearning.model.Lesson;
import com.elearning.model.User;
import com.elearning.service.CourseService;
import com.elearning.service.EnrollmentService;
import com.elearning.service.LessonService;
import com.elearning.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/teacher")
@PreAuthorize("hasRole('TEACHER')")
@RequiredArgsConstructor
public class TeacherController {

    private final CourseService courseService;
    private final LessonService lessonService;
    private final EnrollmentService enrollmentService;
    private final UserService userService;

    // ==================== DASHBOARD ====================

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String username = userDetails.getUsername();
        List<Course> courses = courseService.getCoursesByTeacher(username);
        model.addAttribute("courses", courses);
        model.addAttribute("totalCourses", courses.size());
        long totalStudents = courses.stream()
                .mapToLong(c -> enrollmentService.getEnrollmentsByCourse(c.getId()).size())
                .sum();
        model.addAttribute("totalStudents", totalStudents);
        return "teacher/dashboard";
    }

    // ==================== KHÓA HỌC ====================

    @GetMapping("/courses")
    public String listCourses(@AuthenticationPrincipal UserDetails ud,
                              @RequestParam(required = false) String keyword,
                              Model model) {
        List<Course> courses = courseService.searchTeacherCourses(ud.getUsername(), keyword);
        model.addAttribute("courses", courses);
        model.addAttribute("keyword", keyword);
        return "teacher/courses/list";
    }

    @GetMapping("/courses/create")
    public String createCourseForm(Model model) {
        model.addAttribute("courseForm", new CourseDto.CreateRequest());
        return "teacher/courses/create";
    }

    @PostMapping("/courses/create")
    public String createCourse(@Valid @ModelAttribute("courseForm") CourseDto.CreateRequest request,
                               BindingResult result,
                               @AuthenticationPrincipal UserDetails ud,
                               RedirectAttributes redirectAttr) {
        if (result.hasErrors()) return "teacher/courses/create";
        try {
            Course saved = courseService.createCourse(request, ud.getUsername());
            redirectAttr.addFlashAttribute("successMsg", "Đã tạo khóa học: " + saved.getTitle());
            return "redirect:/teacher/courses/" + saved.getId();
        } catch (Exception e) {
            redirectAttr.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/teacher/courses/create";
        }
    }

    @GetMapping("/courses/{id}")
    public String viewCourse(@PathVariable Long id,
                             @AuthenticationPrincipal UserDetails ud,
                             Model model) {
        Course course = courseService.getCourseById(id);
        List<Lesson> lessons = lessonService.getLessonsByCourse(id);
        List<Enrollment> enrollments = enrollmentService.getEnrollmentsByCourse(id);
        model.addAttribute("course", course);
        model.addAttribute("lessons", lessons);
        model.addAttribute("enrollments", enrollments);
        return "teacher/courses/detail";
    }

    @GetMapping("/courses/{id}/edit")
    public String editCourseForm(@PathVariable Long id,
                                 @AuthenticationPrincipal UserDetails ud,
                                 Model model) {
        Course course = courseService.getCourseById(id);
        CourseDto.UpdateRequest form = new CourseDto.UpdateRequest();
        form.setTitle(course.getTitle());
        form.setDescription(course.getDescription());
        form.setThumbnailUrl(course.getThumbnailUrl());
        form.setMaxStudents(course.getMaxStudents());
        form.setStatus(course.getStatus());
        model.addAttribute("course", course);
        model.addAttribute("courseForm", form);
        model.addAttribute("statuses", Course.Status.values());
        return "teacher/courses/edit";
    }

    @PostMapping("/courses/{id}/edit")
    public String editCourse(@PathVariable Long id,
                             @Valid @ModelAttribute("courseForm") CourseDto.UpdateRequest request,
                             BindingResult result,
                             @AuthenticationPrincipal UserDetails ud,
                             RedirectAttributes redirectAttr,
                             Model model) {
        if (result.hasErrors()) {
            model.addAttribute("course", courseService.getCourseById(id));
            model.addAttribute("statuses", Course.Status.values());
            return "teacher/courses/edit";
        }
        try {
            courseService.updateCourse(id, request, ud.getUsername());
            redirectAttr.addFlashAttribute("successMsg", "Đã cập nhật khóa học thành công!");
        } catch (Exception e) {
            redirectAttr.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/teacher/courses/" + id;
    }

    @PostMapping("/courses/{id}/delete")
    public String deleteCourse(@PathVariable Long id,
                               @AuthenticationPrincipal UserDetails ud,
                               RedirectAttributes redirectAttr) {
        try {
            courseService.deleteCourse(id, ud.getUsername(), false);
            redirectAttr.addFlashAttribute("successMsg", "Đã xóa khóa học thành công!");
        } catch (Exception e) {
            redirectAttr.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/teacher/courses";
    }

    // ==================== BÀI HỌC ====================

    @GetMapping("/courses/{courseId}/lessons/create")
    public String createLessonForm(@PathVariable Long courseId, Model model) {
        model.addAttribute("courseId", courseId);
        model.addAttribute("course", courseService.getCourseById(courseId));
        model.addAttribute("lessonForm", new LessonDto.CreateRequest());
        return "teacher/lessons/create";
    }

    @PostMapping("/courses/{courseId}/lessons/create")
    public String createLesson(@PathVariable Long courseId,
                               @Valid @ModelAttribute("lessonForm") LessonDto.CreateRequest request,
                               BindingResult result,
                               @AuthenticationPrincipal UserDetails ud,
                               RedirectAttributes redirectAttr,
                               Model model) {
        if (result.hasErrors()) {
            model.addAttribute("courseId", courseId);
            model.addAttribute("course", courseService.getCourseById(courseId));
            return "teacher/lessons/create";
        }
        try {
            lessonService.createLesson(courseId, request, ud.getUsername());
            redirectAttr.addFlashAttribute("successMsg", "Đã thêm bài học thành công!");
        } catch (Exception e) {
            redirectAttr.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/teacher/courses/" + courseId;
    }

    @GetMapping("/courses/{courseId}/lessons/{lessonId}/edit")
    public String editLessonForm(@PathVariable Long courseId,
                                 @PathVariable Long lessonId,
                                 Model model) {
        Lesson lesson = lessonService.getLessonById(lessonId);
        LessonDto.UpdateRequest form = new LessonDto.UpdateRequest();
        form.setTitle(lesson.getTitle());
        form.setContent(lesson.getContent());
        form.setVideoUrl(lesson.getVideoUrl());
        form.setOrderIndex(lesson.getOrderIndex());
        form.setDurationMinutes(lesson.getDurationMinutes());
        form.setStatus(lesson.getStatus());
        model.addAttribute("courseId", courseId);
        model.addAttribute("lesson", lesson);
        model.addAttribute("lessonForm", form);
        model.addAttribute("statuses", Lesson.Status.values());
        return "teacher/lessons/edit";
    }

    @PostMapping("/courses/{courseId}/lessons/{lessonId}/edit")
    public String editLesson(@PathVariable Long courseId,
                             @PathVariable Long lessonId,
                             @Valid @ModelAttribute("lessonForm") LessonDto.UpdateRequest request,
                             BindingResult result,
                             @AuthenticationPrincipal UserDetails ud,
                             RedirectAttributes redirectAttr,
                             Model model) {
        if (result.hasErrors()) {
            model.addAttribute("courseId", courseId);
            model.addAttribute("lesson", lessonService.getLessonById(lessonId));
            model.addAttribute("statuses", Lesson.Status.values());
            return "teacher/lessons/edit";
        }
        try {
            lessonService.updateLesson(courseId, lessonId, request, ud.getUsername());
            redirectAttr.addFlashAttribute("successMsg", "Đã cập nhật bài học thành công!");
        } catch (Exception e) {
            redirectAttr.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/teacher/courses/" + courseId;
    }

    @PostMapping("/courses/{courseId}/lessons/{lessonId}/delete")
    public String deleteLesson(@PathVariable Long courseId,
                               @PathVariable Long lessonId,
                               @AuthenticationPrincipal UserDetails ud,
                               RedirectAttributes redirectAttr) {
        try {
            lessonService.deleteLesson(courseId, lessonId, ud.getUsername());
            redirectAttr.addFlashAttribute("successMsg", "Đã xóa bài học thành công!");
        } catch (Exception e) {
            redirectAttr.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/teacher/courses/" + courseId;
    }

    // ==================== QUẢN LÝ HỌC SINH (CRUD) ====================

    @GetMapping("/students")
    public String listStudents(@RequestParam(required = false) String keyword, Model model) {
        List<User> students = userService.searchUsersByRole(User.Role.STUDENT, keyword);
        model.addAttribute("students", students);
        model.addAttribute("keyword", keyword);
        return "teacher/students/list-all";
    }

    @GetMapping("/students/create")
    public String createStudentForm(Model model) {
        UserDto.CreateRequest form = new UserDto.CreateRequest();
        form.setRole(User.Role.STUDENT);
        model.addAttribute("userForm", form);
        return "teacher/students/create";
    }

    @PostMapping("/students/create")
    public String createStudent(@Valid @ModelAttribute("userForm") UserDto.CreateRequest request,
                                BindingResult result,
                                RedirectAttributes redirectAttr,
                                Model model) {
        request.setRole(User.Role.STUDENT);
        if (result.hasErrors()) return "teacher/students/create";
        try {
            User saved = userService.createUser(request);
            redirectAttr.addFlashAttribute("successMsg",
                    "Đã tạo học sinh thành công: " + saved.getUsername());
            return "redirect:/teacher/students";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMsg", e.getMessage());
            return "teacher/students/create";
        }
    }

    @GetMapping("/students/{id}")
    public String viewStudent(@PathVariable Long id, Model model) {
        User student = userService.getUserById(id);
        if (student.getRole() != User.Role.STUDENT) {
            return "redirect:/teacher/students";
        }
        model.addAttribute("user", student);
        return "teacher/students/detail";
    }

    @GetMapping("/students/{id}/edit")
    public String editStudentForm(@PathVariable Long id, Model model) {
        User student = userService.getUserById(id);
        if (student.getRole() != User.Role.STUDENT) {
            return "redirect:/teacher/students";
        }
        UserDto.UpdateRequest form = new UserDto.UpdateRequest();
        form.setFullName(student.getFullName());
        form.setEmail(student.getEmail());
        form.setPhone(student.getPhone());
        form.setAddress(student.getAddress());
        model.addAttribute("user", student);
        model.addAttribute("userForm", form);
        return "teacher/students/edit";
    }

    @PostMapping("/students/{id}/edit")
    public String editStudent(@PathVariable Long id,
                              @Valid @ModelAttribute("userForm") UserDto.UpdateRequest request,
                              BindingResult result,
                              @RequestParam(required = false) Boolean enabled,
                              RedirectAttributes redirectAttr,
                              Model model) {
        if (result.hasErrors()) {
            model.addAttribute("user", userService.getUserById(id));
            return "teacher/students/edit";
        }
        try {
            userService.updateUser(id, request);
            if (enabled != null) {
                userService.updateUserRoleAndStatus(id, null, enabled);
            }
            redirectAttr.addFlashAttribute("successMsg", "Đã cập nhật thông tin học sinh!");
        } catch (IllegalArgumentException e) {
            redirectAttr.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/teacher/students/" + id;
    }

    @PostMapping("/students/{id}/delete")
    public String deleteStudent(@PathVariable Long id, RedirectAttributes redirectAttr) {
        try {
            userService.deleteUser(id);
            redirectAttr.addFlashAttribute("successMsg", "Đã xóa tài khoản học sinh!");
        } catch (Exception e) {
            redirectAttr.addFlashAttribute("errorMsg", "Không thể xóa: " + e.getMessage());
        }
        return "redirect:/teacher/students";
    }

    // ==================== XEM & THÊM HỌC SINH VÀO KHÓA ====================

    @GetMapping("/courses/{courseId}/students")
    public String viewStudents(@PathVariable Long courseId,
                               @RequestParam(required = false) String keyword,
                               Model model) {
        Course course = courseService.getCourseById(courseId);
        List<Enrollment> enrollments = enrollmentService.getEnrollmentsByCourse(courseId);
        model.addAttribute("enrollments", enrollments);
        model.addAttribute("course", course);

        Set<Long> enrolledIds = enrollments.stream()
                .map(e -> e.getStudent().getId())
                .collect(Collectors.toSet());

        List<User> availableStudents = userService.searchUsersByRole(User.Role.STUDENT, keyword)
                .stream()
                .filter(s -> !enrolledIds.contains(s.getId()))
                .collect(Collectors.toList());
        model.addAttribute("availableStudents", availableStudents);
        model.addAttribute("keyword", keyword);
        return "teacher/students/list";
    }

    @PostMapping("/courses/{courseId}/students/add")
    public String addStudent(@PathVariable Long courseId,
                             @RequestParam Long studentId,
                             @AuthenticationPrincipal UserDetails ud,
                             RedirectAttributes redirectAttr) {
        try {
            enrollmentService.addStudentByTeacher(courseId, studentId, ud.getUsername());
            redirectAttr.addFlashAttribute("successMsg", "Đã thêm học sinh vào khóa học!");
        } catch (Exception e) {
            redirectAttr.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/teacher/courses/" + courseId + "/students";
    }
}
