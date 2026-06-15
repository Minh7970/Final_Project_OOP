# E-Learning Management System — Định nghĩa toàn bộ Project

## 1. TỔNG QUAN

| Thuộc tính | Giá trị |
|---|---|
| **Tên project** | E-Learning Management System |
| **Group ID** | com.elearning |
| **Artifact ID** | elearning |
| **Phiên bản** | 1.0.0 |
| **Java** | 17 |
| **Spring Boot** | 4.1.0 |
| **Database (production)** | MySQL |
| **Database (test)** | H2 |
| **Template engine** | Thymeleaf |
| **Build tool** | Maven |
| **Mô tả** | Hệ thống quản lý học tập trực tuyến với 3 vai trò: ADMIN, TEACHER, STUDENT |

---

## 2. CẤU TRÚC THƯ MỤC

```
Nhom3/
├── .github/modernize/java-upgrade/      # Hook scripts (không phải CI/CD)
├── pom.xml                              # Maven config
├── src/
│   ├── main/
│   │   ├── java/com/elearning/
│   │   │   ├── ElearningApplication.java
│   │   │   ├── config/
│   │   │   │   ├── DataInitializer.java
│   │   │   │   └── SecurityConfig.java
│   │   │   ├── controller/
│   │   │   │   ├── AdminController.java
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── StudentController.java
│   │   │   │   └── TeacherController.java
│   │   │   ├── dto/
│   │   │   │   ├── CourseDto.java
│   │   │   │   ├── LessonDto.java
│   │   │   │   └── UserDto.java
│   │   │   ├── exception/
│   │   │   │   ├── AccessDeniedException.java
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   └── ResourceNotFoundException.java
│   │   │   ├── model/
│   │   │   │   ├── User.java
│   │   │   │   ├── Course.java
│   │   │   │   ├── Lesson.java
│   │   │   │   └── Enrollment.java
│   │   │   ├── repository/
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── CourseRepository.java
│   │   │   │   ├── LessonRepository.java
│   │   │   │   └── EnrollmentRepository.java
│   │   │   └── service/
│   │   │       ├── UserService.java
│   │   │       ├── CourseService.java
│   │   │       ├── LessonService.java
│   │   │       ├── EnrollmentService.java
│   │   │       └── CustomUserDetailsService.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── templates/
│   │           ├── layout.html
│   │           ├── auth/
│   │           │   ├── login.html
│   │           │   └── register.html
│   │           ├── error/
│   │           │   ├── 400.html, 403.html, 404.html, 500.html
│   │           ├── admin/
│   │           │   ├── dashboard.html
│   │           │   ├── courses/detail.html, list.html
│   │           │   └── users/create.html, detail.html, edit.html, list.html
│   │           ├── teacher/
│   │           │   ├── dashboard.html
│   │           │   ├── courses/create.html, detail.html, edit.html, list.html
│   │           │   ├── lessons/create.html, edit.html
│   │           │   └── students/create.html, detail.html, edit.html, list.html, list-all.html
│   │           └── student/
│   │               ├── dashboard.html
│   │               ├── courses/detail.html, explore.html, my-courses.html
│   │               └── lessons/view.html
│   └── test/
│       ├── java/com/elearning/service/
│       │   ├── UserServiceTest.java
│       │   ├── CourseServiceTest.java
│       │   ├── LessonServiceTest.java
│       │   └── EnrollmentServiceTest.java
│       └── resources/application.properties
```

**Tổng số file:** 25 Java (main) + 4 Java (test) + 1 properties (main) + 1 properties (test) + 31 templates + 1 pom.xml = **63 files**

---

## 3. Ý NGHĨA TỪNG FILE & CHỨC NĂNG

### 3.1. Root Level

| File | Ý nghĩa & Chức năng |
|---|---|
| `pom.xml` | File cấu hình Maven: định nghĩa project là Spring Boot 4.1.0, Java 17, khai báo tất cả dependencies (Spring Web, Security, JPA, Thymeleaf, MySQL, Lombok, Validation, DevTools, Test). Cấu hình build plugins (spring-boot-maven-plugin, maven-compiler-plugin với Lombok annotation processor). |

### 3.2. Main Application

| File | Ý nghĩa & Chức năng |
|---|---|
| `ElearningApplication.java` | **Entry point** của ứng dụng. Annotation `@SpringBootApplication` kích hoạt auto-configuration, component scanning, và Spring Boot tự động cấu hình toàn bộ ứng dụng. Phương thức `main()` gọi `SpringApplication.run()` để khởi động server Tomcat embedded. |

### 3.3. Config Package

| File | Ý nghĩa & Chức năng |
|---|---|
| `SecurityConfig.java` | **Cấu hình bảo mật toàn bộ hệ thống.** Định nghĩa: (1) `PasswordEncoder` dùng BCrypt strength 12 để mã hóa mật khẩu, (2) `DaoAuthenticationProvider` tích hợp với `CustomUserDetailsService`, (3) `SessionRegistry` quản lý session, (4) `SecurityFilterChain` phân quyền URL theo role, cấu hình form login với redirect tùy role, logout xóa session, giới hạn 5 session/user, và trang access-denied. |
| `DataInitializer.java` | **Seed data khởi tạo.** Implement `CommandLineRunner`, tự động chạy khi ứng dụng start. Kiểm tra nếu chưa có tài khoản mẫu thì tạo 3 tài khoản demo: admin/Admin@123 (ADMIN), teacher01/Teacher@123 (TEACHER), student01/Student@123 (STUDENT). Giúp dev/test không cần nhập liệu thủ công. |

### 3.4. Model Package (Entities)

