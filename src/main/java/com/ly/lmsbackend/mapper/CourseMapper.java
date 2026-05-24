package com.ly.lmsbackend.mapper;

import com.ly.lmsbackend.dto.CourseCreateDTO;
import com.ly.lmsbackend.dto.CourseResponseDTO;
import com.ly.lmsbackend.model.Categories;
import com.ly.lmsbackend.model.Courses;
import com.ly.lmsbackend.model.Users;
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
        course.setCoverDir(dto.coverDir());
        course.setInstructor(instructor);
        course.setCategories(categories);

        return course;
    }

    public CourseResponseDTO toDTO(
            Courses course
    ){
        List<String> coursesName = course.getCategories()
                .stream()
                .map(Categories::getCategory)
                .toList();
        List<Long> categoryIds = course.getCategories()
                .stream()
                .map(Categories::getCategoryId)
                .toList();

        Double rating = courseReviewRepository.findByCourse_CourseId(course.getCourseId())
                .stream()
                .mapToDouble(rate -> rate.getRating())
                .average().orElse(0.0);
        return new CourseResponseDTO(
                course.getCourseId(),
                course.getTitle(),
                course.getDescription(),
                course.getPrice(),
                course.getOverallDuration(),
                course.getCoverDir(),
                course.getInstructor().getId(),
                course.getInstructor().getUsername(),
                categoryIds,
                coursesName,
                rating
        );
    }
}
