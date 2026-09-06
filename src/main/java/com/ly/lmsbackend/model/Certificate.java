package com.ly.lmsbackend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Entity
@Table(
        name = "certificates",
        uniqueConstraints = @UniqueConstraint(columnNames = "certificate_code")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "certificate_id")
    private Long certificateId;

    @Column(name = "certificate_code", nullable = false, unique = true)
    private String certificateCode; // e.g. DA-2026-03-4782 or LMS-2026-XXXXX

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private Users student;

    @ManyToOne(optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Courses course;

    @CreationTimestamp
    @Column(name = "issued_at", nullable = false, updatable = false)
    private Timestamp issuedAt;

    @Column(name = "certificate_url")
    private String certificateUrl;

    @Column(name = "final_score")
    private Double finalScore;
}
