package com.elearning.dto;

import com.elearning.model.Course;
import jakarta.validation.constraints.*;
import lombok.*;

public class CourseDto {

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        @NotBlank(message = "Tên khóa học không được để trống")
        @Size(max = 200, message = "Tên khóa học tối đa 200 ký tự")
        private String title;

        private String description;
        private String thumbnailUrl;

        @Min(value = 1, message = "Số học sinh tối đa phải lớn hơn 0")
        @Max(value = 1000, message = "Số học sinh tối đa không quá 1000")
        private Integer maxStudents;
    }

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    @Builder
    public static class UpdateRequest {
        @NotBlank(message = "Tên khóa học không được để trống")
        @Size(max = 200)
        private String title;

        private String description;
        private String thumbnailUrl;
        private Integer maxStudents;
        private Course.Status status;
    }
}
