package com.ly.lmsbackend.repository;

import com.ly.lmsbackend.model.PaymentStatus;
import com.ly.lmsbackend.model.Payments;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payments, Long> {
    List<Payments> findByUser_Email(String email);
    Optional<Payments> findFirstByUser_EmailAndCourse_CourseIdAndStatusOrderByCreatedAtDesc(
            String email,
            Long courseId,
            PaymentStatus status
    );
}
