package com.elearning.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(columnNames = "username"),
    @UniqueConstraint(columnNames = "email")
})
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 3, max = 50)
    @Column(nullable = false, length = 50)
    private String username;

    @NotBlank
    @Column(nullable = false)
    private String password;

    @NotBlank
    @Email
    @Column(nullable = false, length = 100)
    private String email;

    @NotBlank
    @Size(max = 100)
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(length = 15)
    private String phone;

    @Column(length = 255)
    private String address;

    @Column(name = "avatar_url", length = 255)
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Quan hệ: 1 Teacher -> nhiều Course (cascade handled in service layer)
    @OneToMany(mappedBy = "teacher", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Course> teachingCourses = new HashSet<>();

    // Quan hệ: 1 Student -> nhiều Enrollment (cascade handled in service layer)
    @OneToMany(mappedBy = "student", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Enrollment> enrollments = new HashSet<>();

    public enum Role {
        ADMIN, TEACHER, STUDENT
    }
}
