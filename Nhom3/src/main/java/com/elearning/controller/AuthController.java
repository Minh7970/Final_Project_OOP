package com.elearning.controller;

import com.elearning.dto.UserDto;
import com.elearning.model.User;
import com.elearning.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String loginPage(
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String logout,
            @RequestParam(required = false) String expired,
            Model model) {

        if (error != null) model.addAttribute("errorMsg", "Tên đăng nhập hoặc mật khẩu không đúng!");
        if (logout != null) model.addAttribute("successMsg", "Bạn đã đăng xuất thành công.");
        if (expired != null) model.addAttribute("errorMsg", "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.");
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        UserDto.CreateRequest form = new UserDto.CreateRequest();
        form.setRole(User.Role.STUDENT);
        model.addAttribute("userForm", form);
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("userForm") UserDto.CreateRequest request,
                           BindingResult result,
                           RedirectAttributes redirectAttr,
                           Model model) {
        if (request.getRole() == User.Role.ADMIN) {
            model.addAttribute("errorMsg", "Không thể đăng ký tài khoản Admin!");
            return "auth/register";
        }
        if (result.hasErrors()) return "auth/register";
        try {
            userService.createUser(request);
            redirectAttr.addFlashAttribute("successMsg", "Đăng ký thành công! Vui lòng đăng nhập.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMsg", e.getMessage());
            return "auth/register";
        }
    }

    @GetMapping("/access-denied")
    public String accessDenied(Model model) {
        model.addAttribute("errorTitle", "Không có quyền truy cập");
        model.addAttribute("errorMessage", "Bạn không có quyền thực hiện hành động này.");
        return "error/403";
    }
}
