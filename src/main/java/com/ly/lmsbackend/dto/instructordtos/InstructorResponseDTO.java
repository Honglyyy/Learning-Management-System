package com.ly.lmsbackend.dto.instructordtos;

import com.ly.lmsbackend.dto.coursedtos.CourseResponseDTO;
import lombok.Builder;

import java.sql.Timestamp;
import java.util.List;

@Builder
public record InstructorResponseDTO(
        Long instructorId,
        Long userId,
        String username,
        String email,
        String fullName,
        String phoneNumber,
        String profilePhotoUrl,
        String profilePhotoPublicId,
        String biography,
        String expertise,
        Double averageRating,
        Integer totalCourses,
        List<CourseResponseDTO> courses,
        Timestamp createdAt,
        Timestamp updatedAt
) {
}
