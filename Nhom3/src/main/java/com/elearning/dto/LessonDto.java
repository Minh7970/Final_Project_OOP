package com.elearning.dto;

import com.elearning.model.Lesson;
import jakarta.validation.constraints.*;
import lombok.*;

public class LessonDto {

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        @NotBlank(message = "Tiêu đề bài học không được để trống")
        @Size(max = 200)
        private String title;

        private String content;
        private String videoUrl;
        private Integer orderIndex;
        private Integer durationMinutes;
    }

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    @Builder
    public static class UpdateRequest {
        @NotBlank(message = "Tiêu đề bài học không được để trống")
        @Size(max = 200)
        private String title;

        private String content;
        private String videoUrl;
        private Integer orderIndex;
        private Integer durationMinutes;
        private Lesson.Status status;
    }
}
