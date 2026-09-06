package com.ly.lmsbackend.repository;

import com.ly.lmsbackend.model.Instructors;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InstructorRepository extends JpaRepository<Instructors, Long> {
    Optional<Instructors> findByUser_Id(Long userId);
    Optional<Instructors> findByUser_Email(String email);
    void deleteByUser_Id(Long userId);
}
