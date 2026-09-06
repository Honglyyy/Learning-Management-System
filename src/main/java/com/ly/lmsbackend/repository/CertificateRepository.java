package com.ly.lmsbackend.repository;

import com.ly.lmsbackend.model.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Long> {

    List<Certificate> findByStudent_IdOrderByIssuedAtDesc(Long userId);

    List<Certificate> findByStudent_EmailOrderByIssuedAtDesc(String email);

    Optional<Certificate> findByCertificateCode(String certificateCode);

    Optional<Certificate> findByStudent_IdAndCourse_CourseId(Long userId, Long courseId);

    Optional<Certificate> findByStudent_EmailAndCourse_CourseId(String email, Long courseId);

    boolean existsByStudent_IdAndCourse_CourseId(Long userId, Long courseId);

    boolean existsByCertificateCode(String certificateCode);
}
