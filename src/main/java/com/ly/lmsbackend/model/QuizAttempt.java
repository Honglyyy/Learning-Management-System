package com.ly.lmsbackend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "quiz_attempts",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_quiz_attempt_enrollment_quiz",
                columnNames = {"enrollment_id", "quiz_id"}
        )
)
public class QuizAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attempt_id")
    private Long attemptId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private Enrollments enrollment;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quizzes quiz;

    @Column(nullable = false)
    private Double earnedPoints = 0.0;

    @Column(nullable = false)
    private Double totalPoints = 0.0;

    @Column(nullable = false)
    private Integer correctAnswers = 0;

    @Column(nullable = false)
    private Integer totalQuestions = 0;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Timestamp submittedAt;

    @UpdateTimestamp
    private Timestamp updatedAt;
}