| File | Ý nghĩa & Chức năng |
|---|---|
| `User.java` | **Entity đại diện cho người dùng.** Lưu thông tin đăng nhập (username, password), thông tin cá nhân (email, fullName, phone, address, avatarUrl), phân quyền (role: ADMIN, TEACHER, STUDENT), trạng thái (enabled). Có quan hệ OneToMany với Course (giảng dạy) và Enrollment (ghi danh). Quan trọng nhất vì mọi chức năng đều xoay quanh User. |
| `Course.java` | **Entity đại diện cho khóa học.** Lưu thông tin: title, description, thumbnailUrl, trạng thái (ACTIVE/INACTIVE/DRAFT), maxStudents. Quan hệ ManyToOne với User (teacher - người tạo khóa), OneToMany với Lesson (danh sách bài học có thứ tự) và Enrollment (học sinh đăng ký). Có helper methods `getStudentCount()` và `getLessonCount()`. |
| `Lesson.java` | **Entity đại diện cho bài học trong khóa học.** Lưu: title, content (LONGTEXT - có thể chứa HTML), videoUrl, orderIndex (thứ tự sắp xếp), durationMinutes, status (PUBLISHED/DRAFT). Quan hệ ManyToOne với Course. Mỗi khóa học có thể có nhiều bài học sắp xếp theo thứ tự. |
| `Enrollment.java` | **Entity đại diện cho việc ghi danh học sinh vào khóa học.** Lưu: trạng thái (ACTIVE/COMPLETED/CANCELLED), progressPercent (tiến độ 0-100%), enrolledAt, completedAt. Có unique constraint (student_id, course_id) để đảm bảo 1 học sinh chỉ đăng ký 1 khóa 1 lần. Quan hệ ManyToOne với User (student) và Course. |

### 3.5. DTO Package

| File | Ý nghĩa & Chức năng |
|---|---|
| `UserDto.java` | **Data Transfer Object cho User.** Chứa 4 inner class: (1) `CreateRequest` - validation cho form đăng ký/tạo user (username 3-50 ký tự, password tối thiểu 6, email hợp lệ, phone regex Việt Nam), (2) `UpdateRequest` - cập nhật thông tin (có newPassword optional), (3) `Statistics` - DTO cho thống kê số lượng, (4) `ChangePasswordRequest` - đổi mật khẩu. Mục đích: tách biệt dữ liệu request/response khỏi Entity, bảo mật hơn và dễ validation. |
| `CourseDto.java` | **Data Transfer Object cho Course.** Chứa 2 inner class: (1) `CreateRequest` - validation tạo khóa (title bắt buộc, maxStudents 1-1000), (2) `UpdateRequest` - cập nhật khóa (có thêm status). |
| `LessonDto.java` | **Data Transfer Object cho Lesson.** Chứa 2 inner class: (1) `CreateRequest` - validation tạo bài học (title bắt buộc), (2) `UpdateRequest` - cập nhật bài học (có thêm status PUBLISHED/DRAFT). |

### 3.6. Repository Package

| File | Ý nghĩa & Chức năng |
|---|---|
| `UserRepository.java` | **Truy vấn dữ liệu User.** Interface JpaRepository với các phương thức: tìm theo username/email (cho login, kiểm tra trùng), tìm theo role (lọc danh sách), tìm kiếm với keyword (LIKE trên fullName, username, email), thống kê countByRole, xóa native với @Modifying. |
| `CourseRepository.java` | **Truy vấn dữ liệu Course.** Các truy vấn quan trọng: tìm theo teacher, tìm active, tìm kiếm với keyword (theo title, description, teacher name), tìm khóa học chưa đăng ký (NOT IN subquery), JOIN FETCH teacher để tránh N+1 query, xóa native. |
| `LessonRepository.java` | **Truy vấn dữ liệu Lesson.** Tìm bài học theo course (có sắp xếp orderIndex), tìm max orderIndex (để tự động đặt thứ tự bài mới), kiểm tra lesson thuộc course (bảo mật). Hỗ trợ xóa cascade. |
| `EnrollmentRepository.java` | **Truy vấn dữ liệu Enrollment.** Kiểm tra đã đăng ký, tìm enrollment active với JOIN FETCH (tránh N+1), đếm distinct students của 1 teacher, đếm số học sinh active trong course. Hỗ trợ xóa cascade. |

### 3.7. Service Package

| File | Ý nghĩa & Chức năng |
|---|---|
| `UserService.java` | **Xử lý nghiệp vụ User.** Lớp service lớn nhất (223 dòng). Xử lý: (1) Tạo user - kiểm tra trùng username/email, không cho tạo ADMIN từ form, mã hóa password, (2) Cập nhật - kiểm tra email trùng, có thể đổi password, (3) Xóa - phải disable trước, không xóa admin cuối cùng, xóa native SQL cascade, (4) Tìm kiếm với keyword, (5) Thống kê, (6) Đổi mật khẩu (kiểm tra mật khẩu cũ). |
| `CourseService.java` | **Xử lý nghiệp vụ Course.** Tạo khóa học mới, cập nhật (kiểm tra quyền sở hữu teacher), xóa kèm enrollment + lesson cascade, tìm kiếm đa dạng (admin xem all, student chỉ xem active, teacher chỉ xem của mình), lấy danh sách khóa học chưa đăng ký. |
| `LessonService.java` | **Xử lý nghiệp vụ Lesson.** Tạo bài học với tự động đặt orderIndex, cập nhật (kiểm tra quyền sở hữu), xóa (kiểm tra quyền). Đảm bảo bài học luôn thuộc đúng course và teacher sở hữu course đó mới được thao tác. |
| `EnrollmentService.java` | **Xử lý nghiệp vụ ghi danh.** (1) Học sinh tự đăng ký - kiểm tra trùng, kiểm tra maxStudents, (2) Hủy đăng ký - set CANCELLED, (3) Teacher thêm học sinh - kiểm tra quyền, kiểm tra role STUDENT, (4) Cập nhật tiến độ - clamp 0-100%, tự động đánh dấu COMPLETED khi đạt 100%. |
| `CustomUserDetailsService.java` | **Tích hợp Spring Security.** Implement `UserDetailsService`, load user từ database theo username, kiểm tra enabled, convert role thành `SimpleGrantedAuthority("ROLE_xxx")`. Đây là cầu nối giữa database và Spring Security authentication. |

### 3.8. Controller Package

