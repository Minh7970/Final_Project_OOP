package com.elearning.repository;

import com.elearning.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    // Kiểm tra học sinh đã đăng ký khóa học chưa
    boolean existsByStudentIdAndCourseIdAndStatusNot(Long studentId, Long courseId, Enrollment.Status status);

    Optional<Enrollment> findByStudentIdAndCourseId(Long studentId, Long courseId);

    // Lấy danh sách khóa học học sinh đã đăng ký
    @Query("SELECT e FROM Enrollment e JOIN FETCH e.course c JOIN FETCH c.teacher WHERE e.student.id = :studentId AND e.status != 'CANCELLED'")
    List<Enrollment> findActiveEnrollmentsByStudentId(@Param("studentId") Long studentId);

    // Lấy danh sách học sinh đã đăng ký khóa học (cho teacher xem)
    @Query("SELECT e FROM Enrollment e JOIN FETCH e.student WHERE e.course.id = :courseId AND e.status != 'CANCELLED'")
    List<Enrollment> findActiveEnrollmentsByCourseId(@Param("courseId") Long courseId);

    // Đếm số học sinh của 1 giáo viên (qua các khóa học)
    @Query("SELECT COUNT(DISTINCT e.student.id) FROM Enrollment e WHERE e.course.teacher.id = :teacherId AND e.status != 'CANCELLED'")
    long countDistinctStudentsByTeacherId(@Param("teacherId") Long teacherId);

    long countByCourseIdAndStatus(Long courseId, Enrollment.Status status);
}
