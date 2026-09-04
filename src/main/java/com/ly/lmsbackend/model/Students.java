package com.ly.lmsbackend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "students")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Students {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "id", unique = true, nullable = false)
    private Users user;

    @Column(name = "student_code", unique = true)
    private String studentCode;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private Genders gender;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "education_level")
    private String educationLevel; // High School, Bachelor, Master, etc.

    @Column(name = "profile_photo_url")
    private String profilePhotoUrl;

    @Column(name = "profile_photo_public_id")
    private String profilePhotoPublicId;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL)
    private List<Enrollments> enrollments;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL)
    private List<QuizAttempt> quizAttempts;

//    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL)
//    private List<AssignmentSubmission> assignmentSubmissions;

    @CreationTimestamp
    private Timestamp createdAt;

    @UpdateTimestamp
    private Timestamp updatedAt;
}
