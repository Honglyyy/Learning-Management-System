package com.ly.lmsbackend.repository;

import com.ly.lmsbackend.model.Sections;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SectionRepository extends JpaRepository<Sections, Long> {
    List<Sections> findByCourse_CourseId(Long courseId);
}
