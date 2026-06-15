package com.elearning.controller;

import com.elearning.dto.UserDto;
import com.elearning.model.Course;
import com.elearning.model.User;
import com.elearning.service.CourseService;
import com.elearning.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final CourseService courseService;

    // ==================== DASHBOARD ====================

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        try {
            UserDto.Statistics stats = userService.getStatistics();
            model.addAttribute("stats", stats);
        } catch (Exception e) {
            model.addAttribute("stats", UserDto.Statistics.builder()
                    .totalStudents(0).totalTeachers(0).totalAdmins(0).totalUsers(0).build());
        }
        try {
            model.addAttribute("totalCourses", courseService.getAllCourses().size());
        } catch (Exception e) {
            model.addAttribute("totalCourses", 0);
        }
        try {
            model.addAttribute("recentStudents", userService.getUsersByRole(User.Role.STUDENT)
                    .stream().limit(5).toList());
        } catch (Exception e) {
            model.addAttribute("recentStudents", java.util.List.of());
        }
        try {
            model.addAttribute("recentTeachers", userService.getUsersByRole(User.Role.TEACHER)
                    .stream().limit(5).toList());
        } catch (Exception e) {
            model.addAttribute("recentTeachers", java.util.List.of());
        }
        return "admin/dashboard";
    }

    // ==================== QUẢN LÝ HỌC SINH ====================

    @GetMapping("/students")
    public String listStudents(@RequestParam(required = false) String keyword, Model model) {
        List<User> students = userService.searchUsersByRole(User.Role.STUDENT, keyword);
        model.addAttribute("users", students);
        model.addAttribute("keyword", keyword);
        model.addAttribute("role", "STUDENT");
        model.addAttribute("roleLabel", "Học sinh");
        return "admin/users/list";
    }

    // ==================== QUẢN LÝ GIÁO VIÊN ====================

    @GetMapping("/teachers")
    public String listTeachers(@RequestParam(required = false) String keyword, Model model) {
        List<User> teachers = userService.searchUsersByRole(User.Role.TEACHER, keyword);
        model.addAttribute("users", teachers);
        model.addAttribute("keyword", keyword);
        model.addAttribute("role", "TEACHER");
        model.addAttribute("roleLabel", "Giáo viên");
        return "admin/users/list";
    }

    // ==================== TẠO USER MỚI ====================

    @GetMapping("/users/create")
    public String createUserForm(Model model) {
        model.addAttribute("userForm", new UserDto.CreateRequest());
        model.addAttribute("roles", List.of(User.Role.TEACHER, User.Role.STUDENT));
        return "admin/users/create";
    }

    @PostMapping("/users/create")
    public String createUser(@Valid @ModelAttribute("userForm") UserDto.CreateRequest request,
                             BindingResult result,
                             RedirectAttributes redirectAttr,
                             Model model) {
        if (result.hasErrors()) {
            model.addAttribute("roles", List.of(User.Role.TEACHER, User.Role.STUDENT));
            return "admin/users/create";
        }
        try {
            User saved = userService.createUser(request);
            redirectAttr.addFlashAttribute("successMsg",
                    "Đã tạo tài khoản thành công: " + saved.getUsername());
            return switch (saved.getRole()) {
                case STUDENT -> "redirect:/admin/students";
                case TEACHER -> "redirect:/admin/teachers";
                default      -> "redirect:/admin/dashboard";
            };
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMsg", e.getMessage());
            model.addAttribute("roles", List.of(User.Role.TEACHER, User.Role.STUDENT));
            return "admin/users/create";
        }
    }

    // ==================== XEM CHI TIẾT USER ====================

    @GetMapping("/users/{id}")
    public String viewUser(@PathVariable Long id, Model model) {
        User user = userService.getUserById(id);
        model.addAttribute("user", user);
        return "admin/users/detail";
    }

    // ==================== CHỈNH SỬA USER ====================

    @GetMapping("/users/{id}/edit")
    public String editUserForm(@PathVariable Long id, Model model) {
        User user = userService.getUserById(id);
        UserDto.UpdateRequest form = new UserDto.UpdateRequest();
        form.setFullName(user.getFullName());
        form.setEmail(user.getEmail());
        form.setPhone(user.getPhone());
        form.setAddress(user.getAddress());
        model.addAttribute("user", user);
        model.addAttribute("userForm", form);
        model.addAttribute("roles", List.of(User.Role.TEACHER, User.Role.STUDENT));
        return "admin/users/edit";
    }

    @PostMapping("/users/{id}/edit")
    public String editUser(@PathVariable Long id,
                           @Valid @ModelAttribute("userForm") UserDto.UpdateRequest request,
                           BindingResult result,
                           @RequestParam(required = false) User.Role role,
                           @RequestParam(required = false) Boolean enabled,
                           RedirectAttributes redirectAttr,
                           Model model) {
        if (result.hasErrors()) {
            model.addAttribute("user", userService.getUserById(id));
            model.addAttribute("roles", List.of(User.Role.TEACHER, User.Role.STUDENT));
            return "admin/users/edit";
        }
        try {
            userService.updateUser(id, request);
            if (role != null || enabled != null) {
                userService.updateUserRoleAndStatus(id, role, enabled);
            }
            redirectAttr.addFlashAttribute("successMsg", "Đã cập nhật thông tin thành công!");
        } catch (IllegalArgumentException e) {
            redirectAttr.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/users/" + id;
    }

    // ==================== XÓA USER ====================

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttr) {
        try {
            User user = userService.getUserById(id);
            User.Role role = user.getRole();
            userService.deleteUser(id);
            redirectAttr.addFlashAttribute("successMsg", "Đã xóa tài khoản thành công!");
            return switch (role) {
                case STUDENT -> "redirect:/admin/students";
                case TEACHER -> "redirect:/admin/teachers";
                default      -> "redirect:/admin/dashboard";
            };
        } catch (Exception e) {
            redirectAttr.addFlashAttribute("errorMsg", "Không thể xóa tài khoản: " + e.getMessage());
            return "redirect:/admin/dashboard";
        }
    }

    // ==================== QUẢN LÝ KHÓA HỌC ====================

    @GetMapping("/courses")
    public String listCourses(@RequestParam(required = false) String keyword, Model model) {
        List<Course> courses = courseService.searchCourses(keyword, true);
        model.addAttribute("courses", courses);
        model.addAttribute("keyword", keyword);
        return "admin/courses/list";
    }

    @GetMapping("/courses/{id}")
    public String viewCourse(@PathVariable Long id, Model model) {
        Course course = courseService.getCourseById(id);
        model.addAttribute("course", course);
        return "admin/courses/detail";
    }

    @PostMapping("/courses/{id}/status")
    public String updateCourseStatus(@PathVariable Long id,
                                     @RequestParam Course.Status status,
                                     RedirectAttributes redirectAttr) {
        courseService.updateCourseStatus(id, status);
        redirectAttr.addFlashAttribute("successMsg", "Đã cập nhật trạng thái khóa học!");
        return "redirect:/admin/courses";
    }

    @PostMapping("/courses/{id}/delete")
    public String deleteCourse(@PathVariable Long id, RedirectAttributes redirectAttr) {
        try {
            courseService.deleteCourse(id, null, true);
            redirectAttr.addFlashAttribute("successMsg", "Đã xóa khóa học thành công!");
        } catch (Exception e) {
            redirectAttr.addFlashAttribute("errorMsg", "Không thể xóa: " + e.getMessage());
        }
        return "redirect:/admin/courses";
    }
}
