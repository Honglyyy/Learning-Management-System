package com.ly.lmsbackend.service;

import com.ly.lmsbackend.dto.filedtos.FileUploadResponseDTO;
import com.ly.lmsbackend.dto.studentdtos.StudentProfileResponseDTO;
import com.ly.lmsbackend.dto.studentdtos.StudentProfileUpdateDTO;
import com.ly.lmsbackend.model.Genders;
import com.ly.lmsbackend.model.Students;
import com.ly.lmsbackend.model.Users;
import com.ly.lmsbackend.repository.StudentRepository;
import com.ly.lmsbackend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.Year;

@Service
@RequiredArgsConstructor
public class StudentService {
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final FileUploadService fileUploadService;

    @Transactional
    public StudentProfileResponseDTO getProfile(String email) {
        Users user = getUserByEmail(email);
        Students student = studentRepository.findByUser_Id(user.getId())
                .orElseGet(() -> createDefaultStudentProfile(user));

        return toDTO(student);
    }

    @Transactional
    public StudentProfileResponseDTO updateProfile(String email, StudentProfileUpdateDTO dto) {
        Users user = getUserByEmail(email);
        Students student = studentRepository.findByUser_Id(user.getId())
                .orElseGet(() -> createDefaultStudentProfile(user));

        student.setFullName(dto.fullName());
        student.setPhoneNumber(dto.phoneNumber());
        if (dto.gender() != null) {
            student.setGender(dto.gender());
        }
        student.setDateOfBirth(dto.dateOfBirth());
        student.setEducationLevel(dto.educationLevel());

        if (dto.profilePhotoUrl() != null && !dto.profilePhotoUrl().isBlank()) {
            if (student.getProfilePhotoPublicId() != null
                    && !student.getProfilePhotoPublicId().equals(dto.profilePhotoPublicId())) {
                fileUploadService.deleteAsset(student.getProfilePhotoPublicId());
            }
            student.setProfilePhotoUrl(dto.profilePhotoUrl());
            student.setProfilePhotoPublicId(dto.profilePhotoPublicId());
        }

        user.setFullname(dto.fullName());
        user.setPhoneNumber(dto.phoneNumber());
        userRepository.save(user);

        Students saved = studentRepository.save(student);
        return toDTO(saved);
    }

    @Transactional
    public StudentProfileResponseDTO uploadProfilePhoto(String email, MultipartFile file) {
        Users user = getUserByEmail(email);
        Students student = studentRepository.findByUser_Id(user.getId())
                .orElseGet(() -> createDefaultStudentProfile(user));

        if (student.getProfilePhotoPublicId() != null && !student.getProfilePhotoPublicId().isBlank()) {
            fileUploadService.deleteAsset(student.getProfilePhotoPublicId());
        }

        FileUploadResponseDTO uploadResponse = fileUploadService.uploadStudentPhoto(file);
        student.setProfilePhotoUrl(uploadResponse.url());
        student.setProfilePhotoPublicId(uploadResponse.publicId());

        Students saved = studentRepository.save(student);
        return toDTO(saved);
    }

    public Students createDefaultStudentProfile(Users user) {
        String studentCode = generateStudentCode();
        Students student = Students.builder()
                .user(user)
                .studentCode(studentCode)
                .fullName(user.getFullname() != null ? user.getFullname() : user.getUsername())
                .phoneNumber(user.getPhoneNumber())
                .gender(Genders.NOT_SPECIFIC)
                .build();
        return studentRepository.save(student);
    }

    public String generateStudentCode() {
        int year = Year.now().getValue();
        long count = studentRepository.count() + 1;
        String code;
        do {
            code = String.format("STU-%d-%04d", year, count++);
        } while (studentRepository.existsByStudentCode(code));
        return code;
    }

    private Users getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private StudentProfileResponseDTO toDTO(Students student) {
        Users user = student.getUser();
        return StudentProfileResponseDTO.builder()
                .studentId(student.getId())
                .studentCode(student.getStudentCode())
                .username(user != null ? user.getUsername() : null)
                .email(user != null ? user.getEmail() : null)
                .fullName(student.getFullName())
                .phoneNumber(student.getPhoneNumber())
                .gender(student.getGender())
                .dateOfBirth(student.getDateOfBirth())
                .educationLevel(student.getEducationLevel())
                .profilePhotoUrl(student.getProfilePhotoUrl())
                .profilePhotoPublicId(student.getProfilePhotoPublicId())
                .totalPoints(student.getTotalPoints() != null ? student.getTotalPoints() : 0.0)
                .createdAt(student.getCreatedAt())
                .updatedAt(student.getUpdatedAt())
                .build();
    }
}
