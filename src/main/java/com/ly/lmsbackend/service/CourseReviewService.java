package com.ly.lmsbackend.service;

import com.ly.lmsbackend.dto.CourseReviewCreateDTO;
import com.ly.lmsbackend.dto.CourseReviewResponseDTO;
import com.ly.lmsbackend.mapper.CourseReviewMapper;
import com.ly.lmsbackend.model.CourseReviews;
import com.ly.lmsbackend.model.Courses;
import com.ly.lmsbackend.model.Users;
import com.ly.lmsbackend.repository.CourseRepository;
import com.ly.lmsbackend.repository.CourseReviewRepository;
import com.ly.lmsbackend.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class CourseReviewService {


    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final CourseReviewMapper courseReviewMapper;
    private final CourseReviewRepository courseReviewRepository;

    public CourseReviewService(UserRepository userRepository, CourseRepository courseRepository, CourseReviewMapper courseReviewMapper, CourseReviewRepository courseReviewRepository) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.courseReviewMapper = courseReviewMapper;
        this.courseReviewRepository = courseReviewRepository;
    }

    public CourseReviewResponseDTO addReview(CourseReviewCreateDTO dto){
        Users userId = userRepository.findById(dto.userId())
                .orElseThrow(() -> new RuntimeException("User id " + dto.userId() + " is not found!!"));

        Courses courseId = courseRepository.findById(dto.courseId())
                .orElseThrow(() -> new RuntimeException("Course id " + dto.courseId() + " is not found!!"));

        CourseReviews review = courseReviewMapper.toEntity(dto, userId, courseId);

        return courseReviewMapper.toDto(courseReviewRepository.save(review));
    }

}
