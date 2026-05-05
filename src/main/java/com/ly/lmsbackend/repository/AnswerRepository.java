package com.ly.lmsbackend.repository;

import com.ly.lmsbackend.model.Answers;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnswerRepository extends JpaRepository<Answers, Long> {
}