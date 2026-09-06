package com.ly.lmsbackend;

import com.ly.lmsbackend.dto.authdtos.ChangePasswordRequest;
import com.ly.lmsbackend.dto.authdtos.RegisterRequest;
import com.ly.lmsbackend.dto.authdtos.UserResponseDTO;
import com.ly.lmsbackend.dto.coursedtos.CourseResponseDTO;
import com.ly.lmsbackend.dto.filedtos.FileUploadResponseDTO;
import com.ly.lmsbackend.dto.instructordtos.InstructorProfileUpdateDTO;
import com.ly.lmsbackend.dto.instructordtos.InstructorResponseDTO;
import com.ly.lmsbackend.dto.studentdtos.StudentProfileResponseDTO;
import com.ly.lmsbackend.dto.studentdtos.StudentProfileUpdateDTO;
import com.ly.lmsbackend.mapper.CourseMapper;
import com.ly.lmsbackend.mapper.UserMapper;
import com.ly.lmsbackend.model.Courses;
import com.ly.lmsbackend.model.Genders;
import com.ly.lmsbackend.model.Instructors;
import com.ly.lmsbackend.model.Roles;
import com.ly.lmsbackend.model.Students;
import com.ly.lmsbackend.model.Users;
import com.ly.lmsbackend.repository.*;
import com.ly.lmsbackend.service.EmailService;
import com.ly.lmsbackend.service.FileUploadService;
import com.ly.lmsbackend.service.InstructorService;
import com.ly.lmsbackend.service.StudentService;
import com.ly.lmsbackend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class Stage1ServiceAndControllerTests {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private UserMapper userMapper;
    @Mock private EmailService emailService;
    @Mock private QuizAttemptRepository quizAttemptRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private EnrollmentRepository enrollmentRepository;
    @Mock private CourseReviewRepository courseReviewRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private AnswerRepository answerRepository;
    @Mock private QuestionRepository questionRepository;
    @Mock private QuizRepository quizRepository;
    @Mock private LessonRepository lessonRepository;
    @Mock private SectionRepository sectionRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private InstructorRepository instructorRepository;
    @Mock private FileUploadService fileUploadService;
    @Mock private CourseMapper courseMapper;

    private UserService userService;
    private StudentService studentService;
    private InstructorService instructorService;

    @BeforeEach
    void setUp() {
        userService = new UserService(
                userRepository,
                passwordEncoder,
                userMapper,
                emailService,
                quizAttemptRepository,
                paymentRepository,
                enrollmentRepository,
                courseReviewRepository,
                courseRepository,
                answerRepository,
                questionRepository,
                quizRepository,
                lessonRepository,
                sectionRepository,
                studentRepository,
                instructorRepository
        );

        studentService = new StudentService(
                studentRepository,
                userRepository,
                fileUploadService
        );

        instructorService = new InstructorService(
                instructorRepository,
                userRepository,
                courseRepository,
                courseMapper,
                fileUploadService
        );
    }

    @Test
    void testRegisterStudent_createsUsersAndStudentProfileWithStudentCode() {
        RegisterRequest request = new RegisterRequest(
                "johndoe",
                "john@example.com",
                "password123",
                "John Doe",
                "012345678",
                Roles.STUDENT
        );

        Users mockSavedUser = Users.builder()
                .id(1L)
                .username("johndoe")
                .email("john@example.com")
                .fullname("John Doe")
                .role(Roles.STUDENT)
                .build();

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(mockSavedUser));
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(studentRepository.count()).thenReturn(0L);
        when(studentRepository.existsByStudentCode(anyString())).thenReturn(false);

        when(userRepository.save(any(Users.class))).thenReturn(mockSavedUser);

        UserResponseDTO expectedDTO = UserResponseDTO.builder()
                .id(1L)
                .username("johndoe")
                .email("john@example.com")
                .role("STUDENT")
                .build();
        when(userMapper.dto(mockSavedUser)).thenReturn(expectedDTO);

        UserResponseDTO result = userService.register(request);

        assertNotNull(result);
        assertEquals("STUDENT", result.role());

        org.mockito.ArgumentCaptor<Users> userCaptor = org.mockito.ArgumentCaptor.forClass(Users.class);
        verify(userRepository, atLeastOnce()).save(userCaptor.capture());
        Users firstSaved = userCaptor.getAllValues().get(0);
        assertNotNull(firstSaved.getStudent(), "Student profile should be attached to user");
        assertEquals("John Doe", firstSaved.getStudent().getFullName());
        assertTrue(firstSaved.getStudent().getStudentCode().startsWith("STU-"));
    }

    @Test
    void testRegisterInstructor_createsUsersAndInstructorProfile() {
        RegisterRequest request = new RegisterRequest(
                "profsmith",
                "smith@example.com",
                "password123",
                "Prof Smith",
                "098765432",
                Roles.INSTRUCTOR
        );

        Users mockSavedUser = Users.builder()
                .id(2L)
                .username("profsmith")
                .email("smith@example.com")
                .fullname("Prof Smith")
                .role(Roles.INSTRUCTOR)
                .build();

        when(userRepository.findByEmail("smith@example.com"))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(mockSavedUser));
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

        when(userRepository.save(any(Users.class))).thenReturn(mockSavedUser);

        UserResponseDTO expectedDTO = UserResponseDTO.builder()
                .id(2L)
                .username("profsmith")
                .email("smith@example.com")
                .role("INSTRUCTOR")
                .build();
        when(userMapper.dto(mockSavedUser)).thenReturn(expectedDTO);

        UserResponseDTO result = userService.register(request);

        assertNotNull(result);
        assertEquals("INSTRUCTOR", result.role());

        org.mockito.ArgumentCaptor<Users> userCaptor = org.mockito.ArgumentCaptor.forClass(Users.class);
        verify(userRepository, atLeastOnce()).save(userCaptor.capture());
        Users firstSaved = userCaptor.getAllValues().get(0);
        assertNotNull(firstSaved.getInstructor(), "Instructor profile should be attached to user");
        assertEquals("Prof Smith", firstSaved.getInstructor().getFullName());
        assertEquals(5.0, firstSaved.getInstructor().getAverageRating());
    }

    @Test
    void testChangePassword_success() {
        Users user = Users.builder()
                .id(1L)
                .email("john@example.com")
                .password("encodedOldPassword")
                .build();

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPassword", "encodedOldPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword123")).thenReturn("encodedNewPassword");

        ChangePasswordRequest request = new ChangePasswordRequest("oldPassword", "newPassword123");
        assertDoesNotThrow(() -> userService.changePassword("john@example.com", request));

        assertEquals("encodedNewPassword", user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    void testChangePassword_wrongOldPassword_throwsException() {
        Users user = Users.builder()
                .id(1L)
                .email("john@example.com")
                .password("encodedOldPassword")
                .build();

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "encodedOldPassword")).thenReturn(false);

        ChangePasswordRequest request = new ChangePasswordRequest("wrongPassword", "newPassword123");
        assertThrows(ResponseStatusException.class, () -> userService.changePassword("john@example.com", request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void testStudentProfile_getAndUpdate() {
        Users user = Users.builder()
                .id(10L)
                .username("student1")
                .email("student1@example.com")
                .fullname("Original Name")
                .phoneNumber("011111111")
                .build();

        Students student = Students.builder()
                .id(5L)
                .user(user)
                .studentCode("STU-2026-0005")
                .fullName("Original Name")
                .phoneNumber("011111111")
                .gender(Genders.MALE)
                .dateOfBirth(LocalDate.of(2000, 1, 1))
                .educationLevel("Bachelor")
                .profilePhotoUrl("https://res.cloudinary.com/demo/image/upload/v1/old.jpg")
                .profilePhotoPublicId("profile-photos/students/old_id")
                .build();

        when(userRepository.findByEmail("student1@example.com")).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Id(10L)).thenReturn(Optional.of(student));

        StudentProfileResponseDTO profile = studentService.getProfile("student1@example.com");
        assertEquals("STU-2026-0005", profile.studentCode());
        assertEquals("Original Name", profile.fullName());
        assertEquals(Genders.MALE, profile.gender());

        StudentProfileUpdateDTO updateDTO = new StudentProfileUpdateDTO(
                "Updated Name",
                "099999999",
                Genders.FEMALE,
                LocalDate.of(2001, 2, 2),
                "Master",
                "https://res.cloudinary.com/demo/image/upload/v1/new.jpg",
                "profile-photos/students/new_id"
        );

        when(studentRepository.save(any(Students.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StudentProfileResponseDTO updated = studentService.updateProfile("student1@example.com", updateDTO);

        assertEquals("Updated Name", updated.fullName());
        assertEquals("099999999", updated.phoneNumber());
        assertEquals(Genders.FEMALE, updated.gender());
        assertEquals("Master", updated.educationLevel());
        verify(fileUploadService).deleteAsset("profile-photos/students/old_id");
    }

    @Test
    void testStudentProfile_uploadPhoto_callsCloudinaryAndCleansOldPhoto() {
        Users user = Users.builder().id(10L).email("student1@example.com").build();
        Students student = Students.builder()
                .id(5L)
                .user(user)
                .profilePhotoPublicId("profile-photos/students/existing_id")
                .build();

        when(userRepository.findByEmail("student1@example.com")).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Id(10L)).thenReturn(Optional.of(student));
        when(studentRepository.save(any(Students.class))).thenAnswer(i -> i.getArgument(0));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.png",
                "image/png",
                "test-image-content".getBytes()
        );

        FileUploadResponseDTO uploadResponse = new FileUploadResponseDTO(
                "avatar.png",
                "profile-photos/students/new_public_id",
                "image/png",
                file.getSize(),
                "https://res.cloudinary.com/demo/image/upload/v1/new_public_id.png"
        );
        when(fileUploadService.uploadStudentPhoto(file)).thenReturn(uploadResponse);

        StudentProfileResponseDTO response = studentService.uploadProfilePhoto("student1@example.com", file);

        verify(fileUploadService).deleteAsset("profile-photos/students/existing_id");
        assertEquals("https://res.cloudinary.com/demo/image/upload/v1/new_public_id.png", response.profilePhotoUrl());
        assertEquals("profile-photos/students/new_public_id", response.profilePhotoPublicId());
    }

    @Test
    void testInstructorService_getAllAndGetById() {
        Users user = Users.builder().id(20L).username("teacher").email("teacher@test.com").build();
        Instructors instructor = Instructors.builder()
                .id(3L)
                .user(user)
                .fullName("Teacher Guy")
                .biography("Experienced instructor")
                .expertise("Java, Spring Boot")
                .averageRating(4.9)
                .build();

        Courses course = new Courses();
        course.setCourseId(100L);
        course.setTitle("Spring Boot Masterclass");
        course.setPrice(BigDecimal.valueOf(49.99));
        course.setInstructor(user);

        CourseResponseDTO courseDTO = new CourseResponseDTO(
                100L,
                "Spring Boot Masterclass",
                "Description",
                BigDecimal.valueOf(49.99),
                "10h",
                "https://res.cloudinary.com/demo/image/upload/cover.jpg",
                "cover_id",
                20L,
                "teacher",
                Collections.emptyList(),
                Collections.emptyList(),
                5.0
        );

        when(instructorRepository.findAll()).thenReturn(List.of(instructor));
        when(instructorRepository.findById(3L)).thenReturn(Optional.of(instructor));
        when(courseRepository.findByInstructor_Id(20L)).thenReturn(List.of(course));
        when(courseMapper.toDTO(course)).thenReturn(courseDTO);

        List<InstructorResponseDTO> all = instructorService.getAllInstructors();
        assertEquals(1, all.size());
        assertEquals(1, all.get(0).totalCourses());
        assertEquals("Teacher Guy", all.get(0).fullName());

        InstructorResponseDTO detail = instructorService.getInstructorById(3L);
        assertEquals("Teacher Guy", detail.fullName());
        assertEquals(1, detail.courses().size());
        assertEquals("Spring Boot Masterclass", detail.courses().get(0).title());
    }

    @Test
    void testInstructorService_updateProfileAndPhoto() {
        Users user = Users.builder().id(20L).email("teacher@test.com").build();
        Instructors instructor = Instructors.builder()
                .id(3L)
                .user(user)
                .fullName("Old Teacher")
                .profilePhotoPublicId("profile-photos/instructors/old_pic")
                .build();

        when(userRepository.findByEmail("teacher@test.com")).thenReturn(Optional.of(user));
        when(instructorRepository.findByUser_Id(20L)).thenReturn(Optional.of(instructor));
        when(instructorRepository.save(any(Instructors.class))).thenAnswer(i -> i.getArgument(0));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "photo.jpg",
                "image/jpeg",
                "some-bytes".getBytes()
        );

        FileUploadResponseDTO uploadResponse = new FileUploadResponseDTO(
                "photo.jpg",
                "profile-photos/instructors/new_pic",
                "image/jpeg",
                file.getSize(),
                "https://res.cloudinary.com/demo/image/upload/v1/new_pic.jpg"
        );
        when(fileUploadService.uploadInstructorPhoto(file)).thenReturn(uploadResponse);

        InstructorResponseDTO response = instructorService.uploadProfilePhoto("teacher@test.com", file);

        verify(fileUploadService).deleteAsset("profile-photos/instructors/old_pic");
        assertEquals("https://res.cloudinary.com/demo/image/upload/v1/new_pic.jpg", response.profilePhotoUrl());
        assertEquals("profile-photos/instructors/new_pic", response.profilePhotoPublicId());
    }
}
