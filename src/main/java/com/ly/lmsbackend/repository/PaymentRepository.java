package com.ly.lmsbackend.repository;

import com.ly.lmsbackend.model.PaymentStatus;
import com.ly.lmsbackend.model.Payments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payments, Long> {
    List<Payments> findByUser_Email(String email);
    List<Payments> findByStatus(PaymentStatus status);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payments p WHERE p.status = :status")
    java.math.BigDecimal sumAmountByStatus(@Param("status") PaymentStatus status);

    Optional<Payments> findFirstByUser_EmailAndCourse_CourseIdAndStatusOrderByCreatedAtDesc(
            String email,
            Long courseId,
            PaymentStatus status
    );

    @Modifying
    @Query("""
            delete from Payments p
            where p.user.id = :userId
               or p.course.instructor.id = :userId
            """)
    void deleteAllForUserRemoval(@Param("userId") Long userId);
}
