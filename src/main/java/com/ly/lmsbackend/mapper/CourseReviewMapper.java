package com.ly.lmsbackend.mapper;

import com.ly.lmsbackend.dto.coursereviewdtos.CourseReviewCreateDTO;
import com.ly.lmsbackend.dto.coursereviewdtos.CourseReviewResponseDTO;
import com.ly.lmsbackend.model.CourseReviews;
import com.ly.lmsbackend.model.Courses;
import com.ly.lmsbackend.model.Users;
import org.springframework.stereotype.Component;

@Component
public class CourseReviewMapper {

    public CourseReviewResponseDTO toDto(CourseReviews review) {
        return new CourseReviewResponseDTO(
                review.getReviewId(),
                review.getReviewText(),
                review.getRating(),
                review.getUser().getUsername(),
                review.getCourse().getTitle()
        );
    }

    public CourseReviews toEntity(CourseReviewCreateDTO dto, Users user, Courses course) {
        CourseReviews review = new CourseReviews();

        review.setReviewText(dto.reviewText());
        review.setRating(dto.rating());
        review.setUser(user);
        review.setCourse(course);

        return review;
    }
}