| File | Ý nghĩa & Chức năng |
|---|---|
| `AuthController.java` | **Xử lý xác thực.** Phục vụ: (1) Trang login - hiển thị lỗi/thông báo logout/expired, (2) Đăng ký tài khoản mới (mặc định STUDENT, không cho đăng ký ADMIN), (3) Trang access-denied. |
| `AdminController.java` | **Admin quản trị hệ thống.** Cung cấp: dashboard thống kê (số lượng users, courses, recent users), quản lý học sinh/giáo viên (CRUD + search + khóa/mở tài khoản), quản lý khóa học (xem chi tiết, đổi trạng thái, xóa). Controller này có quyền cao nhất. |
| `TeacherController.java` | **Giáo viên quản lý giảng dạy.** Controller lớn nhất (373 dòng). Cung cấp: dashboard riêng, CRUD khóa học (chỉ khóa học của mình), CRUD bài học trong khóa, CRUD học sinh, quản lý học sinh trong khóa (xem danh sách đã enroll, thêm học sinh mới vào khóa). |
| `StudentController.java` | **Học sinh học tập.** Cung cấp: dashboard cá nhân, tìm kiếm khóa học (chỉ hiện khóa chưa đăng ký), xem chi tiết khóa học, đăng ký/hủy đăng ký, danh sách khóa đã học, xem bài học (kiểm tra quyền - phải đã enroll). |

### 3.9. Exception Package

| File | Ý nghĩa & Chức năng |
|---|---|
| `ResourceNotFoundException.java` | Exception tùy chỉnh khi không tìm thấy tài nguyên (User, Course, Lesson, Enrollment) trong database. Message tự động theo format: "Không tìm thấy {resource} với {field} = {value}". |
| `AccessDeniedException.java` | Exception tùy chỉnh khi người dùng không có quyền thao tác (vd: teacher sửa khóa học không phải của mình). |
| `GlobalExceptionHandler.java` | **Xử lý lỗi tập trung.** Dùng `@ControllerAdvice` để bắt tất cả exception và trả về trang lỗi tương ứng: ResourceNotFound → 404, AccessDenied → 403, IllegalArgumentException → 400, Exception không xác định → 500. Ghi log đầy đủ. |

### 3.10. Templates (Thymeleaf)

| File | Ý nghĩa & Chức năng |
|---|---|
| `layout.html` | **Layout chung cho toàn bộ ứng dụng.** Định nghĩa cấu trúc HTML5, sidebar điều hướng, header với thông tin user, footer. Sử dụng Thymeleaf fragments để các trang con kế thừa. Responsive với Bootstrap 5.3.2, icon với Bootstrap Icons. Sidebar tự động highlight menu theo role và trang hiện tại. |
| `auth/login.html` | **Trang đăng nhập.** Form login với username/password, hiển thị thông báo lỗi/thành công. Kèm thông tin 3 tài khoản demo để tiện test. |
| `auth/register.html` | **Trang đăng ký.** Form đăng ký tài khoản mới với validation client-side (HTML5), chỉ cho đăng ký role STUDENT. |
| `error/400.html` | Trang báo lỗi "Dữ liệu không hợp lệ" (Bad Request) - khi validation fail hoặc illegal argument. |
| `error/403.html` | Trang báo lỗi "Không có quyền truy cập" (Forbidden) - khi user không đủ quyền. |
| `error/404.html` | Trang báo lỗi "Không tìm thấy" (Not Found) - khi tài nguyên không tồn tại. |
| `error/500.html` | Trang báo lỗi "Lỗi hệ thống" (Internal Server Error) - khi có exception không xác định. |
| `admin/dashboard.html` | Dashboard admin: thống kê tổng quan (students, teachers, admins, courses), danh sách 5 học sinh/giáo viên gần đây. |
| `admin/users/list.html` | Danh sách user (dùng chung cho student/teacher list), có search, phân loại bằng badge màu. |
| `admin/users/create.html` | Form tạo user với đầy đủ validation, chọn role (TEACHER/STUDENT). |
| `admin/users/detail.html` | Xem thông tin chi tiết user, các action (edit, delete). |
| `admin/users/edit.html` | Form sửa user, có thể thay đổi role và trạng thái enabled. |
| `admin/courses/list.html` | Grid danh sách khóa học dạng card, có search và badge trạng thái. |
| `admin/courses/detail.html` | Chi tiết khóa học + danh sách bài học. |
| `teacher/dashboard.html` | Dashboard giáo viên: thống kê khóa học, học sinh đang dạy. |
| `teacher/courses/list.html` | Danh sách khóa học của giáo viên, có search. |
| `teacher/courses/create.html` | Form tạo khóa học mới. |
| `teacher/courses/detail.html` | Chi tiết khóa học + quản lý bài học (thêm/sửa/xóa) + danh sách học sinh đã enroll. |
| `teacher/courses/edit.html` | Form sửa khóa học, có thể đổi trạng thái. |
| `teacher/lessons/create.html` | Form thêm bài học mới vào khóa học. |
| `teacher/lessons/edit.html` | Form sửa bài học, có thể publish/draft. |
| `teacher/students/list-all.html` | Danh sách tất cả học sinh trong hệ thống (teacher xem). |
| `teacher/students/list.html` | Quản lý học sinh trong 1 khóa học: xem danh sách đã enroll + thêm học sinh mới. |
| `teacher/students/create.html` | Form tạo tài khoản học sinh mới. |
| `teacher/students/detail.html` | Xem chi tiết học sinh. |
| `teacher/students/edit.html` | Form sửa thông tin học sinh. |
| `student/dashboard.html` | Dashboard học sinh: danh sách khóa đã đăng ký, thống kê (đã học, đã hoàn thành). |
| `student/courses/explore.html` | Tìm kiếm và duyệt khóa học (chỉ hiện khóa chưa đăng ký). |
| `student/courses/detail.html` | Chi tiết khóa học: nếu chưa enroll → button đăng ký, nếu đã enroll → danh sách bài học. |
| `student/courses/my-courses.html` | Danh sách khóa học đã đăng ký (đang học + đã hoàn thành). |
| `student/lessons/view.html` | Xem nội dung bài học: title, content (HTML), videoUrl, navigation giữa các bài. |

### 3.11. Test Package

