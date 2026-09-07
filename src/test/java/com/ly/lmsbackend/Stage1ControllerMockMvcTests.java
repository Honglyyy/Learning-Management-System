package com.ly.lmsbackend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ly.lmsbackend.controller.InstructorController;
import com.ly.lmsbackend.controller.StudentProfileController;
import com.ly.lmsbackend.controller.UserController;
import com.ly.lmsbackend.dto.authdtos.ChangePasswordRequest;
import com.ly.lmsbackend.dto.authdtos.RegisterRequest;
import com.ly.lmsbackend.dto.authdtos.UserResponseDTO;
import com.ly.lmsbackend.dto.coursedtos.CourseResponseDTO;
import com.ly.lmsbackend.dto.instructordtos.InstructorProfileUpdateDTO;
import com.ly.lmsbackend.dto.instructordtos.InstructorResponseDTO;
import com.ly.lmsbackend.dto.studentdtos.StudentProfileResponseDTO;
import com.ly.lmsbackend.dto.studentdtos.StudentProfileUpdateDTO;
import com.ly.lmsbackend.model.Genders;
import com.ly.lmsbackend.model.Roles;
import com.ly.lmsbackend.service.EmailService;
import com.ly.lmsbackend.service.InstructorService;
import com.ly.lmsbackend.service.StudentService;
import com.ly.lmsbackend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class Stage1ControllerMockMvcTests {

    private MockMvc mockMvcStudent;
    private MockMvc mockMvcInstructor;
    private MockMvc mockMvcUser;
    private MockMvc mockMvcAuth;

    @Mock private StudentService studentService;
    @Mock private InstructorService instructorService;
    @Mock private UserService userService;
    @Mock private EmailService emailService;
    @Mock private org.springframework.security.authentication.AuthenticationManager authenticationManager;
    @Mock private com.ly.lmsbackend.util.JwtUtil jwtUtil;
    @Mock private com.ly.lmsbackend.mapper.UserMapper userMapper;
    @Mock private com.ly.lmsbackend.repository.UserRepository userRepository;
    @Mock private com.ly.lmsbackend.service.ActivityLogService activityLogService;

    private ObjectMapper objectMapper;
    private UsernamePasswordAuthenticationToken studentAuth;
    private UsernamePasswordAuthenticationToken instructorAuth;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        mockMvcStudent = MockMvcBuilders.standaloneSetup(new StudentProfileController(studentService)).build();
        mockMvcInstructor = MockMvcBuilders.standaloneSetup(new InstructorController(instructorService)).build();
        mockMvcUser = MockMvcBuilders.standaloneSetup(new UserController(userService)).build();
        mockMvcAuth = MockMvcBuilders.standaloneSetup(new com.ly.lmsbackend.controller.AuthController(
                authenticationManager, jwtUtil, userMapper, emailService, userRepository, activityLogService
        )).build();

        studentAuth = new UsernamePasswordAuthenticationToken(
                "student@test.com",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_STUDENT"))
        );

        instructorAuth = new UsernamePasswordAuthenticationToken(
                "instructor@test.com",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_INSTRUCTOR"))
        );
    }

    @Test
    void testGetStudentProfile_returnsOk() throws Exception {
        StudentProfileResponseDTO profile = StudentProfileResponseDTO.builder()
                .studentId(1L)
                .studentCode("STU-2026-0001")
                .fullName("Alice Student")
                .email("student@test.com")
                .gender(Genders.FEMALE)
                .build();

        when(studentService.getProfile("student@test.com")).thenReturn(profile);

        mockMvcStudent.perform(get("/api/students/profile").principal(studentAuth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentCode").value("STU-2026-0001"))
                .andExpect(jsonPath("$.fullName").value("Alice Student"))
                .andExpect(jsonPath("$.email").value("student@test.com"));
    }

    @Test
    void testUpdateStudentProfile_returnsOk() throws Exception {
        StudentProfileUpdateDTO updateDTO = new StudentProfileUpdateDTO(
                "Alice Updated",
                "012345678",
                Genders.FEMALE,
                LocalDate.of(1999, 5, 20),
                "Bachelor of Science",
                "https://res.cloudinary.com/demo/image/upload/v1/avatar.jpg",
                "profile-photos/students/avatar_id"
        );

        StudentProfileResponseDTO updated = StudentProfileResponseDTO.builder()
                .studentId(1L)
                .studentCode("STU-2026-0001")
                .fullName("Alice Updated")
                .phoneNumber("012345678")
                .educationLevel("Bachelor of Science")
                .build();

        when(studentService.updateProfile(eq("student@test.com"), any(StudentProfileUpdateDTO.class)))
                .thenReturn(updated);

        String updateJson = """
                {
                    "fullName": "Alice Updated",
                    "phoneNumber": "012345678",
                    "gender": "FEMALE",
                    "dateOfBirth": "1999-05-20",
                    "educationLevel": "Bachelor of Science",
                    "profilePhotoUrl": "https://res.cloudinary.com/demo/image/upload/v1/avatar.jpg",
                    "profilePhotoPublicId": "profile-photos/students/avatar_id"
                }
                """;

        mockMvcStudent.perform(put("/api/students/profile")
                        .principal(studentAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Alice Updated"))
                .andExpect(jsonPath("$.phoneNumber").value("012345678"))
                .andExpect(jsonPath("$.educationLevel").value("Bachelor of Science"));
    }

    @Test
    void testUploadStudentProfilePhoto_returnsOk() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "avatar.png", "image/png", "avatar-content".getBytes()
        );

        StudentProfileResponseDTO profile = StudentProfileResponseDTO.builder()
                .studentId(1L)
                .profilePhotoUrl("https://res.cloudinary.com/demo/image/upload/v1/new.png")
                .profilePhotoPublicId("profile-photos/students/new")
                .build();

        when(studentService.uploadProfilePhoto(eq("student@test.com"), any()))
                .thenReturn(profile);

        mockMvcStudent.perform(multipart("/api/students/profile/photo")
                        .file(file)
                        .principal(studentAuth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profilePhotoUrl").value("https://res.cloudinary.com/demo/image/upload/v1/new.png"))
                .andExpect(jsonPath("$.profilePhotoPublicId").value("profile-photos/students/new"));
    }

    @Test
    void testGetAllInstructors_public_returnsList() throws Exception {
        InstructorResponseDTO inst = InstructorResponseDTO.builder()
                .instructorId(10L)
                .fullName("Dr. Robert")
                .expertise("Data Science")
                .averageRating(4.8)
                .totalCourses(2)
                .build();

        when(instructorService.getAllInstructors()).thenReturn(List.of(inst));

        mockMvcInstructor.perform(get("/api/instructors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].instructorId").value(10))
                .andExpect(jsonPath("$[0].fullName").value("Dr. Robert"))
                .andExpect(jsonPath("$[0].expertise").value("Data Science"))
                .andExpect(jsonPath("$[0].totalCourses").value(2));
    }

    @Test
    void testGetInstructorById_public_returnsDetailWithCourses() throws Exception {
        CourseResponseDTO courseDTO = new CourseResponseDTO(
                55L,
                "Machine Learning 101",
                "Intro to ML",
                BigDecimal.valueOf(99.0),
                "20h",
                "cover_url",
                "cover_id",
                10L,
                "Dr. Robert",
                Collections.emptyList(),
                Collections.emptyList(),
                4.9
        );

        InstructorResponseDTO inst = InstructorResponseDTO.builder()
                .instructorId(10L)
                .fullName("Dr. Robert")
                .expertise("Data Science")
                .biography("PhD in AI")
                .courses(List.of(courseDTO))
                .totalCourses(1)
                .build();

        when(instructorService.getInstructorById(10L)).thenReturn(inst);

        mockMvcInstructor.perform(get("/api/instructors/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Dr. Robert"))
                .andExpect(jsonPath("$.biography").value("PhD in AI"))
                .andExpect(jsonPath("$.courses[0].title").value("Machine Learning 101"));
    }

    @Test
    void testGetInstructorMe_returnsProfile() throws Exception {
        InstructorResponseDTO inst = InstructorResponseDTO.builder()
                .instructorId(10L)
                .fullName("Dr. Robert")
                .email("instructor@test.com")
                .build();

        when(instructorService.getMyProfile("instructor@test.com")).thenReturn(inst);

        mockMvcInstructor.perform(get("/api/instructors/me").principal(instructorAuth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Dr. Robert"))
                .andExpect(jsonPath("$.email").value("instructor@test.com"));
    }

    @Test
    void testUpdateInstructorMe_returnsUpdatedProfile() throws Exception {
        InstructorProfileUpdateDTO updateDTO = new InstructorProfileUpdateDTO(
                "Dr. Robert Senior",
                "098765432",
                "Updated Bio",
                "Deep Learning & Cloud",
                "https://res.cloudinary.com/demo/image/upload/v1/prof.jpg",
                "profile-photos/instructors/prof_id"
        );

        InstructorResponseDTO updated = InstructorResponseDTO.builder()
                .instructorId(10L)
                .fullName("Dr. Robert Senior")
                .expertise("Deep Learning & Cloud")
                .build();

        when(instructorService.updateMyProfile(eq("instructor@test.com"), any(InstructorProfileUpdateDTO.class)))
                .thenReturn(updated);

        mockMvcInstructor.perform(put("/api/instructors/me")
                        .principal(instructorAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Dr. Robert Senior"))
                .andExpect(jsonPath("$.expertise").value("Deep Learning & Cloud"));
    }

    @Test
    void testUploadInstructorPhoto_returnsOk() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "prof.jpg", "image/jpeg", "image-bytes".getBytes()
        );

        InstructorResponseDTO updated = InstructorResponseDTO.builder()
                .instructorId(10L)
                .profilePhotoUrl("https://res.cloudinary.com/demo/image/upload/v1/prof_new.jpg")
                .profilePhotoPublicId("profile-photos/instructors/prof_new")
                .build();

        when(instructorService.uploadProfilePhoto(eq("instructor@test.com"), any()))
                .thenReturn(updated);

        mockMvcInstructor.perform(multipart("/api/instructors/me/photo")
                        .file(file)
                        .principal(instructorAuth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profilePhotoUrl").value("https://res.cloudinary.com/demo/image/upload/v1/prof_new.jpg"));
    }

    @Test
    void testRegisterUser_withRoleStudent_callsUserService() throws Exception {
        RegisterRequest registerReq = new RegisterRequest(
                "student1",
                "student1@test.com",
                "secret123",
                "Student One",
                "011223344",
                Roles.STUDENT
        );

        UserResponseDTO userResponse = UserResponseDTO.builder()
                .id(1L)
                .username("student1")
                .email("student1@test.com")
                .role("STUDENT")
                .build();

        when(userService.register(any(RegisterRequest.class))).thenReturn(userResponse);

        mockMvcUser.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("student1"))
                .andExpect(jsonPath("$.role").value("STUDENT"));
    }

    @Test
    void testChangePassword_callsUserService() throws Exception {
        ChangePasswordRequest changeReq = new ChangePasswordRequest("oldSecret", "newSecret123");

        mockMvcUser.perform(post("/api/users/change-password")
                        .principal(studentAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changeReq)))
                .andExpect(status().isOk())
                .andExpect(content().string("Password changed successfully"));

        verify(userService).changePassword(eq("student@test.com"), any(ChangePasswordRequest.class));
    }

    @Test
    void testGetSession_returnsSessionDetails() throws Exception {
        com.ly.lmsbackend.model.Users mockUser = new com.ly.lmsbackend.model.Users();
        mockUser.setId(10L);
        mockUser.setEmail("student@test.com");
        mockUser.setUsername("student10");
        mockUser.setRole(Roles.STUDENT);

        when(userRepository.findByEmail("student@test.com")).thenReturn(java.util.Optional.of(mockUser));

        mockMvcAuth.perform(get("/api/auth/session")
                        .principal(studentAuth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(true))
                .andExpect(jsonPath("$.email").value("student@test.com"))
                .andExpect(jsonPath("$.username").value("student10"))
                .andExpect(jsonPath("$.role").value("STUDENT"))
                .andExpect(jsonPath("$.expiresInSeconds").value(86400));
    }

    @Test
    void testLogout_logsActivityAndReturnsOk() throws Exception {
        com.ly.lmsbackend.model.Users mockUser = new com.ly.lmsbackend.model.Users();
        mockUser.setId(10L);
        mockUser.setEmail("student@test.com");

        when(userRepository.findByEmail("student@test.com")).thenReturn(java.util.Optional.of(mockUser));

        mockMvcAuth.perform(post("/api/auth/logout")
                        .principal(studentAuth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logged out successfully"))
                .andExpect(jsonPath("$.timestamp").isNumber());

        verify(activityLogService).logActivity(eq(mockUser), eq("USER_LOGOUT"), any(String.class));
    }
}
