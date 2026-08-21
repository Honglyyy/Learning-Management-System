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

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EntityListeners(AuditingEntityListener.class)
public class Lessons {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lesson_id")
    private Long lessonId;

    @Column(name = "lesson_title")
    private String title;

    @Column(name = "video_url")
    private String videoUrl;

    @Column(name = "video_public_id")
    private String videoPublicId;

    @CreatedBy
    private String createdBy;

    @CreatedDate
    private Timestamp createdAt;

    @LastModifiedBy
    private String updatedBy;

    @LastModifiedDate
    private Timestamp updatedAt;


    @ManyToOne
    @JoinColumn(name = "section_id")
    private Sections section;


    @OneToOne(mappedBy = "lesson",cascade = CascadeType.ALL, orphanRemoval = true)
    private Quizzes quiz;

    @ManyToOne
    @JoinColumn(name = "instructor_id")
    private Users instructor;
}