| File | Ý nghĩa & Chức năng |
|---|---|
| `UserServiceTest.java` | Unit test cho UserService (~18 tests). Dùng Mockito mock UserRepository và EntityManager. Test: tạo user (thành công, trùng username, trùng email, admin role throw), tìm user (byId, byUsername, notFound), cập nhật (thành công, email trùng, đổi password), xóa (enabled throw, last admin throw, các role khác), tìm kiếm, thống kê, đổi mật khẩu. |
| `CourseServiceTest.java` | Unit test cho CourseService (~12 tests). Mock CourseRepository, EnrollmentRepository, LessonRepository, UserService. Test: tạo khóa, lấy active courses, lấy courses by teacher, tìm byId, cập nhật (thành công, wrong teacher), cập nhật status, xóa (admin, teacher-owner, teacher-non-owner), tìm kiếm. |
| `LessonServiceTest.java` | Unit test cho LessonService (~8 tests). Mock LessonRepository, CourseService. Test: tạo lesson (thành công, wrong teacher, custom orderIndex), lấy lessons by course, tìm byId, cập nhật (thành công, wrong teacher), xóa (thành công, wrong teacher). |
| `EnrollmentServiceTest.java` | Unit test cho EnrollmentService (~12 tests). Mock EnrollmentRepository, CourseService, UserService. Test: enroll (thành công, đã enrolled, full), cancel (thành công, not found), lấy enrollments, kiểm tra isEnrolled, updateProgress (partial, 100%, clamp), teacher thêm student (thành công, sai quyền, không phải student, đã enrolled, full). |

### 3.12. Config Files

| File | Ý nghĩa & Chức năng |
|---|---|
| `application.properties` (main) | Cấu hình production: kết nối MySQL (localhost:3306/elearning_db), Hikari connection pool, JPA/Hibernate (ddl-auto=update, show-sql, format_sql, MySQLDialect), Thymeleaf (UTF-8, cache=false dev mode), logging (DEBUG cho security, elearning, Hibernate SQL). |
| `application.properties` (test) | Cấu hình test: dùng H2 in-memory (mem:testdb), H2Dialect, ddl-auto=create-drop (tạo bảng mới mỗi lần chạy test), tắt show-sql và thymeleaf cache. |

---

## 4. PACKAGE: `com.elearning` — Application Entry

### ElearningApplication.java
```java
package com.elearning;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ElearningApplication {
    public static void main(String[] args) {
        SpringApplication.run(ElearningApplication.class, args);
    }
}
```

---

## 5. PACKAGE: `com.elearning.config` — Cấu hình

### DataInitializer.java
- `@Component`, implements `CommandLineRunner`
- Tự động tạo tài khoản demo khi chạy lần đầu:
  - `admin / Admin@123` (ADMIN)
  - `teacher01 / Teacher@123` (TEACHER)
  - `student01 / Student@123` (STUDENT)
- Sử dụng `PasswordEncoder` (BCrypt) để mã hóa mật khẩu

### SecurityConfig.java
- `@Configuration`, `@EnableWebSecurity`, `@EnableMethodSecurity`
- **PasswordEncoder:** BCrypt với strength = 12
- **AuthenticationProvider:** DaoAuthenticationProvider với CustomUserDetailsService
- **SessionRegistry:** SessionRegistryImpl (hỗ trợ quản lý session)
- **SecurityFilterChain:**
  - Public: `/css/**`, `/js/**`, `/images/**`, `/webjars/**`, `/login`, `/register`
  - ADMIN: `/admin/**`
  - TEACHER: `/teacher/**`
  - STUDENT: `/student/**`
  - Authenticated: `/profile/**`, `/change-password`
  - AnyRequest: authenticated
  - Login form: custom login page, success handler redirect theo role
  - Logout: invalidate session, delete JSESSIONID cookie
  - Session: tối đa 5 session
  - Access denied: `/access-denied`

---

## 6. PACKAGE: `com.elearning.model` — Entities (JPA)

### User.java — Bảng `users`
| Field | Type | Constraints |
|---|---|---|
| id | Long (PK, Identity) | |
| username | String (50) | `@NotBlank`, unique, 3-50 ký tự |
| password | String | `@NotBlank`, mã hóa BCrypt |
| email | String (100) | `@NotBlank`, `@Email`, unique |
| fullName | String (100) | `@NotBlank` |
| phone | String (15) | Có thể null, regex `^(\\+84\|0)[0-9]{9,10}$` |
| address | String (255) | Nullable |
| avatarUrl | String (255) | Nullable |
| role | Enum(Role) | ADMIN, TEACHER, STUDENT |
| enabled | Boolean | Default true |
| createdAt | LocalDateTime | `@CreationTimestamp` |
| updatedAt | LocalDateTime | `@UpdateTimestamp` |

**Relationships:**
- `@OneToMany(mappedBy="teacher")` -> `teachingCourses: Set<Course>`
- `@OneToMany(mappedBy="student")` -> `enrollments: Set<Enrollment>`

### Course.java — Bảng `courses`
| Field | Type | Constraints |
|---|---|---|
| id | Long (PK, Identity) | |
| title | String (200) | `@NotBlank` |
| description | TEXT | Nullable |
| thumbnailUrl | String (255) | Nullable |
| status | Enum(Status) | ACTIVE, INACTIVE, DRAFT. Default ACTIVE |
| maxStudents | Integer | Default 100 |
| teacher | ManyToOne(User) | `@JoinColumn(name="teacher_id")` |
| createdAt | LocalDateTime | `@CreationTimestamp` |
| updatedAt | LocalDateTime | `@UpdateTimestamp` |

**Relationships:**
- `@ManyToOne(fetch=LAZY)` -> `teacher: User`
- `@OneToMany(mappedBy="course", cascade=ALL, orphanRemoval=true, orderBy="orderIndex ASC")` -> `lessons: List<Lesson>`
- `@OneToMany(mappedBy="course", cascade=ALL)` -> `enrollments: Set<Enrollment>`

**Helpers:** `getStudentCount()`, `getLessonCount()`

### Lesson.java — Bảng `lessons`
| Field | Type | Constraints |
|---|---|---|
| id | Long (PK, Identity) | |
| title | String (200) | `@NotBlank` |
| content | LONGTEXT | Nullable |
| videoUrl | String (255) | Nullable |
| orderIndex | Integer | Default 1, `@Column(nullable=false)` |
| durationMinutes | Integer | Nullable |
| status | Enum(Status) | PUBLISHED, DRAFT. Default PUBLISHED |
| course | ManyToOne(Course) | `@JoinColumn(name="course_id")` |
| createdAt | LocalDateTime | `@CreationTimestamp` |
| updatedAt | LocalDateTime | `@UpdateTimestamp` |

