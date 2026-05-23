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
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

import java.util.List;

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

//    public CourseReviewResponseDTO addReview(CourseReviewCreateDTO dto,String email){
//        Users userEmail = userRepository.findByEmail(email)
//                .orElseThrow(() -> new RuntimeException("User is not found!!"));
//
//        Courses courseId = courseRepository.findById(dto.courseId())
//                .orElseThrow(() -> new RuntimeException("Course id " + dto.courseId() + " is not found!!"));
//
//        CourseReviews review = courseReviewMapper.toEntity(dto, userEmail, courseId);
//
//        return courseReviewMapper.toDto(courseReviewRepository.save(review));
//    }

    public List<CourseReviewResponseDTO> getAllReviews() {
        return courseReviewRepository.findAll().stream().map(cr->
                new CourseReviewResponseDTO(
                        cr.getReviewId(),
                        cr.getReviewText(),
                        cr.getRating(),
                        cr.getUser().getUsername(),
                        cr.getCourse().getTitle()
                )

        ).toList();
    }

    public CourseReviewResponseDTO addReviewByCourse(
            Long courseId,
            CourseReviewCreateDTO dto,
            String email
    ) {
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Courses course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        CourseReviews review = courseReviewMapper.toEntity(dto, user, course);

        return courseReviewMapper.toDto(courseReviewRepository.save(review));
    }
    public List<CourseReviewResponseDTO> getByCourse(Long courseId) {
        return courseReviewRepository.findByCourseCourseId(courseId)
                .stream()
                .map(courseReviewMapper::toDto)
                .toList();
    }
}
