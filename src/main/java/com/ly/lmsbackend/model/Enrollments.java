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
        name = "enrollments",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_enrollment_user_course",
                columnNames = {"user_id", "course_id"}
        )
)
public class Enrollments {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "enrollment_id")
    private Long enrollmentId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Courses course;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnrollmentStatus status = EnrollmentStatus.ACTIVE;

    @Column(name = "earned_points")
    private Double earnedPoints = 0.0;

    @Column(name = "total_points")
    private Double totalPoints = 0.0;

    @Column(name = "expiration_date")
    private Timestamp expirationDate;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Timestamp enrolledAt;

    @UpdateTimestamp
    private Timestamp updatedAt;

    public boolean isExpired() {
        if (this.status == EnrollmentStatus.EXPIRED) {
            return true;
        }
        return this.expirationDate != null && this.expirationDate.before(new Timestamp(System.currentTimeMillis()));
    }

    public boolean isActive() {
        return this.status == EnrollmentStatus.ACTIVE && !isExpired();
    }
}
