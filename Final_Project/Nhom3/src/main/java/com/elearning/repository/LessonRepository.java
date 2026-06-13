package com.elearning.repository;

import com.elearning.model.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {

    List<Lesson> findByCourseIdOrderByOrderIndexAsc(Long courseId);

    List<Lesson> findByCourseIdAndStatusOrderByOrderIndexAsc(Long courseId, Lesson.Status status);

    // Lấy orderIndex lớn nhất trong course để tự động đặt thứ tự cho bài mới
    @Query("SELECT COALESCE(MAX(l.orderIndex), 0) FROM Lesson l WHERE l.course.id = :courseId")
    Integer findMaxOrderIndexByCourseId(@Param("courseId") Long courseId);

    long countByCourseId(Long courseId);

    // Kiểm tra lesson có thuộc course không (bảo mật)
    Optional<Lesson> findByIdAndCourseId(Long id, Long courseId);
}
