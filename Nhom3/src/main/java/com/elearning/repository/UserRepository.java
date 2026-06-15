package com.elearning.repository;

import com.elearning.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<User> findByRole(User.Role role);

    // Tìm kiếm theo tên hoặc username hoặc email (không phân biệt hoa thường)
    @Query("SELECT u FROM User u WHERE u.role = :role AND " +
           "(LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<User> searchByRoleAndKeyword(@Param("role") User.Role role, @Param("keyword") String keyword);

    // Tìm kiếm chung (Admin - tìm tất cả roles)
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<User> searchByKeyword(@Param("keyword") String keyword);

    // Thống kê số lượng theo role
    long countByRole(User.Role role);

    @Modifying
    @Query("DELETE FROM User u WHERE u.id = :id")
    void deleteUserById(@Param("id") Long id);
}
