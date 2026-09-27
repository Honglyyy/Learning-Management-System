package com.ly.lmsbackend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Component
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EntityListeners(AuditingEntityListener.class)
public class Courses {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_id")
    private Long courseId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(name = "overall_duration")
    private String overallDuration;

    @Column(name = "cover_url")
    private String coverUrl;

    @Column(name = "cover_public_id")
    private String coverPublicId;

    private BigDecimal price;

    @Column(name = "access_duration_days")
    private Integer accessDurationDays = 180;

    @CreatedBy
    private String createdBy;

    @CreatedDate
    private Timestamp createdAt;

    @LastModifiedBy
    private String updatedBy;

    @LastModifiedDate
    private Timestamp updatedAt;


    @ManyToMany
    @JoinTable(
            name = "course_category",
            joinColumns = @JoinColumn(name = "course_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private List<Categories> categories;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Sections> sections;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CourseReviews> reviews;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Enrollments> enrollments;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Payments> payments;

    @Enumerated(EnumType.STRING)
    @org.hibernate.annotations.ColumnDefault("'ALL_LEVELS'")
    private CourseLevel level = CourseLevel.ALL_LEVELS;

    @Enumerated(EnumType.STRING)
    @org.hibernate.annotations.ColumnDefault("'PUBLISHED'")
    private CourseStatus status = CourseStatus.PUBLISHED;

    @Column(name = "learning_outcomes", columnDefinition = "TEXT")
    private String learningOutcomes;

    @Column(name = "requirements", columnDefinition = "TEXT")
    private String requirements;

    @ManyToOne
    @JoinColumn(name = "instructor_id")
    private Users instructor;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CourseFavorite> favorites;

    public CourseLevel getLevel() {
        return level != null ? level : CourseLevel.ALL_LEVELS;
    }

    public CourseStatus getStatus() {
        return status != null ? status : CourseStatus.PUBLISHED;
    }

    public Integer getAccessDurationDays() {
        return accessDurationDays != null && accessDurationDays > 0 ? accessDurationDays : 180;
    }

    @PrePersist
    @PreUpdate
    public void ensureStage4Defaults() {
        if (this.level == null) {
            this.level = CourseLevel.ALL_LEVELS;
        }
        if (this.status == null) {
            this.status = CourseStatus.PUBLISHED;
        }
        if (this.accessDurationDays == null || this.accessDurationDays <= 0) {
            this.accessDurationDays = 180;
        }
    }
}
