package com.ly.lmsbackend.service;

import com.ly.lmsbackend.dto.enrollmentdtos.EnrollmentAdminCreateDTO;
import com.ly.lmsbackend.dto.enrollmentdtos.EnrollmentCreateDTO;
import com.ly.lmsbackend.dto.enrollmentdtos.EnrollmentResponseDTO;
import com.ly.lmsbackend.mapper.EnrollmentMapper;
import com.ly.lmsbackend.model.Courses;
import com.ly.lmsbackend.model.EnrollmentStatus;
import com.ly.lmsbackend.model.Enrollments;
import com.ly.lmsbackend.model.PaymentStatus;
import com.ly.lmsbackend.model.Payments;
import com.ly.lmsbackend.model.Roles;
import com.ly.lmsbackend.model.Users;
import com.ly.lmsbackend.repository.CourseRepository;
import com.ly.lmsbackend.repository.EnrollmentRepository;
import com.ly.lmsbackend.repository.PaymentRepository;
import com.ly.lmsbackend.repository.QuizAttemptRepository;
import com.ly.lmsbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

@Service
public class EnrollmentService {
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentMapper enrollmentMapper;
    private final QuizAttemptRepository quizAttemptRepository;
    private final ActivityLogService activityLogService;
    private final NotificationService notificationService;
    private final PaymentRepository paymentRepository;

    public EnrollmentService(
            EnrollmentRepository enrollmentRepository,
            UserRepository userRepository,
            CourseRepository courseRepository,
            EnrollmentMapper enrollmentMapper,
            QuizAttemptRepository quizAttemptRepository
    ) {
        this(enrollmentRepository, userRepository, courseRepository, enrollmentMapper, quizAttemptRepository, null, null, null);
    }

    public EnrollmentService(
            EnrollmentRepository enrollmentRepository,
            UserRepository userRepository,
            CourseRepository courseRepository,
            EnrollmentMapper enrollmentMapper,
            QuizAttemptRepository quizAttemptRepository,
            ActivityLogService activityLogService,
            NotificationService notificationService
    ) {
        this(enrollmentRepository, userRepository, courseRepository, enrollmentMapper, quizAttemptRepository, activityLogService, notificationService, null);
    }

    @Autowired
    public EnrollmentService(
            EnrollmentRepository enrollmentRepository,
            UserRepository userRepository,
            CourseRepository courseRepository,
            EnrollmentMapper enrollmentMapper,
            QuizAttemptRepository quizAttemptRepository,
            ActivityLogService activityLogService,
            NotificationService notificationService,
            PaymentRepository paymentRepository
    ) {
        this.enrollmentRepository = enrollmentRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.enrollmentMapper = enrollmentMapper;
        this.quizAttemptRepository = quizAttemptRepository;
        this.activityLogService = activityLogService;
        this.notificationService = notificationService;
        this.paymentRepository = paymentRepository;
    }

    public EnrollmentResponseDTO enrollCurrentUser(EnrollmentCreateDTO dto, String email) {
        Users user = getUserByEmail(email);
        Courses course = getCourse(dto.courseId());

        int durationDays = course.getAccessDurationDays() != null && course.getAccessDurationDays() > 0
                ? course.getAccessDurationDays()
                : 180;
        Timestamp expirationDate = new Timestamp(System.currentTimeMillis() + (long) durationDays * 24L * 60L * 60L * 1000L);

        Enrollments existingEnrollment = enrollmentRepository
                .findByUser_IdAndCourse_CourseId(user.getId(), course.getCourseId())
                .orElse(null);

        boolean isExpiredOrCancelled = existingEnrollment != null &&
                (existingEnrollment.getStatus() == EnrollmentStatus.CANCELLED || existingEnrollment.isExpired());

        if (existingEnrollment != null && !isExpiredOrCancelled) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User is already actively enrolled in this course");
        }

