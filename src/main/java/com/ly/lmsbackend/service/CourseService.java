package com.ly.lmsbackend.service;

import com.ly.lmsbackend.dto.coursedtos.CourseCreateDTO;
import com.ly.lmsbackend.dto.coursedtos.CourseDetailDTO;
import com.ly.lmsbackend.dto.coursedtos.CourseResponseDTO;
import com.ly.lmsbackend.dto.coursedtos.MyCoursesSummaryDTO;
import com.ly.lmsbackend.dto.coursereviewdtos.CourseReviewResponseDTO;
import com.ly.lmsbackend.dto.lessondtos.LessonDetailDTO;
import com.ly.lmsbackend.dto.sectiondtos.SectionDetailDTO;
import com.ly.lmsbackend.mapper.CourseMapper;
import com.ly.lmsbackend.mapper.SectionMapper;
import com.ly.lmsbackend.model.*;
import com.ly.lmsbackend.repository.*;
import com.ly.lmsbackend.repository.specification.CourseSpecification;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final SectionRepository sectionRepository;
    private final SectionMapper sectionMapper;
    private final CourseReviewRepository courseReviewRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final CourseFavoriteRepository courseFavoriteRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final FileUploadService fileUploadService;

    public CourseService(
            CourseRepository courseRepository,
            CourseMapper courseMapper,
            CategoryRepository categoryRepository,
            UserRepository userRepository,
            SectionRepository sectionRepository,
            SectionMapper sectionMapper,
            CourseReviewRepository courseReviewRepository,
            QuizAttemptRepository quizAttemptRepository,
            CourseFavoriteRepository courseFavoriteRepository,
            EnrollmentRepository enrollmentRepository
    ) {
        this(courseRepository, courseMapper, categoryRepository, userRepository, sectionRepository,
                sectionMapper, courseReviewRepository, quizAttemptRepository, courseFavoriteRepository,
                enrollmentRepository, null);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public CourseService(
            CourseRepository courseRepository,
            CourseMapper courseMapper,
            CategoryRepository categoryRepository,
            UserRepository userRepository,
            SectionRepository sectionRepository,
            SectionMapper sectionMapper,
            CourseReviewRepository courseReviewRepository,
            QuizAttemptRepository quizAttemptRepository,
            CourseFavoriteRepository courseFavoriteRepository,
            EnrollmentRepository enrollmentRepository,
            @org.springframework.beans.factory.annotation.Autowired(required = false) FileUploadService fileUploadService
    ) {
        this.courseRepository = courseRepository;
        this.categoryRepository = categoryRepository;
        this.courseMapper = courseMapper;
        this.userRepository = userRepository;
        this.sectionRepository = sectionRepository;
        this.sectionMapper = sectionMapper;
        this.courseReviewRepository = courseReviewRepository;
        this.quizAttemptRepository = quizAttemptRepository;
        this.courseFavoriteRepository = courseFavoriteRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.fileUploadService = fileUploadService;
    }

    public List<CourseResponseDTO> getAllCourses() {
        return searchAndFilterCourses(null, null, null, null, null, CourseStatus.PUBLISHED, "latest", null);
    }

    public List<CourseResponseDTO> searchAndFilterCourses(
            String query,
            Long categoryId,
            CourseLevel level,
            Double minRating,
            BigDecimal maxPrice,
            CourseStatus status,
            String sort,
            String userEmail
    ) {
        Specification<Courses> spec = CourseSpecification.filterCourses(
                query, categoryId, level, maxPrice, status
        );

        Sort dbSort = Sort.by(Sort.Direction.DESC, "courseId");
        if ("latest".equalsIgnoreCase(sort)) {
            dbSort = Sort.by(Sort.Direction.DESC, "courseId");
        } else if ("price_asc".equalsIgnoreCase(sort)) {
            dbSort = Sort.by(Sort.Direction.ASC, "price");
        } else if ("price_desc".equalsIgnoreCase(sort)) {
            dbSort = Sort.by(Sort.Direction.DESC, "price");
        }

        List<Courses> courses = courseRepository.findAll(spec, dbSort);

        Set<Long> userFavoriteCourseIds = (userEmail != null && !userEmail.isBlank())
                ? courseFavoriteRepository.findByUser_EmailOrderByCreatedAtDesc(userEmail)
                .stream()
                .map(f -> f.getCourse().getCourseId())
                .collect(Collectors.toSet())
                : Collections.emptySet();

        List<CourseResponseDTO> dtos = courses.stream()
                .map(c -> courseMapper.toDTO(c, userFavoriteCourseIds.contains(c.getCourseId())))
                .collect(Collectors.toList());

        // Min rating filter
        if (minRating != null && minRating > 0) {
            dtos = dtos.stream()
                    .filter(dto -> dto.rating() != null && dto.rating() >= minRating)
                    .collect(Collectors.toList());
        }

        // Popular or Rating sorting in-memory
        if ("popular".equalsIgnoreCase(sort)) {
            dtos.sort(Comparator.comparing(CourseResponseDTO::enrollmentCount, Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(CourseResponseDTO::rating, Comparator.nullsLast(Comparator.reverseOrder())));
        } else if ("rating".equalsIgnoreCase(sort)) {
            dtos.sort(Comparator.comparing(CourseResponseDTO::rating, Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(CourseResponseDTO::enrollmentCount, Comparator.nullsLast(Comparator.reverseOrder())));
        }

        return dtos;
    }

    public List<CourseResponseDTO> getFeaturedCourses(String userEmail) {
        List<CourseResponseDTO> allPublished = searchAndFilterCourses(
                null, null, null, null, null, CourseStatus.PUBLISHED, "rating", userEmail
        );
        return allPublished.stream().limit(6).toList();
    }

    public List<CourseResponseDTO> getPopularCourses(String userEmail) {
        List<CourseResponseDTO> allPublished = searchAndFilterCourses(
                null, null, null, null, null, CourseStatus.PUBLISHED, "popular", userEmail
        );
        return allPublished.stream().limit(6).toList();
    }

    public List<CourseResponseDTO> getCoursesByInstructor(String instructorEmail) {
        return courseRepository.findByInstructor_Email(instructorEmail)
                .stream()
                .map(courseMapper::toDTO)
                .toList();
    }

    public CourseResponseDTO addCourse(CourseCreateDTO dto) {
        Users instructor = null;
        if (dto.instructor() != null) {
            instructor = userRepository.findById(dto.instructor()).orElse(null);
        }
        if (instructor == null && dto.instructorUsername() != null && !dto.instructorUsername().isBlank()) {
            instructor = userRepository.findByUsername(dto.instructorUsername()).orElse(null);
        }
        if (instructor == null) {
            String identifier = dto.instructor() != null ? "id " + dto.instructor() : dto.instructorUsername();
            throw new RuntimeException("Instructor " + identifier + " is not found!!");
        }

        List<Categories> categories = (dto.categoryId() != null && !dto.categoryId().isEmpty())
                ? categoryRepository.findAllById(dto.categoryId())
                : Collections.emptyList();

        Courses course = courseMapper.toEntity(dto, instructor, categories);
        organizeCourseCover(course);
        return courseMapper.toDTO(courseRepository.save(course));
    }

    public CourseResponseDTO addCourseForInstructor(CourseCreateDTO dto, String instructorEmail) {
        Users instructor = userRepository.findByEmail(instructorEmail)
                .orElseThrow(() -> new RuntimeException("Instructor not found"));

        List<Categories> categories = (dto.categoryId() != null && !dto.categoryId().isEmpty())
                ? categoryRepository.findAllById(dto.categoryId())
                : Collections.emptyList();

        Courses course = courseMapper.toEntity(dto, instructor, categories);
        organizeCourseCover(course);
        return courseMapper.toDTO(courseRepository.save(course));
    }

    @Transactional
    public void deleteCourse(Long id) {
        Courses course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        if (fileUploadService != null && course.getCoverPublicId() != null && !course.getCoverPublicId().isBlank()) {
            fileUploadService.deleteAsset(course.getCoverPublicId());
        }

        course.getCategories().clear();
        quizAttemptRepository.deleteAllByCourseId(id);
        courseRepository.delete(course);
    }

    public CourseResponseDTO updateCourse(Long id, CourseCreateDTO dto) {
        Courses existingCourse = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course id " + id + " not found"));

        Users newInstructor = null;
        if (dto.instructor() != null) {
            newInstructor = userRepository.findById(dto.instructor()).orElse(null);
        }
        if (newInstructor == null && dto.instructorUsername() != null && !dto.instructorUsername().isBlank()) {
            newInstructor = userRepository.findByUsername(dto.instructorUsername()).orElse(null);
        }
        if (newInstructor != null) {
            existingCourse.setInstructor(newInstructor);
        }

        if (dto.categoryId() != null) {
            List<Categories> categories = categoryRepository.findAllById(dto.categoryId());
            existingCourse.setCategories(categories);
        }

        existingCourse.setTitle(dto.title());
        existingCourse.setDescription(dto.description());
        existingCourse.setPrice(dto.price());
        existingCourse.setOverallDuration(dto.overallDuration());
        existingCourse.setCoverUrl(dto.coverUrl());
        existingCourse.setCoverPublicId(dto.coverPublicId());

        if (dto.level() != null) {
            existingCourse.setLevel(dto.level());
        }
        if (dto.status() != null) {
            existingCourse.setStatus(dto.status());
        }
        if (dto.learningOutcomes() != null) {
            existingCourse.setLearningOutcomes(dto.learningOutcomes());
        }
        if (dto.requirements() != null) {
            existingCourse.setRequirements(dto.requirements());
        }
        if (dto.accessDurationDays() != null && dto.accessDurationDays() > 0) {
            existingCourse.setAccessDurationDays(dto.accessDurationDays());
        }

        organizeCourseCover(existingCourse);
        return courseMapper.toDTO(courseRepository.save(existingCourse));
    }

    @Transactional
    public CourseResponseDTO updateCourseStatus(Long courseId, CourseStatus newStatus, String requesterEmail) {
        Courses course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));

        Users requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        boolean isAdmin = requester.getRole() == Roles.ADMIN;
        boolean isOwner = course.getInstructor() != null && course.getInstructor().getId().equals(requester.getId());

        if (!isAdmin && !isOwner) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized to update this course status");
        }

        course.setStatus(newStatus);
        Courses saved = courseRepository.save(course);
        return courseMapper.toDTO(saved);
    }

    public CourseDetailDTO getCourseDetail(Long courseId) {
        return getCourseDetail(courseId, null);
    }

    public CourseDetailDTO getCourseDetail(Long courseId, String userEmail) {
        Courses course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course id " + courseId + " not found"));

        boolean hasCourseAccess = false;
        boolean isEnrolled = false;
        boolean isExpired = false;
        java.sql.Timestamp expirationDate = null;
        boolean hasReEnrollmentDiscount = false;
        BigDecimal discountedPrice = null;

        if (userEmail != null && !userEmail.isBlank()) {
            Users user = userRepository.findByEmail(userEmail).orElse(null);
            if (user != null) {
                if (user.getRole() == Roles.ADMIN) {
                    hasCourseAccess = true;
                } else if (user.getRole() == Roles.INSTRUCTOR) {
                    if (course.getInstructor() != null && user.getEmail().equals(course.getInstructor().getEmail())) {
                        hasCourseAccess = true;
                    }
                } else if (user.getRole() == Roles.STUDENT || user.getRole() == Roles.USER) {
                    Optional<Enrollments> enrollmentOpt = enrollmentRepository.findByUser_IdAndCourse_CourseId(user.getId(), courseId);
                    if (enrollmentOpt.isPresent()) {
                        Enrollments enrollment = enrollmentOpt.get();
                        isEnrolled = true;
                        isExpired = enrollment.isExpired();
                        expirationDate = enrollment.getExpirationDate();
                        hasCourseAccess = enrollment.getStatus() == EnrollmentStatus.ACTIVE && !isExpired;

                        if (isExpired && course.getPrice() != null && course.getPrice().compareTo(BigDecimal.ZERO) > 0) {
                            hasReEnrollmentDiscount = true;
                            discountedPrice = course.getPrice().multiply(BigDecimal.valueOf(0.5)).setScale(2, java.math.RoundingMode.HALF_UP);
                        }
                    }
                }
            }
        }

        final boolean finalHasCourseAccess = hasCourseAccess;

        List<SectionDetailDTO> sectionsDetail = sectionRepository.findByCourse_CourseId(courseId)
                .stream()
                .map(sections -> new SectionDetailDTO(
                        sections.getSectionId(),
                        sections.getTitle(),
                        sections.getDuration(),
                        (long) (sections.getLessons() != null ? sections.getLessons().size() : 0),
                        (sections.getLessons() != null ? sections.getLessons().stream() : java.util.stream.Stream.<Lessons>empty())
                                .map(lessons -> {
                                    boolean canViewVideo = finalHasCourseAccess || Boolean.TRUE.equals(lessons.getIsFree());
                                    return new LessonDetailDTO(
                                            lessons.getLessonId(),
                                            lessons.getTitle(),
                                            canViewVideo ? lessons.getVideoUrl() : null,
                                            canViewVideo ? lessons.getVideoPublicId() : null,
                                            lessons.getDescription(),
                                            lessons.getTextContent(),
                                            lessons.getOrderIndex(),
                                            lessons.getDuration(),
                                            lessons.getIsFree() != null ? lessons.getIsFree() : false
                                    );
                                })
                                .toList()
                ))
                .toList();

        List<String> categories = course.getCategories() != null
                ? course.getCategories().stream().map(Categories::getCategory).toList()
                : Collections.emptyList();

        List<CourseReviewResponseDTO> reviews = courseReviewRepository.findByCourse_CourseId(courseId)
                .stream()
                .map(review -> new CourseReviewResponseDTO(
                        review.getReviewId(),
                        review.getReviewText(),
                        review.getRating(),
                        review.getUser() != null ? review.getUser().getUsername() : "Anonymous",
                        review.getCourse() != null ? review.getCourse().getTitle() : ""
                ))
                .toList();

        Double rating = courseReviewRepository.findByCourse_CourseId(course.getCourseId())
                .stream()
                .mapToDouble(CourseReviews::getRating)
                .average().orElse(5.0);

        long enrollmentCount = (course.getEnrollments() != null) ? course.getEnrollments().size() : 0L;
        boolean isFavorite = false;
        if (userEmail != null && !userEmail.isBlank()) {
            isFavorite = courseFavoriteRepository.existsByUser_EmailAndCourse_CourseId(userEmail, courseId);
        }

        String instructorName = course.getInstructor() != null ? course.getInstructor().getUsername() : "Unknown";
        long sectionCount = (course.getSections() != null) ? course.getSections().size() : 0L;

        return new CourseDetailDTO(
                course.getCourseId(),
                course.getTitle(),
                course.getDescription(),
                course.getPrice(),
                course.getOverallDuration(),
                course.getCoverUrl(),
                course.getCoverPublicId(),
                instructorName,
                sectionCount,
                rating,
                categories,
                sectionsDetail,
                reviews,
                course.getLevel() != null ? course.getLevel() : CourseLevel.ALL_LEVELS,
                course.getStatus() != null ? course.getStatus() : CourseStatus.PUBLISHED,
                course.getLearningOutcomes(),
                course.getRequirements(),
                enrollmentCount,
                isFavorite,
                course.getAccessDurationDays(),
                isEnrolled,
                isExpired,
                expirationDate,
                hasReEnrollmentDiscount,
                discountedPrice
        );
    }

    @Transactional
    public void deleteMyCourse(Long id, String instructorEmail) {
        Courses course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        if (!course.getInstructor().getEmail().equals(instructorEmail)) {
            throw new RuntimeException("Unauthorized");
        }

        if (fileUploadService != null && course.getCoverPublicId() != null && !course.getCoverPublicId().isBlank()) {
            fileUploadService.deleteAsset(course.getCoverPublicId());
        }

        course.getCategories().clear();
        quizAttemptRepository.deleteAllByCourseId(id);
        courseRepository.delete(course);
    }

    public CourseResponseDTO updateMyCourse(
            Long id,
            CourseCreateDTO dto,
            String instructorEmail
    ) {
        Courses course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        if (!course.getInstructor().getEmail().equals(instructorEmail)) {
            throw new RuntimeException("Unauthorized");
        }

        if (dto.categoryId() != null) {
            List<Categories> categories = categoryRepository.findAllById(dto.categoryId());
            course.setCategories(categories);
        }

        course.setTitle(dto.title());
        course.setDescription(dto.description());
        course.setPrice(dto.price());
        course.setOverallDuration(dto.overallDuration());
        course.setCoverUrl(dto.coverUrl());
        course.setCoverPublicId(dto.coverPublicId());

        if (dto.level() != null) {
            course.setLevel(dto.level());
        }
        if (dto.status() != null) {
            course.setStatus(dto.status());
        }
        if (dto.learningOutcomes() != null) {
            course.setLearningOutcomes(dto.learningOutcomes());
        }
        if (dto.requirements() != null) {
            course.setRequirements(dto.requirements());
        }
        if (dto.accessDurationDays() != null && dto.accessDurationDays() > 0) {
            course.setAccessDurationDays(dto.accessDurationDays());
        }

        organizeCourseCover(course);
        Courses updated = courseRepository.save(course);
        return courseMapper.toDTO(updated);
    }

    private void organizeCourseCover(Courses course) {
        if (fileUploadService == null || course == null || course.getCoverPublicId() == null || course.getCoverPublicId().isBlank()) {
            return;
        }
        String instructorUsername = course.getInstructor() != null ? course.getInstructor().getUsername() : null;
        var organized = fileUploadService.organizeCourseCover(course.getCoverPublicId(), instructorUsername, course.getTitle());
        if (organized != null) {
            course.setCoverPublicId(organized.publicId());
            course.setCoverUrl(organized.url());
        }
    }

    @Transactional(readOnly = true)
    public MyCoursesSummaryDTO getMyCoursesSummary(String userEmail) {
        List<Enrollments> enrollments = enrollmentRepository.findByUser_Email(userEmail);

        List<CourseResponseDTO> inProgress = enrollments.stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE && !e.isExpired())
                .map(e -> courseMapper.toDTO(e.getCourse(), courseFavoriteRepository.existsByUser_EmailAndCourse_CourseId(userEmail, e.getCourse().getCourseId())))
                .toList();

        List<CourseResponseDTO> completed = enrollments.stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.COMPLETED && !e.isExpired())
                .map(e -> courseMapper.toDTO(e.getCourse(), courseFavoriteRepository.existsByUser_EmailAndCourse_CourseId(userEmail, e.getCourse().getCourseId())))
                .toList();

        List<CourseResponseDTO> expired = enrollments.stream()
                .filter(Enrollments::isExpired)
                .map(e -> courseMapper.toDTO(e.getCourse(), courseFavoriteRepository.existsByUser_EmailAndCourse_CourseId(userEmail, e.getCourse().getCourseId())))
                .toList();

        List<CourseResponseDTO> saved = courseFavoriteRepository.findByUser_EmailOrderByCreatedAtDesc(userEmail)
                .stream()
                .map(f -> courseMapper.toDTO(f.getCourse(), true))
                .toList();

        return new MyCoursesSummaryDTO(inProgress, completed, saved, expired);
    }
}
