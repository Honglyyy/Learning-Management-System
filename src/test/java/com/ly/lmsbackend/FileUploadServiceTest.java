package com.ly.lmsbackend;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.ly.lmsbackend.dto.filedtos.FileUploadResponseDTO;
import com.ly.lmsbackend.model.*;
import com.ly.lmsbackend.repository.*;
import com.ly.lmsbackend.service.FileUploadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FileUploadServiceTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private SectionRepository sectionRepository;

    @Mock
    private AssignmentRepository assignmentRepository;

    private FileUploadService fileUploadService;

    @BeforeEach
    void setUp() throws IOException {
        lenient().when(cloudinary.uploader()).thenReturn(uploader);
        fileUploadService = new FileUploadService(
                cloudinary,
                5242880L,
                524288000L,
                52428800L,
                userRepository,
                courseRepository,
                lessonRepository,
                sectionRepository,
                assignmentRepository
        );
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Folder sanitization replaces spaces and forbidden path characters")
    void testSanitize() {
        assertEquals("general", FileUploadService.sanitize(null));
        assertEquals("general", FileUploadService.sanitize(""));
        assertEquals("johndoe", FileUploadService.sanitize("JohnDoe"));
        assertEquals("john_doe", FileUploadService.sanitize("john_doe"));
        assertEquals("spring-boot-react-2026", FileUploadService.sanitize("Spring Boot & React: 2026!"));
        assertEquals("01-intro-overview", FileUploadService.sanitize("01. Intro / Overview?"));
    }

    @Test
    @DisplayName("Upload profile picture organizes into profile-picture/$username")
    void testUploadProfilePicture() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.png",
                "image/png",
                new byte[]{1, 2, 3, 4}
        );

        when(uploader.upload(any(byte[].class), anyMap())).thenAnswer(invocation -> {
            Map<String, Object> options = invocation.getArgument(1);
            String folder = (String) options.get("folder");
            String publicId = (String) options.get("public_id");
            return Map.of(
                    "secure_url", "https://res.cloudinary.com/test/" + folder + "/" + publicId + ".png",
                    "public_id", folder + "/" + publicId
            );
        });

        FileUploadResponseDTO response = fileUploadService.uploadProfilePhoto(file, "alice_smith");

        assertNotNull(response);
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(uploader).upload(any(byte[].class), captor.capture());

        Map<String, Object> options = captor.getValue();
        assertEquals("profile-picture/alice_smith", options.get("folder"));
        assertEquals("image", options.get("resource_type"));
        assertTrue(response.url().contains("profile-picture/alice_smith"));
        assertTrue(response.publicId().startsWith("profile-picture/alice_smith/"));
    }

    @Test
    @DisplayName("Upload course cover organizes into course/$username/$coursename")
    void testUploadCourseCover() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "cover.jpg",
                "image/jpeg",
                new byte[]{1, 2, 3, 4}
        );

        when(uploader.upload(any(byte[].class), anyMap())).thenAnswer(invocation -> {
            Map<String, Object> options = invocation.getArgument(1);
            String folder = (String) options.get("folder");
            String publicId = (String) options.get("public_id");
            return Map.of(
                    "secure_url", "https://res.cloudinary.com/test/" + folder + "/" + publicId + ".jpg",
                    "public_id", folder + "/" + publicId
            );
        });

        FileUploadResponseDTO response = fileUploadService.uploadCourseCover(
                file,
                null,
                "Spring Boot Masterclass",
                "instructor_john"
        );

        assertNotNull(response);
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(uploader).upload(any(byte[].class), captor.capture());

        Map<String, Object> options = captor.getValue();
        assertEquals("course/instructor_john/spring-boot-masterclass", options.get("folder"));
        assertEquals("image", options.get("resource_type"));
    }

    @Test
    @DisplayName("Upload lesson video organizes into course/$username/$coursename/$lesson")
    void testUploadLessonVideo() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "intro.mp4",
                "video/mp4",
                new byte[]{1, 2, 3, 4}
        );

        when(uploader.upload(any(byte[].class), anyMap())).thenAnswer(invocation -> {
            Map<String, Object> options = invocation.getArgument(1);
            String folder = (String) options.get("folder");
            String publicId = (String) options.get("public_id");
            return Map.of(
                    "secure_url", "https://res.cloudinary.com/test/" + folder + "/" + publicId + ".mp4",
                    "public_id", folder + "/" + publicId
            );
        });

        FileUploadResponseDTO response = fileUploadService.uploadLessonVideo(
                file,
                null,
                null,
                null,
                "01. Introduction to Java",
                "Java Basics",
                "prof_bob"
        );

        assertNotNull(response);
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(uploader).upload(any(byte[].class), captor.capture());

        Map<String, Object> options = captor.getValue();
        assertEquals("course/prof_bob/java-basics/01-introduction-to-java", options.get("folder"));
        assertEquals("video", options.get("resource_type"));
    }

    @Test
    @DisplayName("Upload lesson material organizes into course/$username/$coursename/$lesson")
    void testUploadLessonMaterial() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "notes.pdf",
                "application/pdf",
                new byte[]{1, 2, 3, 4}
        );

        when(uploader.upload(any(byte[].class), anyMap())).thenAnswer(invocation -> {
            Map<String, Object> options = invocation.getArgument(1);
            String folder = (String) options.get("folder");
            String publicId = (String) options.get("public_id");
            return Map.of(
                    "secure_url", "https://res.cloudinary.com/test/" + folder + "/" + publicId + ".pdf",
                    "public_id", folder + "/" + publicId
            );
        });

        FileUploadResponseDTO response = fileUploadService.uploadMaterial(
                file,
                null,
                null,
                null,
                "02. Variables and Types",
                "Java Basics",
                "prof_bob"
        );

        assertNotNull(response);
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(uploader).upload(any(byte[].class), captor.capture());

        Map<String, Object> options = captor.getValue();
        assertEquals("course/prof_bob/java-basics/02-variables-and-types", options.get("folder"));
        assertEquals("auto", options.get("resource_type"));
    }

    @Test
    @DisplayName("Upload lesson video resolving course and instructor from lessonId in DB")
    void testUploadLessonVideoResolvingFromDB() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "deep-dive.mp4",
                "video/mp4",
                new byte[]{1, 2, 3, 4}
        );

        Users instructor = Users.builder().id(10L).username("master_instructor").build();
        Courses course = new Courses();
        course.setCourseId(100L);
        course.setTitle("Advanced Algorithms");
        course.setInstructor(instructor);

        Sections section = new Sections();
        section.setSectionId(200L);
        section.setCourse(course);

        Lessons lesson = new Lessons();
        lesson.setLessonId(300L);
        lesson.setTitle("Dynamic Programming 101");
        lesson.setSection(section);

        when(lessonRepository.findById(300L)).thenReturn(Optional.of(lesson));

        when(uploader.upload(any(byte[].class), anyMap())).thenAnswer(invocation -> {
            Map<String, Object> options = invocation.getArgument(1);
            String folder = (String) options.get("folder");
            String publicId = (String) options.get("public_id");
            return Map.of(
                    "secure_url", "https://res.cloudinary.com/test/" + folder + "/" + publicId + ".mp4",
                    "public_id", folder + "/" + publicId
            );
        });

        FileUploadResponseDTO response = fileUploadService.uploadLessonVideo(
                file,
                300L,
                null,
                null,
                null,
                null,
                null
        );

        assertNotNull(response);
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(uploader).upload(any(byte[].class), captor.capture());

        Map<String, Object> options = captor.getValue();
        assertEquals("course/master_instructor/advanced-algorithms/dynamic-programming-101", options.get("folder"));
    }

    @Test
    @DisplayName("Upload assignment supporting file organizes into course/$username/$coursename/assignments")
    void testUploadAssignmentSupportingFile() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "instructions.pdf",
                "application/pdf",
                new byte[]{1, 2, 3, 4}
        );

        when(uploader.upload(any(byte[].class), anyMap())).thenAnswer(invocation -> {
            Map<String, Object> options = invocation.getArgument(1);
            String folder = (String) options.get("folder");
            String publicId = (String) options.get("public_id");
            return Map.of(
                    "secure_url", "https://res.cloudinary.com/test/" + folder + "/" + publicId + ".pdf",
                    "public_id", folder + "/" + publicId
            );
        });

        FileUploadResponseDTO response = fileUploadService.uploadAssignmentFile(
                file,
                null,
                null,
                null,
                null,
                "Final Project",
                "Full Stack Web",
                null,
                "instructor_dave"
        );

        assertNotNull(response);
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(uploader).upload(any(byte[].class), captor.capture());

        Map<String, Object> options = captor.getValue();
        assertEquals("course/instructor_dave/full-stack-web/assignments", options.get("folder"));
        assertEquals("auto", options.get("resource_type"));
    }

    @Test
    @DisplayName("Upload assignment submission by student organizes into course/$username/$coursename/assignments/submissions/$student")
    void testUploadAssignmentSubmissionStudent() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "submission.zip",
                "application/zip",
                new byte[]{1, 2, 3, 4}
        );

        Users studentUser = Users.builder().id(50L).username("student_charlie").email("charlie@test.com").role(Roles.STUDENT).build();
        when(userRepository.findByEmail("charlie@test.com")).thenReturn(Optional.of(studentUser));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("charlie@test.com", "password", studentUser.getAuthorities())
        );

        when(uploader.upload(any(byte[].class), anyMap())).thenAnswer(invocation -> {
            Map<String, Object> options = invocation.getArgument(1);
            String folder = (String) options.get("folder");
            String publicId = (String) options.get("public_id");
            return Map.of(
                    "secure_url", "https://res.cloudinary.com/test/" + folder + "/" + publicId + ".zip",
                    "public_id", folder + "/" + publicId
            );
        });

        FileUploadResponseDTO response = fileUploadService.uploadAssignmentFile(
                file,
                null,
                null,
                null,
                null,
                null,
                "Full Stack Web",
                null,
                "instructor_dave"
        );

        assertNotNull(response);
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(uploader).upload(any(byte[].class), captor.capture());

        Map<String, Object> options = captor.getValue();
        assertEquals("course/instructor_dave/full-stack-web/assignments/submissions/student_charlie", options.get("folder"));
        assertEquals("auto", options.get("resource_type"));
    }

    @Test
    @DisplayName("Delete asset destroys asset using full publicId")
    void testDeleteAsset() throws IOException {
        when(uploader.destroy(eq("course/john/java/intro"), anyMap())).thenReturn(Map.of("result", "ok"));

        fileUploadService.deleteAsset("course/john/java/intro");

        verify(uploader).destroy(eq("course/john/java/intro"), anyMap());
    }
}
