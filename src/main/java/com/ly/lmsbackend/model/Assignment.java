package com.ly.lmsbackend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "assignments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Assignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "assignment_id")
    private Long assignmentId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Courses course;

    @ManyToOne
    @JoinColumn(name = "section_id")
    private Sections section;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String instructions;

    @Column(name = "start_date")
    private Timestamp startDate;

    @Column(name = "due_date")
    private Timestamp dueDate;

    @Builder.Default
    @Column(name = "max_score", nullable = false)
    private Double maxScore = 100.0;

    @Column(name = "supporting_file_url")
    private String supportingFileUrl;

    @Column(name = "supporting_file_public_id")
    private String supportingFilePublicId;

    @Builder.Default
    @Column(name = "allow_resubmission", nullable = false)
    private Boolean allowResubmission = true;

    @ManyToOne
    @JoinColumn(name = "instructor_id")
    private Users instructor;

    @Builder.Default
    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AssignmentSubmission> submissions = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Timestamp createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Timestamp updatedAt;
}
