package com.ly.lmsbackend.repository;

import com.ly.lmsbackend.model.Students;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Students, Long> {
    Optional<Students> findByUser_Id(Long userId);
    Optional<Students> findByUser_Email(String email);
    Optional<Students> findByStudentCode(String studentCode);
    boolean existsByStudentCode(String studentCode);
    void deleteByUser_Id(Long userId);
}
