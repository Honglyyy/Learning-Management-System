package com.ly.lmsbackend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ly.lmsbackend.controller.CourseController;
import com.ly.lmsbackend.controller.CourseFavoriteController;
import com.ly.lmsbackend.dto.coursedtos.*;
import com.ly.lmsbackend.mapper.CourseMapper;
import com.ly.lmsbackend.mapper.SectionMapper;
import com.ly.lmsbackend.model.*;
import com.ly.lmsbackend.repository.*;
import com.ly.lmsbackend.service.CourseFavoriteService;
import com.ly.lmsbackend.service.CourseService;
import com.ly.lmsbackend.service.SectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.security.Principal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class Stage4CourseDiscoveryAndFavoritesTests {

    @Mock private CourseRepository courseRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private UserRepository userRepository;
    @Mock private SectionRepository sectionRepository;
    @Mock private SectionMapper sectionMapper;
    @Mock private CourseReviewRepository courseReviewRepository;
    @Mock private QuizAttemptRepository quizAttemptRepository;
    @Mock private CourseFavoriteRepository courseFavoriteRepository;
    @Mock private EnrollmentRepository enrollmentRepository;
    @Mock private SectionService sectionService;

    private CourseMapper courseMapper;
    private CourseService courseService;
    private CourseFavoriteService courseFavoriteService;
    private MockMvc mockMvcCourse;
    private MockMvc mockMvcFavorite;
    private ObjectMapper objectMapper;

    private Users studentUser;
    private Users instructorUser;
    private Users adminUser;
    private Courses course1;
    private Courses course2;
    private Categories category;

    @BeforeEach
    void setUp() {
        courseMapper = new CourseMapper(courseReviewRepository);
        courseService = new CourseService(
                courseRepository,
                courseMapper,
                categoryRepository,
                userRepository,
                sectionRepository,
                sectionMapper,
                courseReviewRepository,
                quizAttemptRepository,
                courseFavoriteRepository,
                enrollmentRepository
        );

        courseFavoriteService = new CourseFavoriteService(
                courseFavoriteRepository,
                courseRepository,
                userRepository,
                courseMapper
        );

        CourseController courseController = new CourseController(courseService, sectionService);
        mockMvcCourse = MockMvcBuilders.standaloneSetup(courseController).build();

        CourseFavoriteController favoriteController = new CourseFavoriteController(courseFavoriteService);
        mockMvcFavorite = MockMvcBuilders.standaloneSetup(favoriteController).build();

        objectMapper = new ObjectMapper();

        studentUser = Users.builder()
                .id(1L)
                .username("student1")
                .email("student@test.com")
                .fullname("Alice Student")
                .role(Roles.STUDENT)
                .build();

        instructorUser = Users.builder()
                .id(2L)
                .username("instructor1")
                .email("instructor@test.com")
                .fullname("Bob Instructor")
                .role(Roles.INSTRUCTOR)
                .build();

        adminUser = Users.builder()
                .id(3L)
                .username("admin1")
                .email("admin@test.com")
                .fullname("Charlie Admin")
                .role(Roles.ADMIN)
                .build();

        category = new Categories();
        category.setCategoryId(10L);
        category.setCategory("Programming");

        course1 = new Courses();
        course1.setCourseId(100L);
        course1.setTitle("Spring Boot Mastery");
        course1.setDescription("Deep dive into Spring Framework");
        course1.setPrice(BigDecimal.valueOf(49.99));
        course1.setOverallDuration("10 hours");
        course1.setLevel(CourseLevel.INTERMEDIATE);
        course1.setStatus(CourseStatus.PUBLISHED);
        course1.setInstructor(instructorUser);
        course1.setCategories(List.of(category));
        course1.setSections(new ArrayList<>());
        course1.setEnrollments(new ArrayList<>());

        course2 = new Courses();
        course2.setCourseId(101L);
        course2.setTitle("React & TypeScript for Beginners");
        course2.setDescription("Frontend fundamentals");
        course2.setPrice(BigDecimal.valueOf(19.99));
        course2.setOverallDuration("5 hours");
        course2.setLevel(CourseLevel.BEGINNER);
        course2.setStatus(CourseStatus.DRAFT);
        course2.setInstructor(instructorUser);
        course2.setCategories(List.of(category));
        course2.setSections(new ArrayList<>());
        course2.setEnrollments(new ArrayList<>());
    }

    // ─────────────────────────────────────────────────────────────
    // 1. DTO Backward Compatibility Tests
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("CourseResponseDTO backward compatible constructor works")
    void testCourseResponseDTOBackwardCompatibility() {
        CourseResponseDTO dto = new CourseResponseDTO(
                1L, "Title", "Desc", BigDecimal.TEN, "2 hours",
                "cover.jpg", "pub-1", 2L, "instructor",
                List.of(1L), List.of("Category"), 4.5
        );
        assertEquals(CourseLevel.ALL_LEVELS, dto.level());
        assertEquals(CourseStatus.PUBLISHED, dto.status());
        assertEquals(0L, dto.lessonCount());
        assertFalse(dto.isFavorite());
    }

    @Test
    @DisplayName("CourseCreateDTO backward compatible constructor works")
    void testCourseCreateDTOBackwardCompatibility() {
        CourseCreateDTO dto = new CourseCreateDTO(
                "Title", "Desc", BigDecimal.TEN, "2 hours",
                "cover.jpg", "pub-1", 2L, List.of(1L)
        );
        assertEquals(CourseLevel.ALL_LEVELS, dto.level());
        assertEquals(CourseStatus.PUBLISHED, dto.status());
        assertNull(dto.learningOutcomes());
    }

    @Test
    @DisplayName("CourseDetailDTO backward compatible constructor works")
    void testCourseDetailDTOBackwardCompatibility() {
        CourseDetailDTO dto = new CourseDetailDTO(
                1L, "Title", "Desc", BigDecimal.TEN, "2 hours",
                "cover.jpg", "pub-1", "instructor", 2L, 4.5,
                List.of("Category"), List.of(), List.of()
        );
        assertEquals(CourseLevel.ALL_LEVELS, dto.level());
        assertEquals(CourseStatus.PUBLISHED, dto.status());
        assertFalse(dto.isFavorite());
    }

    // ─────────────────────────────────────────────────────────────
    // 2. Dynamic Course Search & Filtering Tests
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("Search and filter courses returns published courses with correct mapping")
    void testSearchAndFilterCourses() {
        when(courseRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(List.of(course1));

        List<CourseResponseDTO> results = courseService.searchAndFilterCourses(
                "Spring", 10L, CourseLevel.INTERMEDIATE, null, BigDecimal.valueOf(50.00), CourseStatus.PUBLISHED, "latest", null
        );

        assertEquals(1, results.size());
        assertEquals("Spring Boot Mastery", results.get(0).title());
        assertEquals(CourseLevel.INTERMEDIATE, results.get(0).level());
        assertEquals(CourseStatus.PUBLISHED, results.get(0).status());
    }

    @Test
    @DisplayName("Filter courses with minRating filters out lower rated courses")
    void testSearchAndFilterWithMinRating() {
        when(courseRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(List.of(course1));

        CourseReviews review = new CourseReviews();
        review.setRating(3);
        when(courseReviewRepository.findByCourse_CourseId(100L)).thenReturn(List.of(review));

        List<CourseResponseDTO> results = courseService.searchAndFilterCourses(
                null, null, null, 4.0, null, CourseStatus.PUBLISHED, "latest", null
        );

        // course1 has avg rating 3.0, which is < 4.0, so it should be filtered out
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("Search courses marks isFavorite true when user favorited course")
    void testSearchCoursesWithUserFavorites() {
        when(courseRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(List.of(course1));

        CourseFavorite fav = CourseFavorite.builder()
                .id(1L)
                .user(studentUser)
                .course(course1)
                .build();
        when(courseFavoriteRepository.findByUser_EmailOrderByCreatedAtDesc("student@test.com"))
                .thenReturn(List.of(fav));

        List<CourseResponseDTO> results = courseService.searchAndFilterCourses(
                null, null, null, null, null, CourseStatus.PUBLISHED, "latest", "student@test.com"
        );

        assertEquals(1, results.size());
        assertTrue(results.get(0).isFavorite());
    }

    // ─────────────────────────────────────────────────────────────
    // 3. Featured & Popular Course Feeds Tests
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("getFeaturedCourses returns top published courses limited to 6")
    void testGetFeaturedCourses() {
        when(courseRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(List.of(course1));

        List<CourseResponseDTO> featured = courseService.getFeaturedCourses(null);
        assertEquals(1, featured.size());
        assertEquals("Spring Boot Mastery", featured.get(0).title());
    }

    @Test
    @DisplayName("getPopularCourses returns popular courses")
    void testGetPopularCourses() {
        when(courseRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(List.of(course1));

        List<CourseResponseDTO> popular = courseService.getPopularCourses(null);
        assertEquals(1, popular.size());
    }

    // ─────────────────────────────────────────────────────────────
    // 4. Course Status Management Tests (PATCH /api/courses/{id}/status)
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("Instructor owner can update course status from DRAFT to PUBLISHED")
    void testUpdateCourseStatusByOwner() {
        when(courseRepository.findById(101L)).thenReturn(Optional.of(course2));
        when(userRepository.findByEmail("instructor@test.com")).thenReturn(Optional.of(instructorUser));
        when(courseRepository.save(any(Courses.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CourseResponseDTO result = courseService.updateCourseStatus(101L, CourseStatus.PUBLISHED, "instructor@test.com");

        assertEquals(CourseStatus.PUBLISHED, result.status());
        verify(courseRepository).save(course2);
    }

    @Test
    @DisplayName("Admin can update course status of any course")
    void testUpdateCourseStatusByAdmin() {
        when(courseRepository.findById(101L)).thenReturn(Optional.of(course2));
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(adminUser));
        when(courseRepository.save(any(Courses.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CourseResponseDTO result = courseService.updateCourseStatus(101L, CourseStatus.ARCHIVED, "admin@test.com");

        assertEquals(CourseStatus.ARCHIVED, result.status());
    }

    @Test
    @DisplayName("Non-owner student is forbidden from updating course status")
    void testUpdateCourseStatusForbidden() {
        when(courseRepository.findById(101L)).thenReturn(Optional.of(course2));
        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(studentUser));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                courseService.updateCourseStatus(101L, CourseStatus.PUBLISHED, "student@test.com"));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    @DisplayName("Update course status throws 404 if course not found")
    void testUpdateCourseStatusNotFound() {
        when(courseRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () ->
                courseService.updateCourseStatus(999L, CourseStatus.PUBLISHED, "admin@test.com"));
    }

    // ─────────────────────────────────────────────────────────────
    // 5. Course Favorites Service Tests
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("Add favorite creates new CourseFavorite")
    void testAddFavorite() {
        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(studentUser));
        when(courseRepository.findById(100L)).thenReturn(Optional.of(course1));
        when(courseFavoriteRepository.existsByUser_EmailAndCourse_CourseId("student@test.com", 100L))
                .thenReturn(false);

        CourseResponseDTO result = courseFavoriteService.addFavorite(100L, "student@test.com");

        assertNotNull(result);
        assertTrue(result.isFavorite());
        verify(courseFavoriteRepository).save(any(CourseFavorite.class));
    }

    @Test
    @DisplayName("Remove favorite deletes existing favorite")
    void testRemoveFavorite() {
        when(courseFavoriteRepository.existsByUser_EmailAndCourse_CourseId("student@test.com", 100L))
                .thenReturn(true);

        courseFavoriteService.removeFavorite(100L, "student@test.com");
        verify(courseFavoriteRepository).deleteByUser_EmailAndCourse_CourseId("student@test.com", 100L);
    }

    @Test
    @DisplayName("Get favorites returns list of favorited courses")
    void testGetFavorites() {
        CourseFavorite fav = CourseFavorite.builder()
                .id(1L)
                .user(studentUser)
                .course(course1)
                .createdAt(Timestamp.from(Instant.now()))
                .build();

        when(courseFavoriteRepository.findByUser_EmailOrderByCreatedAtDesc("student@test.com"))
                .thenReturn(List.of(fav));

        List<CourseResponseDTO> favorites = courseFavoriteService.getFavorites("student@test.com");

        assertEquals(1, favorites.size());
        assertEquals("Spring Boot Mastery", favorites.get(0).title());
        assertTrue(favorites.get(0).isFavorite());
    }

    @Test
    @DisplayName("isFavorite returns true if course is saved")
    void testIsFavorite() {
        when(courseFavoriteRepository.existsByUser_EmailAndCourse_CourseId("student@test.com", 100L))
                .thenReturn(true);

        Map<String, Boolean> result = courseFavoriteService.isFavorite(100L, "student@test.com");
        assertTrue(result.get("isFavorite"));
    }

    // ─────────────────────────────────────────────────────────────
    // 6. My Courses Summary Test (Requirement #15)
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("getMyCoursesSummary groups courses into In Progress, Completed, and Saved")
    void testGetMyCoursesSummary() {
        Enrollments activeEnrollment = new Enrollments();
        activeEnrollment.setEnrollmentId(1L);
        activeEnrollment.setUser(studentUser);
        activeEnrollment.setCourse(course1);
        activeEnrollment.setStatus(EnrollmentStatus.ACTIVE);

        Enrollments completedEnrollment = new Enrollments();
        completedEnrollment.setEnrollmentId(2L);
        completedEnrollment.setUser(studentUser);
        completedEnrollment.setCourse(course2);
        completedEnrollment.setStatus(EnrollmentStatus.COMPLETED);

        CourseFavorite fav = CourseFavorite.builder()
                .id(10L)
                .user(studentUser)
                .course(course1)
                .build();

        when(enrollmentRepository.findByUser_Email("student@test.com"))
                .thenReturn(List.of(activeEnrollment, completedEnrollment));
        when(courseFavoriteRepository.findByUser_EmailOrderByCreatedAtDesc("student@test.com"))
                .thenReturn(List.of(fav));

        MyCoursesSummaryDTO summary = courseService.getMyCoursesSummary("student@test.com");

        assertEquals(1, summary.inProgressCourses().size());
        assertEquals("Spring Boot Mastery", summary.inProgressCourses().get(0).title());
        assertEquals(1, summary.completedCourses().size());
        assertEquals("React & TypeScript for Beginners", summary.completedCourses().get(0).title());
        assertEquals(1, summary.savedCourses().size());
        assertEquals("Spring Boot Mastery", summary.savedCourses().get(0).title());
    }

    // ─────────────────────────────────────────────────────────────
    // 7. MockMvc Controller Endpoint Tests
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("GET /api/courses with query params returns 200 OK")
    void testGetCoursesController() throws Exception {
        when(courseRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(List.of(course1));

        mockMvcCourse.perform(get("/api/courses")
                        .param("query", "Spring")
                        .param("level", "INTERMEDIATE")
                        .param("sort", "latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Spring Boot Mastery"))
                .andExpect(jsonPath("$[0].level").value("INTERMEDIATE"));
    }

    @Test
    @DisplayName("GET /api/courses/featured returns 200 OK")
    void testGetFeaturedCoursesController() throws Exception {
        when(courseRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(List.of(course1));

        mockMvcCourse.perform(get("/api/courses/featured"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].courseId").value(100));
    }

    @Test
    @DisplayName("PATCH /api/courses/{id}/status updates course status")
    void testPatchCourseStatusController() throws Exception {
        when(courseRepository.findById(100L)).thenReturn(Optional.of(course1));
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(adminUser));
        when(courseRepository.save(any(Courses.class))).thenAnswer(i -> i.getArgument(0));

        Principal principal = new UsernamePasswordAuthenticationToken("admin@test.com", null);

        mockMvcCourse.perform(patch("/api/courses/100/status")
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CourseStatusUpdateDTO(CourseStatus.ARCHIVED))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ARCHIVED"));
    }

    @Test
    @DisplayName("POST /api/favorites/{courseId} adds course to favorites")
    void testAddFavoriteController() throws Exception {
        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(studentUser));
        when(courseRepository.findById(100L)).thenReturn(Optional.of(course1));
        when(courseFavoriteRepository.existsByUser_EmailAndCourse_CourseId("student@test.com", 100L))
                .thenReturn(false);

        Principal principal = new UsernamePasswordAuthenticationToken("student@test.com", null);

        mockMvcFavorite.perform(post("/api/favorites/100")
                        .principal(principal))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.courseId").value(100))
                .andExpect(jsonPath("$.isFavorite").value(true));
    }

    @Test
    @DisplayName("DELETE /api/favorites/{courseId} removes course from favorites")
    void testRemoveFavoriteController() throws Exception {
        when(courseFavoriteRepository.existsByUser_EmailAndCourse_CourseId("student@test.com", 100L))
                .thenReturn(true);

        Principal principal = new UsernamePasswordAuthenticationToken("student@test.com", null);

        mockMvcFavorite.perform(delete("/api/favorites/100")
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("GET /api/favorites/check/{courseId} returns favorite status")
    void testCheckFavoriteController() throws Exception {
        when(courseFavoriteRepository.existsByUser_EmailAndCourse_CourseId("student@test.com", 100L))
                .thenReturn(true);

        Principal principal = new UsernamePasswordAuthenticationToken("student@test.com", null);

        mockMvcFavorite.perform(get("/api/favorites/check/100")
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isFavorite").value(true));
    }
}
