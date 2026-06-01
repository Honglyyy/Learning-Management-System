package com.ly.lmsbackend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
import java.util.List;

@Component
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString

@EntityListeners(AuditingEntityListener.class)
public class Categories {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryId;

    @Column(unique = true,  nullable = false)
    private String category;

    @CreatedBy
    private String createdBy;

    @CreatedDate
    private Timestamp createdAt;

    @LastModifiedBy
    private String updatedBy;

    @LastModifiedDate
    private Timestamp updatedAt;

    @ManyToMany(mappedBy = "categories")
    @JsonIgnore
    private List<Courses> courses;
}
