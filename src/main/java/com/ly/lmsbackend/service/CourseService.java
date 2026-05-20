package com.ly.lmsbackend.service;

import com.ly.lmsbackend.dto.*;
import com.ly.lmsbackend.mapper.CourseMapper;
import com.ly.lmsbackend.mapper.SectionMapper;
import com.ly.lmsbackend.model.Categories;
import com.ly.lmsbackend.model.Courses;
import com.ly.lmsbackend.model.Users;
import com.ly.lmsbackend.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final SectionRepository sectionRepository;
    private final SectionMapper sectionMapper;
    private final CourseReviewRepository courseReviewRepository;

    public CourseService(CourseRepository courseRepository, CourseMapper courseMapper, CategoryRepository categoryRepository, UserRepository userRepository, SectionRepository sectionRepository, SectionMapper sectionMapper, CourseReviewRepository courseReviewRepository) {
        this.courseRepository = courseRepository;
        this.categoryRepository = categoryRepository;
        this.courseMapper = courseMapper;
        this.userRepository = userRepository;
        this.sectionRepository = sectionRepository;
        this.sectionMapper = sectionMapper;
        this.courseReviewRepository = courseReviewRepository;
    }

    public List<CourseResponseDTO> getAllCourses(){
        return courseRepository.findAll()
                .stream()
                .map(courseMapper::toDTO)
                .toList();
    }

    public List<CourseResponseDTO> getCoursesByInstructor(String instructorEmail) {
        return courseRepository.findByInstructor_Email(instructorEmail)
                .stream()
                .map(courseMapper::toDTO)
                .toList();
    }


    public CourseResponseDTO addCourse(CourseCreateDTO dto){
        Users instructor = userRepository.findById(dto.instructor())
                .orElseThrow(() -> new RuntimeException("Instructor id " + dto.instructor() + " is now found!!"));

        List<Categories> categories = categoryRepository.findAllById(dto.categoryId());

        Courses course = courseMapper.toEntity(dto,instructor,categories);

        return courseMapper.toDTO(courseRepository.save(course));
    }

    public CourseResponseDTO addCourseForInstructor(CourseCreateDTO dto, String instructorEmail) {
        Users instructor = userRepository.findByEmail(instructorEmail)
                .orElseThrow(() -> new RuntimeException("Instructor not found"));

        List<Categories> categories = categoryRepository.findAllById(dto.categoryId());
        Courses course = courseMapper.toEntity(dto, instructor, categories);

        return courseMapper.toDTO(courseRepository.save(course));
    }

    public void deleteCourse(Long id){
        courseRepository.deleteById(id);
    }

    public CourseResponseDTO updateCourse(Long id, CourseCreateDTO dto){
        Courses existingCourse = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course id" + id + " not found"));

        Users instructor = userRepository.findById(dto.instructor())
                .orElseThrow(() -> new RuntimeException("Instructor not found"));

        List<Categories> categories = categoryRepository.findAllById(dto.categoryId());

        existingCourse.setTitle(dto.title());
        existingCourse.setDescription(dto.description());
        existingCourse.setPrice(dto.price());
        existingCourse.setOverallDuration(dto.overallDuration());
        existingCourse.setCoverDir(dto.coverDir());
        existingCourse.setInstructor(instructor);
        existingCourse.setCategories(categories);

        return courseMapper.toDTO(courseRepository.save(existingCourse));
    }

    public CourseDetailDTO getCourseDetail(Long courseId) {
        Courses course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course id " + courseId + " not found"));

        List<SectionDetailDTO> sectionsDetail = sectionRepository.findByCourse_CourseId(courseId)
                .stream()
                .map(sections -> new SectionDetailDTO(
                    sections.getSectionId(),
                    sections.getTitle(),
                    sections.getDuration(),
                    (long) sections.getLessons().size(),
                    sections.getLessons().stream()
                            .map(lessons -> new LessonDetailDTO(
                                    lessons.getLessonId(),
                                    lessons.getTitle(),
                                    lessons.getVideoDir()
                            ))
                            .toList()
                ))
                .toList();

        List<String> categories = course.getCategories()
                .stream()
                .map(Categories::getCategory)
                .toList();


        List<CourseReviewResponseDTO> reviews = courseReviewRepository.findByCourse_CourseId(courseId)
                .stream()
                .map(review -> new CourseReviewResponseDTO(
                        review.getReviewId(),
                        review.getReviewText(),
                        review.getRating(),
                        review.getUser().getUsername(),
                        review.getCourse().getTitle()
                ))
                .toList();

        Double rating = courseReviewRepository.findByCourse_CourseId(course.getCourseId())
                .stream()
                .mapToDouble(rate -> rate.getRating())
                .average().orElse(5);


        return new CourseDetailDTO(
                course.getCourseId(),
                course.getTitle(),
                course.getDescription(),
                course.getPrice(),
                course.getOverallDuration(),
                course.getCoverDir(),
                course.getInstructor().getUsername(),
                (long) course.getSections().size(),
                rating,
                categories,
                sectionsDetail,
                reviews
        );
    }
}