### Enrollment.java — Bảng `enrollments`
| Field | Type | Constraints |
|---|---|---|
| id | Long (PK, Identity) | |
| student | ManyToOne(User) | `@JoinColumn(name="student_id")` |
| course | ManyToOne(Course) | `@JoinColumn(name="course_id")` |
| status | Enum(Status) | ACTIVE, COMPLETED, CANCELLED. Default ACTIVE |
| progressPercent | Integer | Default 0 |
| enrolledAt | LocalDateTime | `@CreationTimestamp` |
| completedAt | LocalDateTime | Nullable |

**UniqueConstraint:** `(student_id, course_id)`

---

## 7. PACKAGE: `com.elearning.dto` — Data Transfer Objects

### UserDto.java
- **CreateRequest:** username, password, email, fullName, phone, address, role (có validation)
- **UpdateRequest:** fullName, email, phone, address, newPassword (optional)
- **Statistics:** totalStudents, totalTeachers, totalAdmins, totalUsers
- **ChangePasswordRequest:** oldPassword, newPassword, confirmPassword

### CourseDto.java
- **CreateRequest:** title (`@NotBlank`, max 200), description, thumbnailUrl, maxStudents (`@Min(1)`, `@Max(1000)`)
- **UpdateRequest:** title, description, thumbnailUrl, maxStudents, status

### LessonDto.java
- **CreateRequest:** title (`@NotBlank`, max 200), content, videoUrl, orderIndex, durationMinutes
- **UpdateRequest:** title, content, videoUrl, orderIndex, durationMinutes, status

---

## 8. PACKAGE: `com.elearning.repository` — JPA Repositories

### UserRepository
| Method | Query |
|---|---|
| `findByUsername(username)` | Derived |
| `findByEmail(email)` | Derived |
| `existsByUsername(username)` | Derived |
| `existsByEmail(email)` | Derived |
| `findByRole(role)` | Derived |
| `searchByRoleAndKeyword(role, keyword)` | JPQL: LIKE trên fullName, username, email + role filter |
| `searchByKeyword(keyword)` | JPQL: LIKE trên fullName, username, email |
| `countByRole(role)` | Derived |
| `deleteUserById(id)` | JPQL DELETE + `@Modifying` |

### CourseRepository
| Method | Query |
|---|---|
| `findByTeacher(teacher)` | Derived |
| `findByTeacherId(teacherId)` | Derived |
| `findByStatus(status)` | Derived |
| `searchActiveCoursesByKeyword(keyword)` | JPQL: status='ACTIVE' + title/description LIKE |
| `searchAllByKeyword(keyword)` | JPQL: title/description LIKE (không filter status) |
| `searchByTeacherAndKeyword(teacher, keyword)` | JPQL: teacher + title/description LIKE |
| `findAvailableCoursesForStudent(studentId)` | JPQL: ACTIVE + NOT IN (enrollments của student, status != CANCELLED) |
| `findAllActiveCoursesWithTeacher()` | JPQL: JOIN FETCH teacher, WHERE status='ACTIVE' |
| `searchByTitleOrTeacherName(keyword)` | JPQL: JOIN teacher, title/teacher.fullName LIKE |
| `deleteByTeacherId(teacherId)` | JPQL DELETE + `@Modifying` |
| `deleteCourseById(id)` | JPQL DELETE + `@Modifying` |

### LessonRepository
| Method | Query |
|---|---|
| `findByCourseIdOrderByOrderIndexAsc(courseId)` | Derived |
| `findByCourseIdAndStatusOrderByOrderIndexAsc(courseId, status)` | Derived |
| `findMaxOrderIndexByCourseId(courseId)` | JPQL: `COALESCE(MAX(l.orderIndex), 0)` |
| `countByCourseId(courseId)` | Derived |
| `findByIdAndCourseId(id, courseId)` | Derived (bảo mật) |
| `deleteLessonById(id)` | JPQL DELETE + `@Modifying` |
| `deleteByCourseId(courseId)` | JPQL DELETE + `@Modifying` |
| `deleteByTeacherId(teacherId)` | JPQL DELETE subquery + `@Modifying` |

### EnrollmentRepository
| Method | Query |
|---|---|
| `existsByStudentIdAndCourseIdAndStatusNot(studentId, courseId, status)` | Derived |
| `findByStudentIdAndCourseId(studentId, courseId)` | Derived |
| `findActiveEnrollmentsByStudentId(studentId)` | JPQL: JOIN FETCH course, JOIN FETCH teacher, status != CANCELLED |
| `findActiveEnrollmentsByCourseId(courseId)` | JPQL: JOIN FETCH student, status != CANCELLED |
| `countDistinctStudentsByTeacherId(teacherId)` | JPQL: COUNT DISTINCT student.id |
| `countByCourseIdAndStatus(courseId, status)` | Derived |
| `findByStudentId(studentId)` | Derived |
| `deleteByStudentId(studentId)` | JPQL DELETE + `@Modifying` |
| `deleteByCourseId(courseId)` | JPQL DELETE + `@Modifying` |
| `deleteByTeacherId(teacherId)` | JPQL DELETE subquery + `@Modifying` |

---

## 9. PACKAGE: `com.elearning.service` — Business Logic

### UserService.java (223 dòng)
- `createUser(request)`: Kiểm tra trùng username/email, không cho tạo ADMIN, mã hóa password
- `getAllUsers()`, `getUsersByRole(role)`, `getUserById(id)`, `getUserByUsername(username)`
- `updateUser(id, request)`: Cập nhật profile, kiểm tra email trùng, có thể đổi password
- `updateUserRoleAndStatus(id, role, enabled)`: Admin thay đổi role/status, không cho set ADMIN
- `deleteUser(id)`: Kiểm tra enabled=false, không xóa admin cuối cùng, native SQL xóa cascade
- `searchUsers(keyword)`, `searchUsersByRole(role, keyword)`
- `getStatistics()`: Đếm số lượng theo role
- `changePassword(username, oldPassword, newPassword)`: Kiểm tra mật khẩu cũ

