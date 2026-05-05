package com.ly.lmsbackend.repository;

import com.ly.lmsbackend.model.Lessons;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonRepository extends JpaRepository<Lessons, Long> {
}
