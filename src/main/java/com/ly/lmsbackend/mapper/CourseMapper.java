package com.ly.lmsbackend.mapper;

import com.ly.lmsbackend.dto.coursedtos.CourseCreateDTO;
import com.ly.lmsbackend.dto.coursedtos.CourseResponseDTO;
import com.ly.lmsbackend.model.Categories;
import com.ly.lmsbackend.model.Courses;
import com.ly.lmsbackend.model.Users;
import com.ly.lmsbackend.model.CourseLevel;
import com.ly.lmsbackend.model.CourseStatus;
import com.ly.lmsbackend.repository.CourseReviewRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CourseMapper {

    private final CourseReviewRepository courseReviewRepository;

    public CourseMapper(CourseReviewRepository courseReviewRepository) {
        this.courseReviewRepository = courseReviewRepository;
    }

    public Courses toEntity(
            CourseCreateDTO dto,
            Users instructor,
            List<Categories> categories
    ){
        Courses course = new Courses();

        course.setTitle(dto.title());
        course.setDescription(dto.description());
        course.setPrice(dto.price());
        course.setOverallDuration(dto.overallDuration());
        course.setCoverUrl(dto.coverUrl());
        course.setCoverPublicId(dto.coverPublicId());
        course.setInstructor(instructor);
        course.setCategories(categories);

        course.setLevel(dto.level() != null ? dto.level() : CourseLevel.ALL_LEVELS);
        course.setStatus(dto.status() != null ? dto.status() : CourseStatus.PUBLISHED);
        course.setLearningOutcomes(dto.learningOutcomes());
        course.setRequirements(dto.requirements());

        return course;
    }

    public CourseResponseDTO toDTO(Courses course) {
        return toDTO(course, false);
    }

    public CourseResponseDTO toDTO(Courses course, Boolean isFavorite) {
        List<String> coursesName = course.getCategories() != null
                ? course.getCategories().stream().map(Categories::getCategory).toList()
                : List.of();
        List<Long> categoryIds = course.getCategories() != null
                ? course.getCategories().stream().map(Categories::getCategoryId).toList()
                : List.of();

        Double rating = 0.0;
        if (course.getCourseId() != null) {
            rating = courseReviewRepository.findByCourse_CourseId(course.getCourseId())
                    .stream()
                    .mapToDouble(rate -> rate.getRating())
                    .average().orElse(0.0);
        }

        Long instructorId = course.getInstructor() != null ? course.getInstructor().getId() : null;
        String instructorName = course.getInstructor() != null ? course.getInstructor().getUsername() : null;

        long lessonCount = 0L;
        if (course.getSections() != null) {
            lessonCount = course.getSections().stream()
                    .filter(s -> s.getLessons() != null)
                    .mapToLong(s -> s.getLessons().size())
                    .sum();
        }

        long enrollmentCount = (course.getEnrollments() != null) ? course.getEnrollments().size() : 0L;

        return new CourseResponseDTO(
                course.getCourseId(),
                course.getTitle(),
                course.getDescription(),
                course.getPrice(),
                course.getOverallDuration(),
                course.getCoverUrl(),
                course.getCoverPublicId(),
                instructorId,
                instructorName,
                categoryIds,
                coursesName,
                rating,
                course.getLevel() != null ? course.getLevel() : CourseLevel.ALL_LEVELS,
                course.getStatus() != null ? course.getStatus() : CourseStatus.PUBLISHED,
                lessonCount,
                enrollmentCount,
                isFavorite != null ? isFavorite : false
        );
    }
}