### CourseService.java (144 dòng)
- `createCourse(request, teacherUsername)`: Tạo khóa học mới
- `getAllCourses()`, `getActiveCourses()`, `getCoursesByTeacher(username)`, `getCourseById(id)`
- `updateCourse(id, request, username)`: Cập nhật, kiểm tra quyền sở hữu
- `updateCourseStatus(id, status)`: Admin cập nhật trạng thái
- `deleteCourse(id, username, isAdmin)`: Xóa kèm enrollment + lesson liên quan
- `searchCourses(keyword, adminView)`: Tìm kiếm (admin xem tất cả, student chỉ active)
- `searchTeacherCourses(username, keyword)`: Tìm kiếm theo teacher
- `getAvailableCoursesForStudent(studentId, keyword)`: Khóa học chưa đăng ký

### LessonService.java (93 dòng)
- `createLesson(courseId, request, teacherUsername)`: Tạo bài học, tự động orderIndex
- `getLessonsByCourse(courseId)`, `getLessonById(lessonId)`
- `updateLesson(courseId, lessonId, request, teacherUsername)`: Kiểm tra quyền sở hữu
- `deleteLesson(courseId, lessonId, teacherUsername)`: Kiểm tra quyền sở hữu
- `verifyTeacherOwnership(course, username)`: Helper kiểm tra teacher sở hữu course

### EnrollmentService.java (139 dòng)
- `enrollCourse(courseId, studentUsername)`: Kiểm tra trùng, kiểm tra maxStudents
- `cancelEnrollment(courseId, studentUsername)`: Set status = CANCELLED
- `getStudentEnrollments(studentUsername)`: JOIN FETCH để tránh N+1
- `getEnrollmentsByCourse(courseId)`: JOIN FETCH student
- `isEnrolled(studentId, courseId)`: Kiểm tra đã đăng ký chưa
- `addStudentByTeacher(courseId, studentId, teacherUsername)`: Teacher thêm học sinh
- `updateProgress(courseId, studentUsername, progressPercent)`: Clamp 0-100, tự động COMPLETED nếu 100%

### CustomUserDetailsService.java (44 dòng)
- Implements `UserDetailsService`
- `loadUserByUsername(username)`: Tìm user, kiểm tra enabled, trả về `org.springframework.security.core.userdetails.User`
- `buildAuthorities(role)`: `ROLE_ADMIN`, `ROLE_TEACHER`, `ROLE_STUDENT`

---

## 10. PACKAGE: `com.elearning.controller` — Controllers (MVC)

### AuthController.java — `/login`, `/register`
| Method | Endpoint | Mô tả |
|---|---|---|
| GET | `/login` | Trang đăng nhập, nhận params error/logout/expired |
| GET | `/register` | Form đăng ký (mặc định role STUDENT) |
| POST | `/register` | Xử lý đăng ký, không cho đăng ký ADMIN |
| GET | `/access-denied` | Trang báo lỗi 403 |

### AdminController.java — `/admin/**`
| Method | Endpoint | Mô tả |
|---|---|---|
| GET | `/admin/dashboard` | Dashboard với thống kê, recent students/teachers |
| GET | `/admin/students` | Danh sách học sinh (có search) |
| GET | `/admin/teachers` | Danh sách giáo viên (có search) |
| GET | `/admin/users/create` | Form tạo user (TEACHER hoặc STUDENT) |
| POST | `/admin/users/create` | Xử lý tạo user |
| GET | `/admin/users/{id}` | Xem chi tiết user |
| GET | `/admin/users/{id}/edit` | Form sửa user |
| POST | `/admin/users/{id}/edit` | Xử lý sửa user (có thể đổi role/status) |
| POST | `/admin/users/{id}/delete` | Xóa user (phải disabled trước) |
| GET | `/admin/courses` | Danh sách khóa học (có search) |
| GET | `/admin/courses/{id}` | Xem chi tiết khóa học |
| POST | `/admin/courses/{id}/status` | Cập nhật trạng thái khóa học |
| POST | `/admin/courses/{id}/delete` | Xóa khóa học |

### TeacherController.java — `/teacher/**` (373 dòng, controller lớn nhất)
| Method | Endpoint | Mô tả |
|---|---|---|
| GET | `/teacher/dashboard` | Dashboard với thống kê khóa học, học sinh |
| GET | `/teacher/courses` | Danh sách khóa học của giáo viên |
| GET | `/teacher/courses/create` | Form tạo khóa học |
| POST | `/teacher/courses/create` | Xử lý tạo khóa học |
| GET | `/teacher/courses/{id}` | Chi tiết khóa học + lessons + enrollments |
| GET | `/teacher/courses/{id}/edit` | Form sửa khóa học |
| POST | `/teacher/courses/{id}/edit` | Xử lý sửa khóa học |
| POST | `/teacher/courses/{id}/delete` | Xóa khóa học |
| GET | `/teacher/courses/{courseId}/lessons/create` | Form tạo bài học |
| POST | `/teacher/courses/{courseId}/lessons/create` | Xử lý tạo bài học |
| GET | `/teacher/courses/{courseId}/lessons/{lessonId}/edit` | Form sửa bài học |
| POST | `/teacher/courses/{courseId}/lessons/{lessonId}/edit` | Xử lý sửa bài học |
| POST | `/teacher/courses/{courseId}/lessons/{lessonId}/delete` | Xóa bài học |
| GET | `/teacher/students` | Danh sách tất cả học sinh |
| GET | `/teacher/students/create` | Form tạo học sinh |
| POST | `/teacher/students/create` | Xử lý tạo học sinh |
| GET | `/teacher/students/{id}` | Xem chi tiết học sinh |
| GET | `/teacher/students/{id}/edit` | Form sửa học sinh |
| POST | `/teacher/students/{id}/edit` | Xử lý sửa học sinh |
| POST | `/teacher/students/{id}/delete` | Xóa học sinh |
| GET | `/teacher/courses/{courseId}/students` | Xem học sinh trong khóa + thêm học sinh |
| POST | `/teacher/courses/{courseId}/students/add` | Thêm học sinh vào khóa học |

