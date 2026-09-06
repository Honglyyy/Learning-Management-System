package com.ly.lmsbackend.service;

import com.ly.lmsbackend.dto.assignmentdtos.*;
import com.ly.lmsbackend.model.*;
import com.ly.lmsbackend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Service
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmissionRepository submissionRepository;
    private final CourseRepository courseRepository;
    private final SectionRepository sectionRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final FileUploadService fileUploadService;
    private final StudentPointsService studentPointsService;
    private final NotificationService notificationService;
    private final ActivityLogService activityLogService;
    private final ProgressService progressService;

    public AssignmentService(
            AssignmentRepository assignmentRepository,
            AssignmentSubmissionRepository submissionRepository,
            CourseRepository courseRepository,
            SectionRepository sectionRepository,
            UserRepository userRepository,
            StudentRepository studentRepository,
            EnrollmentRepository enrollmentRepository,
            FileUploadService fileUploadService
    ) {
        this(assignmentRepository, submissionRepository, courseRepository, sectionRepository,
                userRepository, studentRepository, enrollmentRepository, fileUploadService,
                null, null, null, null);
    }

    @Autowired
    public AssignmentService(
            AssignmentRepository assignmentRepository,
            AssignmentSubmissionRepository submissionRepository,
            CourseRepository courseRepository,
            SectionRepository sectionRepository,
            UserRepository userRepository,
            StudentRepository studentRepository,
            EnrollmentRepository enrollmentRepository,
            FileUploadService fileUploadService,
            StudentPointsService studentPointsService,
            NotificationService notificationService,
            ActivityLogService activityLogService,
            ProgressService progressService
    ) {
        this.assignmentRepository = assignmentRepository;
        this.submissionRepository = submissionRepository;
        this.courseRepository = courseRepository;
        this.sectionRepository = sectionRepository;
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.fileUploadService = fileUploadService;
        this.studentPointsService = studentPointsService;
        this.notificationService = notificationService;
        this.activityLogService = activityLogService;
        this.progressService = progressService;
    }

    @Transactional
    public AssignmentResponseDTO createAssignment(AssignmentCreateDTO dto, String userEmail) {
        if (dto.courseId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Course ID is required");
        }
        if (dto.title() == null || dto.title().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assignment title is required");
        }

        Users user = getUserByEmail(userEmail);
        Courses course = courseRepository.findById(dto.courseId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found with id: " + dto.courseId()));

        verifyCanManageCourse(course, user);

        Sections section = null;
        if (dto.sectionId() != null) {
            section = sectionRepository.findById(dto.sectionId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Section not found with id: " + dto.sectionId()));
            if (section.getCourse() == null || !section.getCourse().getCourseId().equals(course.getCourseId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Section does not belong to the specified course");
            }
        }

        Assignment assignment = Assignment.builder()
                .course(course)
                .section(section)
                .instructor(user)
                .title(dto.title().trim())
                .description(dto.description())
                .instructions(dto.instructions())
                .startDate(dto.startDate())
                .dueDate(dto.dueDate())
                .maxScore(dto.maxScore() != null && dto.maxScore() > 0 ? dto.maxScore() : 100.0)
                .supportingFileUrl(dto.supportingFileUrl())
                .supportingFilePublicId(dto.supportingFilePublicId())
                .allowResubmission(dto.allowResubmission() != null ? dto.allowResubmission() : true)
                .build();

        Assignment saved = assignmentRepository.save(assignment);
        return toResponseDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<AssignmentResponseDTO> getCourseAssignments(Long courseId, String userEmail) {
        Courses course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found with id: " + courseId));

        if (userEmail != null) {
            Users user = userRepository.findByEmail(userEmail).orElse(null);
            if (user != null && (user.getRole() == Roles.STUDENT || user.getRole() == Roles.USER)) {
                verifyStudentEnrolledInCourse(user, courseId);
            }
        }

        return assignmentRepository.findByCourse_CourseIdOrderByCreatedAtDesc(courseId)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public AssignmentResponseDTO getAssignmentById(Long assignmentId, String userEmail) {
        Assignment assignment = getAssignment(assignmentId);

        if (userEmail != null) {
            Users user = userRepository.findByEmail(userEmail).orElse(null);
            if (user != null && (user.getRole() == Roles.STUDENT || user.getRole() == Roles.USER)) {
                verifyStudentEnrolledInCourse(user, assignment.getCourse().getCourseId());
            }
        }

        return toResponseDTO(assignment);
    }

    @Transactional
    public AssignmentResponseDTO updateAssignment(Long assignmentId, AssignmentUpdateDTO dto, String userEmail) {
        Assignment assignment = getAssignment(assignmentId);
        Users user = getUserByEmail(userEmail);
        verifyCanManageAssignment(assignment, user);

        if (dto.title() != null && !dto.title().isBlank()) {
            assignment.setTitle(dto.title().trim());
        }
        if (dto.description() != null) {
            assignment.setDescription(dto.description());
        }
        if (dto.instructions() != null) {
            assignment.setInstructions(dto.instructions());
        }
        if (dto.startDate() != null) {
            assignment.setStartDate(dto.startDate());
        }
        if (dto.dueDate() != null) {
            assignment.setDueDate(dto.dueDate());
        }
        if (dto.maxScore() != null && dto.maxScore() > 0) {
            assignment.setMaxScore(dto.maxScore());
        }
        if (dto.supportingFileUrl() != null) {
            assignment.setSupportingFileUrl(dto.supportingFileUrl());
        }
        if (dto.supportingFilePublicId() != null) {
            assignment.setSupportingFilePublicId(dto.supportingFilePublicId());
        }
        if (dto.allowResubmission() != null) {
            assignment.setAllowResubmission(dto.allowResubmission());
        }
        if (dto.sectionId() != null) {
            Sections section = sectionRepository.findById(dto.sectionId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Section not found with id: " + dto.sectionId()));
            if (section.getCourse() == null || !section.getCourse().getCourseId().equals(assignment.getCourse().getCourseId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Section does not belong to the assignment course");
            }
            assignment.setSection(section);
        }

        Assignment updated = assignmentRepository.save(assignment);
        return toResponseDTO(updated);
    }

    @Transactional
    public void deleteAssignment(Long assignmentId, String userEmail) {
        Assignment assignment = getAssignment(assignmentId);
        Users user = getUserByEmail(userEmail);
        verifyCanManageAssignment(assignment, user);

        if (assignment.getSupportingFilePublicId() != null && !assignment.getSupportingFilePublicId().isBlank()) {
            fileUploadService.deleteAsset(assignment.getSupportingFilePublicId());
        }

        assignmentRepository.delete(assignment);
    }

    @Transactional
    public AssignmentSubmissionResponseDTO submitAssignment(Long assignmentId, AssignmentSubmitDTO dto, String userEmail) {
        Assignment assignment = getAssignment(assignmentId);
        Users user = getUserByEmail(userEmail);

        if (user.getRole() != Roles.ADMIN) {
            verifyStudentEnrolledInCourse(user, assignment.getCourse().getCourseId());
        }

        boolean hasText = dto.textSubmission() != null && !dto.textSubmission().isBlank();
        boolean hasFile = dto.fileUrl() != null && !dto.fileUrl().isBlank();
        if (!hasText && !hasFile) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Submission must contain text or a file URL");
        }

        Timestamp now = new Timestamp(System.currentTimeMillis());
        AssignmentStatus initialStatus = (assignment.getDueDate() != null && now.after(assignment.getDueDate()))
                ? AssignmentStatus.LATE
                : AssignmentStatus.SUBMITTED;

        Optional<AssignmentSubmission> existingOpt = submissionRepository
                .findByAssignment_AssignmentIdAndStudent_Id(assignmentId, user.getId());

        AssignmentSubmission submission;
        if (existingOpt.isPresent()) {
            submission = existingOpt.get();
            if (submission.getStatus() == AssignmentStatus.GRADED || submission.getScore() != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot edit or resubmit an assignment that has already been graded");
            }
            if (Boolean.FALSE.equals(assignment.getAllowResubmission())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Resubmission is not allowed for this assignment");
            }
            if (submission.getFilePublicId() != null && !submission.getFilePublicId().equals(dto.filePublicId())) {
                fileUploadService.deleteAsset(submission.getFilePublicId());
            }
            submission.setTextSubmission(dto.textSubmission());
            submission.setFileUrl(dto.fileUrl());
            submission.setFilePublicId(dto.filePublicId());
            submission.setSubmittedAt(now);
            submission.setStatus(initialStatus);
            submission.setScore(null);
            submission.setGrade(null);
            submission.setFeedback(null);
            submission.setGradedBy(null);
            submission.setGradedAt(null);
        } else {
            submission = AssignmentSubmission.builder()
                    .assignment(assignment)
                    .student(user)
                    .textSubmission(dto.textSubmission())
                    .fileUrl(dto.fileUrl())
                    .filePublicId(dto.filePublicId())
                    .submittedAt(now)
                    .status(initialStatus)
                    .build();
        }

        AssignmentSubmission saved = submissionRepository.save(submission);

        if (activityLogService != null) {
            activityLogService.logActivity(
                    user,
                    "ASSIGNMENT_SUBMITTED",
                    "Submitted assignment: " + assignment.getTitle() + " for course " + assignment.getCourse().getTitle()
            );
        }

        if (progressService != null) {
            progressService.checkCourseCompletion(user, assignment.getCourse());
        }

        return toSubmissionDTO(saved);
    }

    @Transactional(readOnly = true)
    public AssignmentSubmissionResponseDTO getMySubmission(Long assignmentId, String userEmail) {
        Assignment assignment = getAssignment(assignmentId);
        Users user = getUserByEmail(userEmail);

        if (user.getRole() != Roles.ADMIN) {
            verifyStudentEnrolledInCourse(user, assignment.getCourse().getCourseId());
        }

        return submissionRepository.findByAssignment_AssignmentIdAndStudent_Id(assignmentId, user.getId())
                .map(this::toSubmissionDTO)
                .orElse(new AssignmentSubmissionResponseDTO(
                        null,
                        assignmentId,
                        user.getId(),
                        user.getStudent() != null ? user.getStudent().getFullName() : user.getFullname(),
                        user.getEmail(),
                        null,
                        null,
                        null,
                        null,
                        AssignmentStatus.NOT_SUBMITTED,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                ));
    }

    @Transactional(readOnly = true)
    public List<AssignmentSubmissionResponseDTO> getAssignmentSubmissions(Long assignmentId, String userEmail) {
        Assignment assignment = getAssignment(assignmentId);
        Users user = getUserByEmail(userEmail);
        verifyCanManageAssignment(assignment, user);

        return submissionRepository.findByAssignment_AssignmentIdOrderBySubmittedAtDesc(assignmentId)
                .stream()
                .map(this::toSubmissionDTO)
                .toList();
    }

    @Transactional
    public AssignmentSubmissionResponseDTO gradeSubmission(Long submissionId, AssignmentGradeDTO dto, String userEmail) {
        AssignmentSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Submission not found with id: " + submissionId));

        Users grader = getUserByEmail(userEmail);
        verifyCanManageAssignment(submission.getAssignment(), grader);

        Double maxScore = submission.getAssignment().getMaxScore() != null ? submission.getAssignment().getMaxScore() : 100.0;
        if (dto.score() != null) {
            if (dto.score() < 0 || dto.score() > maxScore) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Score must be between 0 and " + maxScore);
            }
        }

        String calculatedGrade = null;
        if (dto.grade() != null && !dto.grade().isBlank()) {
            calculatedGrade = dto.grade().trim().toUpperCase();
        } else if (dto.score() != null && maxScore > 0) {
            double pct = (dto.score() / maxScore) * 100.0;
            if (pct >= 90.0) calculatedGrade = "A";
            else if (pct >= 80.0) calculatedGrade = "B";
            else if (pct >= 70.0) calculatedGrade = "C";
            else if (pct >= 60.0) calculatedGrade = "D";
            else calculatedGrade = "F";
        }

        submission.setScore(dto.score());
        submission.setGrade(calculatedGrade);
        submission.setFeedback(dto.feedback());
        submission.setGradedBy(grader);
        submission.setGradedAt(new Timestamp(System.currentTimeMillis()));

        if (dto.score() != null || (calculatedGrade != null && !calculatedGrade.isBlank())) {
            submission.setStatus(AssignmentStatus.GRADED);
        } else {
            submission.setStatus(AssignmentStatus.REVIEWED);
        }

        AssignmentSubmission saved = submissionRepository.save(submission);

        // Sync points
        if (studentPointsService != null) {
            studentPointsService.recalculateCourseAndStudentPoints(submission.getStudent(), submission.getAssignment().getCourse());
        }

        // Notify student
        String scoreDisplay = submission.getScore() != null ? String.valueOf(submission.getScore()) : "Reviewed";
        if (notificationService != null) {
            notificationService.sendNotification(
                    submission.getStudent(),
                    "Assignment Graded",
                    "Your submission for '" + submission.getAssignment().getTitle() + "' has been evaluated. Score: " + scoreDisplay + (submission.getGrade() != null ? " (" + submission.getGrade() + ")" : ""),
                    "ASSIGNMENT",
                    "/api/assignments/" + submission.getAssignment().getAssignmentId()
            );
        }

        if (activityLogService != null) {
            activityLogService.logActivity(
                    submission.getStudent(),
                    "ASSIGNMENT_GRADED",
                    "Assignment graded: " + submission.getAssignment().getTitle() + " (Score: " + scoreDisplay + ")"
            );
        }

        if (progressService != null) {
            progressService.checkCourseCompletion(submission.getStudent(), submission.getAssignment().getCourse());
        }

        return toSubmissionDTO(saved);
    }

    private Assignment getAssignment(Long assignmentId) {
        return assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment not found with id: " + assignmentId));
    }

    private Users getUserByEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private void verifyCanManageCourse(Courses course, Users user) {
        if (user.getRole() == Roles.ADMIN) {
            return;
        }
        Users courseInstructor = course.getInstructor();
        if (courseInstructor == null || !courseInstructor.getEmail().equals(user.getEmail())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to manage assignments for this course");
        }
    }

    private void verifyCanManageAssignment(Assignment assignment, Users user) {
        if (user.getRole() == Roles.ADMIN) {
            return;
        }
        Users assignInstructor = assignment.getInstructor();
        Users courseInstructor = assignment.getCourse() != null ? assignment.getCourse().getInstructor() : null;

        boolean isAssignInstructor = assignInstructor != null && assignInstructor.getEmail().equals(user.getEmail());
        boolean isCourseInstructor = courseInstructor != null && courseInstructor.getEmail().equals(user.getEmail());

        if (!isAssignInstructor && !isCourseInstructor) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to manage this assignment");
        }
    }

    private void verifyStudentEnrolledInCourse(Users user, Long courseId) {
        boolean isEnrolled = enrollmentRepository.findByUser_IdAndCourse_CourseId(user.getId(), courseId)
                .map(enrollment -> enrollment.getStatus() == EnrollmentStatus.ACTIVE)
                .orElse(false);

        if (!isEnrolled) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: student is not actively enrolled in this course");
        }
    }

    public AssignmentResponseDTO toResponseDTO(Assignment a) {
        return new AssignmentResponseDTO(
                a.getAssignmentId(),
                a.getCourse() != null ? a.getCourse().getCourseId() : null,
                a.getCourse() != null ? a.getCourse().getTitle() : null,
                a.getSection() != null ? a.getSection().getSectionId() : null,
                a.getSection() != null ? a.getSection().getTitle() : null,
                a.getTitle(),
                a.getDescription(),
                a.getInstructions(),
                a.getStartDate(),
                a.getDueDate(),
                a.getMaxScore(),
                a.getSupportingFileUrl(),
                a.getSupportingFilePublicId(),
                a.getAllowResubmission(),
                a.getInstructor() != null ? a.getInstructor().getId() : null,
                a.getInstructor() != null ? (a.getInstructor().getStudent() != null ? a.getInstructor().getStudent().getFullName() : a.getInstructor().getFullname()) : null,
                a.getCreatedAt(),
                a.getUpdatedAt()
        );
    }

    public AssignmentSubmissionResponseDTO toSubmissionDTO(AssignmentSubmission s) {
        Users student = s.getStudent();
        String studentName = null;
        if (student != null) {
            if (student.getStudent() != null && student.getStudent().getFullName() != null) {
                studentName = student.getStudent().getFullName();
            } else {
                studentName = student.getFullname();
            }
        }
        Users grader = s.getGradedBy();
        String graderName = null;
        if (grader != null) {
            if (grader.getInstructor() != null && grader.getInstructor().getFullName() != null) {
                graderName = grader.getInstructor().getFullName();
            } else {
                graderName = grader.getFullname();
            }
        }

        return new AssignmentSubmissionResponseDTO(
                s.getSubmissionId(),
                s.getAssignment() != null ? s.getAssignment().getAssignmentId() : null,
                student != null ? student.getId() : null,
                studentName,
                student != null ? student.getEmail() : null,
                s.getTextSubmission(),
                s.getFileUrl(),
                s.getFilePublicId(),
                s.getSubmittedAt(),
                s.getStatus(),
                s.getScore(),
                s.getGrade(),
                s.getFeedback(),
                grader != null ? grader.getId() : null,
                graderName,
                s.getGradedAt()
        );
    }
}
