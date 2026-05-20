package com.ly.lmsbackend.service;

import com.ly.lmsbackend.dto.EnrollmentAdminCreateDTO;
import com.ly.lmsbackend.dto.EnrollmentCreateDTO;
import com.ly.lmsbackend.dto.EnrollmentResponseDTO;
import com.ly.lmsbackend.mapper.EnrollmentMapper;
import com.ly.lmsbackend.model.Courses;
import com.ly.lmsbackend.model.EnrollmentStatus;
import com.ly.lmsbackend.model.Enrollments;
import com.ly.lmsbackend.model.Roles;
import com.ly.lmsbackend.model.Users;
import com.ly.lmsbackend.repository.CourseRepository;
import com.ly.lmsbackend.repository.EnrollmentRepository;
import com.ly.lmsbackend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class EnrollmentService {
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentMapper enrollmentMapper;

    public EnrollmentService(
            EnrollmentRepository enrollmentRepository,
            UserRepository userRepository,
            CourseRepository courseRepository,
            EnrollmentMapper enrollmentMapper
    ) {
        this.enrollmentRepository = enrollmentRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.enrollmentMapper = enrollmentMapper;
    }

    public EnrollmentResponseDTO enrollCurrentUser(EnrollmentCreateDTO dto, String email) {
        Users user = getUserByEmail(email);
        Courses course = getCourse(dto.courseId());

        Enrollments existingEnrollment = enrollmentRepository
                .findByUser_IdAndCourse_CourseId(user.getId(), course.getCourseId())
                .orElse(null);

        if (existingEnrollment != null) {
            if (existingEnrollment.getStatus() != EnrollmentStatus.CANCELLED) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "User is already enrolled in this course");
            }

            existingEnrollment.setStatus(EnrollmentStatus.ACTIVE);
            return enrollmentMapper.toDTO(enrollmentRepository.save(existingEnrollment));
        }

        Enrollments enrollment = new Enrollments();
        enrollment.setUser(user);
        enrollment.setCourse(course);
        enrollment.setStatus(EnrollmentStatus.ACTIVE);

        return enrollmentMapper.toDTO(enrollmentRepository.save(enrollment));
    }

    public EnrollmentResponseDTO enrollUser(EnrollmentAdminCreateDTO dto, String managerEmail) {
        Users user = getUser(dto.userId());
        Courses course = getCourse(dto.courseId());
        verifyCanManageCourse(managerEmail, course);

        Enrollments existingEnrollment = enrollmentRepository
                .findByUser_IdAndCourse_CourseId(user.getId(), course.getCourseId())
                .orElse(null);

        if (existingEnrollment != null) {
            if (existingEnrollment.getStatus() != EnrollmentStatus.CANCELLED) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "User is already enrolled in this course");
            }

            existingEnrollment.setStatus(dto.status() == null ? EnrollmentStatus.ACTIVE : dto.status());
            return enrollmentMapper.toDTO(enrollmentRepository.save(existingEnrollment));
        }

        Enrollments enrollment = new Enrollments();
        enrollment.setUser(user);
        enrollment.setCourse(course);
        enrollment.setStatus(dto.status() == null ? EnrollmentStatus.ACTIVE : dto.status());

        return enrollmentMapper.toDTO(enrollmentRepository.save(enrollment));
    }

    public List<EnrollmentResponseDTO> getAllEnrollments(String managerEmail) {
        Users manager = getUserByEmail(managerEmail);

        return enrollmentRepository.findAll()
                .stream()
                .filter(enrollment -> canManageCourse(manager, enrollment.getCourse()))
                .map(enrollmentMapper::toDTO)
                .toList();
    }

    public List<EnrollmentResponseDTO> getMyEnrollments(String email) {
        return enrollmentRepository.findByUser_Email(email)
                .stream()
                .map(enrollmentMapper::toDTO)
                .toList();
    }

    public List<EnrollmentResponseDTO> getCourseEnrollments(Long courseId, String managerEmail) {
        Courses course = getCourse(courseId);
        verifyCanManageCourse(managerEmail, course);

        return enrollmentRepository.findByCourse_CourseId(courseId)
                .stream()
                .map(enrollmentMapper::toDTO)
                .toList();
    }

    public EnrollmentResponseDTO updateStatus(Long enrollmentId, EnrollmentStatus status, String managerEmail) {
        Enrollments enrollment = getEnrollment(enrollmentId);
        verifyCanManageCourse(managerEmail, enrollment.getCourse());

        enrollment.setStatus(status);
        return enrollmentMapper.toDTO(enrollmentRepository.save(enrollment));
    }

    public void deleteEnrollment(Long enrollmentId, String managerEmail) {
        Enrollments enrollment = getEnrollment(enrollmentId);
        verifyCanManageCourse(managerEmail, enrollment.getCourse());
        enrollmentRepository.delete(enrollment);
    }

    public void cancelCurrentUserEnrollment(Long courseId, String email) {
        Enrollments enrollment = enrollmentRepository.findByUser_EmailAndCourse_CourseId(email, courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Enrollment not found"));

        enrollment.setStatus(EnrollmentStatus.CANCELLED);
        enrollmentRepository.save(enrollment);
    }

    private Enrollments getEnrollment(Long enrollmentId) {
        return enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Enrollment not found"));
    }

    private Users getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private Users getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private Courses getCourse(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
    }

    private void verifyCanManageCourse(String managerEmail, Courses course) {
        Users manager = getUserByEmail(managerEmail);
        if (!canManageCourse(manager, course)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot manage enrollments for this course");
        }
    }

    private boolean canManageCourse(Users manager, Courses course) {
        if (manager.getRole() == Roles.ADMIN) {
            return true;
        }

        return manager.getRole() == Roles.INSTRUCTOR
                && course.getInstructor() != null
                && course.getInstructor().getId().equals(manager.getId());
    }
}