        if (course.getPrice() != null && course.getPrice().compareTo(BigDecimal.ZERO) > 0) {
            boolean hasPaid = false;
            if (paymentRepository != null) {
                var paymentOpt = paymentRepository.findFirstByUser_EmailAndCourse_CourseIdAndStatusOrderByCreatedAtDesc(
                        email, course.getCourseId(), PaymentStatus.PAID
                );
                if (paymentOpt.isPresent()) {
                    Payments latestPaid = paymentOpt.get();
                    if (existingEnrollment != null && existingEnrollment.isExpired()) {
                        Timestamp cutoff = existingEnrollment.getExpirationDate() != null
                                ? existingEnrollment.getExpirationDate()
                                : existingEnrollment.getEnrolledAt();
                        hasPaid = latestPaid.getCreatedAt() != null &&
                                (cutoff == null || latestPaid.getCreatedAt().after(cutoff) || latestPaid.getCreatedAt().equals(cutoff)
                                        || Boolean.TRUE.equals(latestPaid.getIsReEnrollmentDiscount()));
                    } else {
                        hasPaid = true;
                    }
                }
            }

            if (!hasPaid) {
                throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED, "Payment required to enroll in this course");
            }
        }

        if (existingEnrollment != null) {
            existingEnrollment.setStatus(EnrollmentStatus.ACTIVE);
            existingEnrollment.setExpirationDate(expirationDate);
            existingEnrollment.setEnrolledAt(new Timestamp(System.currentTimeMillis()));
            Enrollments saved = enrollmentRepository.save(existingEnrollment);
            if (activityLogService != null) {
                activityLogService.logActivity(user, "ENROLLED", "Re-enrolled in course: " + course.getTitle() + " (Access valid for " + durationDays + " days)");
            }
            if (notificationService != null) {
                notificationService.sendNotification(user, "Course Enrollment", "You have re-enrolled in " + course.getTitle() + ". Your access is active until " + expirationDate, "COURSE", "/api/courses/" + course.getCourseId());
            }
            return enrollmentMapper.toDTO(saved);
        }

        Enrollments enrollment = new Enrollments();
        enrollment.setUser(user);
        enrollment.setCourse(course);
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        enrollment.setExpirationDate(expirationDate);

        Enrollments saved = enrollmentRepository.save(enrollment);
        if (activityLogService != null) {
            activityLogService.logActivity(user, "ENROLLED", "Enrolled in course: " + course.getTitle() + " (Access valid for " + durationDays + " days)");
        }
        if (notificationService != null) {
            notificationService.sendNotification(user, "Welcome to the Course!", "You are now enrolled in " + course.getTitle() + ". Start learning today! Access valid until " + expirationDate, "COURSE", "/api/courses/" + course.getCourseId());
        }

        return enrollmentMapper.toDTO(saved);
    }

    public EnrollmentResponseDTO enrollUser(EnrollmentAdminCreateDTO dto, String managerEmail) {
        Users user = getUser(dto.userId());
        Courses course = getCourse(dto.courseId());
        verifyCanManageCourse(managerEmail, course);

        int durationDays = course.getAccessDurationDays() != null && course.getAccessDurationDays() > 0
                ? course.getAccessDurationDays()
                : 180;
        Timestamp expirationDate = new Timestamp(System.currentTimeMillis() + (long) durationDays * 24L * 60L * 60L * 1000L);

        Enrollments existingEnrollment = enrollmentRepository
                .findByUser_IdAndCourse_CourseId(user.getId(), course.getCourseId())
                .orElse(null);

        if (existingEnrollment != null) {
            boolean isExpiredOrCancelled = existingEnrollment.getStatus() == EnrollmentStatus.CANCELLED || existingEnrollment.isExpired();
            if (!isExpiredOrCancelled && dto.status() == null) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "User is already actively enrolled in this course");
            }

            existingEnrollment.setStatus(dto.status() == null ? EnrollmentStatus.ACTIVE : dto.status());
            existingEnrollment.setExpirationDate(expirationDate);
            return enrollmentMapper.toDTO(enrollmentRepository.save(existingEnrollment));
        }

        Enrollments enrollment = new Enrollments();
        enrollment.setUser(user);
        enrollment.setCourse(course);
        enrollment.setStatus(dto.status() == null ? EnrollmentStatus.ACTIVE : dto.status());
        enrollment.setExpirationDate(expirationDate);

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

    @Transactional
    public void deleteEnrollment(Long enrollmentId, String managerEmail) {
        Enrollments enrollment = getEnrollment(enrollmentId);
        verifyCanManageCourse(managerEmail, enrollment.getCourse());
        quizAttemptRepository.deleteAllByEnrollmentId(enrollmentId);
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