### StudentController.java — `/student/**`
| Method | Endpoint | Mô tả |
|---|---|---|
| GET | `/student/dashboard` | Dashboard với danh sách khóa học đã đăng ký |
| GET | `/student/courses` | Tìm kiếm khóa học (chưa đăng ký) |
| GET | `/student/courses/{id}` | Chi tiết khóa học (nếu đã enroll thì xem lessons) |
| POST | `/student/courses/{id}/enroll` | Đăng ký khóa học |
| POST | `/student/courses/{id}/cancel` | Hủy đăng ký |
| GET | `/student/my-courses` | Khóa học đã đăng ký |
| GET | `/student/courses/{courseId}/lessons/{lessonId}` | Xem bài học (kiểm tra quyền) |

---

## 11. PACKAGE: `com.elearning.exception` — Exception Handling

| Class | Mô tả |
|---|---|
| `ResourceNotFoundException` | RuntimeException, message: "Không tìm thấy {resource} với {field} = {value}" |
| `AccessDeniedException` | RuntimeException |
| `GlobalExceptionHandler` | `@ControllerAdvice` xử lý: ResourceNotFound -> 404, AccessDenied -> 403, IllegalArgumentException -> 400, Exception -> 500 |

---

## 12. THYMELEAF TEMPLATES

### Layout (`layout.html`)
- Layout chung với sidebar điều hướng
- Hiển thị thông tin user đăng nhập
- Role-based menu: ADMIN, TEACHER, STUDENT
- Blocks: content, sidebar, scripts
- CDN: Bootstrap 5.3.2 CSS/JS + Bootstrap Icons 1.11.3

### Auth
- `login.html`: Form login với thông tin tài khoản demo, xử lý lỗi/đăng xuất/hết hạn
- `register.html`: Form đăng ký với validation client-side

### Error
- `400.html`: Dữ liệu không hợp lệ
- `403.html`: Không có quyền truy cập
- `404.html`: Không tìm thấy tài nguyên
- `500.html`: Lỗi hệ thống

### Admin (4 templates)
- `dashboard.html`: Thống kê (users, courses), recent students/teachers
- `users/list.html`: Danh sách user với search, badge role, actions
- `users/create.html`: Form tạo user (chọn TEACHER/STUDENT)
- `users/detail.html`: Thông tin chi tiết user
- `users/edit.html`: Form sửa user + role/status
- `courses/list.html`: Grid course cards
- `courses/detail.html`: Course detail + lesson list

### Teacher (14 templates)
- `dashboard.html`: Thống kê courses + students
- `courses/list.html`, `create.html`, `detail.html`, `edit.html`: CRUD khóa học
- `lessons/create.html`, `edit.html`: CRUD bài học
- `students/list-all.html`: Danh sách tất cả học sinh
- `students/list.html`: Quản lý enrollment trong khóa học
- `students/create.html`, `detail.html`, `edit.html`: CRUD học sinh

### Student (5 templates)
- `dashboard.html`: Enrollments stats + active courses
- `courses/explore.html`: Tìm kiếm/browse khóa học
- `courses/detail.html`: Chi tiết + enroll/cancel
- `courses/my-courses.html`: Khóa học đã đăng ký
- `lessons/view.html`: Xem bài học với navigation

---

## 13. CẤU HÌNH

### application.properties (main)
```properties
spring.application.name=E-Learning Management System
server.port=8080

# MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/elearning_db?useSSL=false&serverTimezone=Asia/Ho_Chi_Minh&allowPublicKeyRetrieval=true&characterEncoding=UTF-8
spring.datasource.username=root
spring.datasource.password=
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# Hikari Pool
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
spring.jpa.properties.hibernate.jdbc.time_zone=Asia/Ho_Chi_Minh
spring.jpa.open-in-view=true

# Thymeleaf
spring.thymeleaf.encoding=UTF-8
spring.thymeleaf.cache=false

# Logging
logging.level.org.springframework.security=DEBUG
logging.level.com.elearning=DEBUG
logging.level.org.hibernate.SQL=DEBUG
```

### application.properties (test)
```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=false
spring.thymeleaf.cache=false
```

---

## 14. DEPENDENCIES (pom.xml)

| Dependency | Usage |
|---|---|
| `spring-boot-starter-web` | REST/Web MVC |
| `spring-boot-starter-security` | Authentication & Authorization |
| `spring-boot-starter-data-jpa` | JPA/Hibernate ORM |
| `spring-boot-starter-thymeleaf` | Template engine |
| `thymeleaf-extras-springsecurity6` | Security integration with Thymeleaf |
| `spring-boot-starter-validation` | Bean Validation (Jakarta) |
| `mysql-connector-j` (runtime) | MySQL driver |
| `lombok` 1.18.46 (provided) | Boilerplate reduction |
| `spring-boot-devtools` (runtime) | Hot reload |
| `spring-boot-starter-test` (test) | JUnit 5 + Mockito |
| `spring-security-test` (test) | Security test support |
| `h2` (test) | In-memory database for testing |

---

## 15. MODULES & CLASS DIAGRAM

```
┌─────────────────────────────────────────────────────────────────────┐
│                        ElearningApplication                         │
│                        (@SpringBootApplication)                     │
└─────────────────────────────────────────────────────────────────────┘
                                    │
          ┌─────────────────────────┼─────────────────────────┐
          │                         │                         │
          ▼                         ▼                         ▼
┌──────────────────┐   ┌─────────────────────┐   ┌──────────────────────┐
│   SecurityConfig  │   │   DataInitializer   │   │ GlobalExceptionHandler│
│   (@Configuration)│   │   (CommandLineRunner)│   │   (@ControllerAdvice) │
└──────────────────┘   └─────────────────────┘   └──────────────────────┘
          │
          ▼
┌──────────────────────────────────────────────────────────────────────┐
│                        Security Filter Chain                         │
│  Public → Login → /admin/** (ADMIN) → /teacher/** (TEACHER) → ...   │
└──────────────────────────────────────────────────────────────────────┘

┌──────────────────┐       ┌──────────────────┐       ┌──────────────────┐
│    Controllers   │──────▶│     Services     │──────▶│   Repositories   │
├──────────────────┤       ├──────────────────┤       ├──────────────────┤
│  AuthController  │       │  UserService     │       │  UserRepository  │
│  AdminController │       │  CourseService   │       │  CourseRepository│
│  TeacherController│      │  LessonService   │       │  LessonRepository│
│  StudentController│      │  EnrollmentService│      │ EnrollmentRepo   │
└──────────────────┘       │ CustomUserDetails │       └──────────────────┘
                           └──────────────────┘               │
                                                               ▼
                                                      ┌──────────────────┐
                                                      │     Entities     │
                                                      ├──────────────────┤
                                                      │  User (users)    │
                                                      │  Course (courses) │
                                                      │  Lesson (lessons) │
                                                      │  Enrollment (enr) │
                                                      └──────────────────┘
```

