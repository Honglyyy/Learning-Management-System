package com.ly.lmsbackend.repository;

import com.ly.lmsbackend.model.Lessons;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LessonRepository extends JpaRepository<Lessons, Long> {
    List<Lessons> findByInstructor_Email(String email);
}
