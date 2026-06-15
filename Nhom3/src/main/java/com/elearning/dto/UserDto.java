package com.elearning.dto;

import com.elearning.model.User;
import jakarta.validation.constraints.*;
import lombok.*;

public class UserDto {

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        @NotBlank(message = "Username không được để trống")
        @Size(min = 3, max = 50, message = "Username phải từ 3-50 ký tự")
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username chỉ được chứa chữ, số và dấu gạch dưới")
        private String username;

        @NotBlank(message = "Mật khẩu không được để trống")
        @Size(min = 6, message = "Mật khẩu phải ít nhất 6 ký tự")
        private String password;

        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không hợp lệ")
        private String email;

        @NotBlank(message = "Họ tên không được để trống")
        @Size(max = 100, message = "Họ tên tối đa 100 ký tự")
        private String fullName;

        @Pattern(regexp = "^(\\+84|0)[0-9]{9,10}$", message = "Số điện thoại không hợp lệ")
        private String phone;

        private String address;

        @NotNull(message = "Vai trò không được để trống")
        private User.Role role;
    }

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    @Builder
    public static class UpdateRequest {
        @NotBlank(message = "Họ tên không được để trống")
        @Size(max = 100)
        private String fullName;

        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không hợp lệ")
        private String email;

        @Pattern(regexp = "^(\\+84|0)[0-9]{9,10}$", message = "Số điện thoại không hợp lệ")
        private String phone;

        private String address;

        // Chỉ cần điền nếu muốn đổi mật khẩu
        private String newPassword;
    }

    @Getter @Setter
    @Builder
    @NoArgsConstructor @AllArgsConstructor
    public static class Statistics {
        private long totalStudents;
        private long totalTeachers;
        private long totalAdmins;
        private long totalUsers;
    }

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    public static class ChangePasswordRequest {
        @NotBlank(message = "Mật khẩu cũ không được để trống")
        private String oldPassword;

        @NotBlank(message = "Mật khẩu mới không được để trống")
        @Size(min = 6, message = "Mật khẩu mới phải ít nhất 6 ký tự")
        private String newPassword;

        @NotBlank(message = "Xác nhận mật khẩu không được để trống")
        private String confirmPassword;
    }
}
