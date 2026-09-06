package com.ly.lmsbackend.service;

import com.ly.lmsbackend.dto.coursedtos.CourseResponseDTO;
import com.ly.lmsbackend.dto.filedtos.FileUploadResponseDTO;
import com.ly.lmsbackend.dto.instructordtos.InstructorProfileUpdateDTO;
import com.ly.lmsbackend.dto.instructordtos.InstructorResponseDTO;
import com.ly.lmsbackend.mapper.CourseMapper;
import com.ly.lmsbackend.model.Courses;
import com.ly.lmsbackend.model.Instructors;
import com.ly.lmsbackend.model.Users;
import com.ly.lmsbackend.repository.CourseRepository;
import com.ly.lmsbackend.repository.InstructorRepository;
import com.ly.lmsbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InstructorService {
    private final InstructorRepository instructorRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;
    private final FileUploadService fileUploadService;

    public List<InstructorResponseDTO> getAllInstructors() {
        return instructorRepository.findAll().stream()
                .map(instructor -> {
                    List<Courses> courses = getCoursesForInstructor(instructor);
                    return toDTO(instructor, courses);
                })
                .toList();
    }

    public InstructorResponseDTO getInstructorById(Long id) {
        Instructors instructor = instructorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Instructor not found"));

        List<Courses> courses = getCoursesForInstructor(instructor);
        return toDTO(instructor, courses);
    }

    @Transactional
    public InstructorResponseDTO getMyProfile(String email) {
        Users user = getUserByEmail(email);
        Instructors instructor = instructorRepository.findByUser_Id(user.getId())
                .orElseGet(() -> createDefaultInstructorProfile(user));

        List<Courses> courses = getCoursesForInstructor(instructor);
        return toDTO(instructor, courses);
    }

    @Transactional
    public InstructorResponseDTO updateMyProfile(String email, InstructorProfileUpdateDTO dto) {
        Users user = getUserByEmail(email);
        Instructors instructor = instructorRepository.findByUser_Id(user.getId())
                .orElseGet(() -> createDefaultInstructorProfile(user));

        instructor.setFullName(dto.fullName());
        instructor.setPhoneNumber(dto.phoneNumber());
        instructor.setBiography(dto.biography());
        instructor.setExpertise(dto.expertise());

        if (dto.profilePhotoUrl() != null && !dto.profilePhotoUrl().isBlank()) {
            if (instructor.getProfilePhotoPublicId() != null
                    && !instructor.getProfilePhotoPublicId().equals(dto.profilePhotoPublicId())) {
                fileUploadService.deleteAsset(instructor.getProfilePhotoPublicId());
            }
            instructor.setProfilePhotoUrl(dto.profilePhotoUrl());
            instructor.setProfilePhotoPublicId(dto.profilePhotoPublicId());
        }

        user.setFullname(dto.fullName());
        user.setPhoneNumber(dto.phoneNumber());
        userRepository.save(user);

        Instructors saved = instructorRepository.save(instructor);
        List<Courses> courses = getCoursesForInstructor(saved);
        return toDTO(saved, courses);
    }

    @Transactional
    public InstructorResponseDTO uploadProfilePhoto(String email, MultipartFile file) {
        Users user = getUserByEmail(email);
        Instructors instructor = instructorRepository.findByUser_Id(user.getId())
                .orElseGet(() -> createDefaultInstructorProfile(user));

        if (instructor.getProfilePhotoPublicId() != null && !instructor.getProfilePhotoPublicId().isBlank()) {
            fileUploadService.deleteAsset(instructor.getProfilePhotoPublicId());
        }

        FileUploadResponseDTO uploadResponse = fileUploadService.uploadInstructorPhoto(file);
        instructor.setProfilePhotoUrl(uploadResponse.url());
        instructor.setProfilePhotoPublicId(uploadResponse.publicId());

        Instructors saved = instructorRepository.save(instructor);
        List<Courses> courses = getCoursesForInstructor(saved);
        return toDTO(saved, courses);
    }

    public Instructors createDefaultInstructorProfile(Users user) {
        Instructors instructor = Instructors.builder()
                .user(user)
                .fullName(user.getFullname() != null ? user.getFullname() : user.getUsername())
                .phoneNumber(user.getPhoneNumber())
                .averageRating(5.0)
                .build();
        return instructorRepository.save(instructor);
    }

    private List<Courses> getCoursesForInstructor(Instructors instructor) {
        if (instructor.getUser() == null || instructor.getUser().getId() == null) {
            return Collections.emptyList();
        }
        return courseRepository.findByInstructor_Id(instructor.getUser().getId());
    }

    private Users getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private InstructorResponseDTO toDTO(Instructors instructor, List<Courses> courses) {
        Users user = instructor.getUser();
        List<CourseResponseDTO> courseDTOs = (courses == null) ? Collections.emptyList() :
                courses.stream().map(courseMapper::toDTO).toList();

        return InstructorResponseDTO.builder()
                .instructorId(instructor.getId())
                .userId(user != null ? user.getId() : null)
                .username(user != null ? user.getUsername() : null)
                .email(user != null ? user.getEmail() : null)
                .fullName(instructor.getFullName())
                .phoneNumber(instructor.getPhoneNumber())
                .profilePhotoUrl(instructor.getProfilePhotoUrl())
                .profilePhotoPublicId(instructor.getProfilePhotoPublicId())
                .biography(instructor.getBiography())
                .expertise(instructor.getExpertise())
                .averageRating(instructor.getAverageRating() != null ? instructor.getAverageRating() : 5.0)
                .totalCourses(courseDTOs.size())
                .courses(courseDTOs)
                .createdAt(instructor.getCreatedAt())
                .updatedAt(instructor.getUpdatedAt())
                .build();
    }
}
