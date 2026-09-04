package com.ly.lmsbackend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;
import java.util.List;

@Entity
@Table(name = "instructors")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Instructors {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "id", unique = true, nullable = false)
    private Users user;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "profile_photo_url")
    private String profilePhotoUrl;

    @Column(name = "profile_photo_public_id")
    private String profilePhotoPublicId;

    @Column(columnDefinition = "TEXT")
    private String biography;

    private String expertise; // e.g. "Spring Boot, Cloud Architecture"

    @Column(name = "average_rating")
    private Double averageRating = 5.0;

    @OneToMany(mappedBy = "instructor", cascade = CascadeType.ALL)
    private List<Courses> courses;

    @CreationTimestamp
    private Timestamp createdAt;

    @UpdateTimestamp
    private Timestamp updatedAt;
}