---

## 16. DATABASE SCHEMA (MySQL — tự động tạo bởi JPA ddl-auto=update)

```sql
-- Bảng users
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(15),
    address VARCHAR(255),
    avatar_url VARCHAR(255),
    role VARCHAR(20) NOT NULL,  -- ADMIN, TEACHER, STUDENT
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME,
    updated_at DATETIME
);

-- Bảng courses
CREATE TABLE courses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    thumbnail_url VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',  -- ACTIVE, INACTIVE, DRAFT
    max_students INT DEFAULT 100,
    teacher_id BIGINT NOT NULL,
    created_at DATETIME,
    updated_at DATETIME,
    FOREIGN KEY (teacher_id) REFERENCES users(id)
);

-- Bảng lessons
CREATE TABLE lessons (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content LONGTEXT,
    video_url VARCHAR(255),
    order_index INT NOT NULL DEFAULT 1,
    duration_minutes INT,
    status VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED',  -- PUBLISHED, DRAFT
    course_id BIGINT NOT NULL,
    created_at DATETIME,
    updated_at DATETIME,
    FOREIGN KEY (course_id) REFERENCES courses(id)
);

-- Bảng enrollments
CREATE TABLE enrollments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',  -- ACTIVE, COMPLETED, CANCELLED
    progress_percent INT DEFAULT 0,
    enrolled_at DATETIME,
    completed_at DATETIME,
    FOREIGN KEY (student_id) REFERENCES users(id),
    FOREIGN KEY (course_id) REFERENCES courses(id),
    UNIQUE (student_id, course_id)
);
```

---

## 17. API ENDPOINT SUMMARY

### Public
| Method | URL | Role |
|---|---|---|
| GET | `/login` | Public |
| GET | `/register` | Public |
| POST | `/register` | Public |
| GET | `/access-denied` | Public |

### Admin
| Method | URL |
|---|---|
| GET | `/admin/dashboard` |
| GET | `/admin/students?keyword=` |
| GET | `/admin/teachers?keyword=` |
| GET/POST | `/admin/users/create` |
| GET | `/admin/users/{id}` |
| GET/POST | `/admin/users/{id}/edit` |
| POST | `/admin/users/{id}/delete` |
| GET | `/admin/courses?keyword=` |
| GET | `/admin/courses/{id}` |
| POST | `/admin/courses/{id}/status` |
| POST | `/admin/courses/{id}/delete` |

### Teacher
| Method | URL |
|---|---|
| GET | `/teacher/dashboard` |
| GET/POST | `/teacher/courses/create` |
| GET | `/teacher/courses?keyword=` |
| GET | `/teacher/courses/{id}` |
| GET/POST | `/teacher/courses/{id}/edit` |
| POST | `/teacher/courses/{id}/delete` |
| GET/POST | `/teacher/courses/{courseId}/lessons/create` |
| GET/POST | `/teacher/courses/{courseId}/lessons/{lessonId}/edit` |
| POST | `/teacher/courses/{courseId}/lessons/{lessonId}/delete` |
| GET | `/teacher/students?keyword=` |
| GET/POST | `/teacher/students/create` |
| GET/POST | `/teacher/students/{id}/edit` |
| POST | `/teacher/students/{id}/delete` |
| GET | `/teacher/courses/{courseId}/students` |
| POST | `/teacher/courses/{courseId}/students/add` |

### Student
| Method | URL |
|---|---|
| GET | `/student/dashboard` |
| GET | `/student/courses?keyword=` |
| GET | `/student/courses/{id}` |
| POST | `/student/courses/{id}/enroll` |
| POST | `/student/courses/{id}/cancel` |
| GET | `/student/my-courses` |
| GET | `/student/courses/{courseId}/lessons/{lessonId}` |

---

## 18. SECURITY RULES

| Pattern | Required Role |
|---|---|
| `/css/**`, `/js/**`, `/images/**`, `/webjars/**` | Public |
| `/login`, `/register`, `/login-error` | Public |
| `/admin/**` | ADMIN |
| `/teacher/**` | TEACHER |
| `/student/**` | STUDENT |
| `/profile/**`, `/change-password` | Authenticated (bất kỳ role nào) |
| Any other request | Authenticated |

---

## 19. TÀI KHOẢN DEMO (tạo tự động bởi DataInitializer)

| Username | Password | Role |
|---|---|---|
| admin | Admin@123 | ADMIN |
| teacher01 | Teacher@123 | TEACHER |
| student01 | Student@123 | STUDENT |

---

## 20. TEST (JUnit 5 + Mockito, H2 database)

| Test class | Số lượng test |
|---|---|
| `UserServiceTest.java` | ~18 tests |
| `CourseServiceTest.java` | ~12 tests |
| `EnrollmentServiceTest.java` | ~12 tests |
| `LessonServiceTest.java` | ~8 tests |

**Tổng cộng:** ~50 unit tests, sử dụng Mockito để mock repository layer.

---

## 21. LUỒNG CHÍNH

### Đăng ký → Học tập:
```
Register → Login → Student Dashboard → Explore Courses → Enroll → View Lessons
```

### Tạo khóa học → Giảng dạy:
```
Login (Teacher) → Dashboard → Create Course → Add Lessons → Manage Students
```

### Quản trị hệ thống:
```
Login (Admin) → Dashboard → Manage Users (CRUD) → Manage Courses (Status/Delete)
```

---

*File này được tạo tự động bởi AI — bao gồm toàn bộ định nghĩa project E-Learning Management System.*
