package com.elearning.repository;

import com.elearning.model.Course;
import com.elearning.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    // Tìm khóa học của 1 giáo viên
    List<Course> findByTeacher(User teacher);

    List<Course> findByTeacherId(Long teacherId);

    // Tìm khóa học theo trạng thái
    List<Course> findByStatus(Course.Status status);

    // Tìm kiếm khóa học theo tiêu đề hoặc mô tả
    @Query("SELECT c FROM Course c WHERE c.status = 'ACTIVE' AND " +
           "(LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Course> searchActiveCoursesByKeyword(@Param("keyword") String keyword);

    // Admin tìm tất cả khóa học (kể cả inactive)
    @Query("SELECT c FROM Course c WHERE " +
           "LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Course> searchAllByKeyword(@Param("keyword") String keyword);

    // Tìm khóa học theo giáo viên + keyword
    @Query("SELECT c FROM Course c WHERE c.teacher = :teacher AND " +
           "(LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Course> searchByTeacherAndKeyword(@Param("teacher") User teacher, @Param("keyword") String keyword);

    // Tìm khóa học mà học sinh CHƯA đăng ký
    @Query("SELECT c FROM Course c WHERE c.status = 'ACTIVE' AND c.id NOT IN " +
           "(SELECT e.course.id FROM Enrollment e WHERE e.student.id = :studentId AND e.status != 'CANCELLED')")
    List<Course> findAvailableCoursesForStudent(@Param("studentId") Long studentId);

    // Lấy khóa học kèm thông tin giáo viên (fetch join để tránh N+1)
    @Query("SELECT c FROM Course c JOIN FETCH c.teacher WHERE c.status = 'ACTIVE'")
    List<Course> findAllActiveCoursesWithTeacher();

    // Tìm theo tên giáo viên (cho student tìm kiếm)
    @Query("SELECT c FROM Course c JOIN c.teacher t WHERE c.status = 'ACTIVE' AND " +
           "(LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(t.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Course> searchByTitleOrTeacherName(@Param("keyword") String keyword);
}
