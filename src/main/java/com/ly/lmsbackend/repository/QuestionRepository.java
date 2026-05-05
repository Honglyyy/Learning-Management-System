package com.ly.lmsbackend.repository;

import com.ly.lmsbackend.model.Questions;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Questions, Long> {
}
