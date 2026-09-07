package com.ly.lmsbackend.service;

import com.ly.lmsbackend.dto.certificatedtos.CertificateResponseDTO;
import com.ly.lmsbackend.dto.certificatedtos.CertificateVerifyDTO;
import com.ly.lmsbackend.dto.progressdtos.CourseProgressDTO;
import com.ly.lmsbackend.model.*;
import com.ly.lmsbackend.repository.CertificateRepository;
import com.ly.lmsbackend.repository.CourseRepository;
import com.ly.lmsbackend.repository.EnrollmentRepository;
import com.ly.lmsbackend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final ProgressService progressService;
    private final NotificationService notificationService;
    private final ActivityLogService activityLogService;

    public CertificateService(
            CertificateRepository certificateRepository,
            CourseRepository courseRepository,
            EnrollmentRepository enrollmentRepository,
            UserRepository userRepository,
            ProgressService progressService,
            NotificationService notificationService,
            ActivityLogService activityLogService
    ) {
        this.certificateRepository = certificateRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.userRepository = userRepository;
        this.progressService = progressService;
        this.notificationService = notificationService;
        this.activityLogService = activityLogService;
    }

    @Transactional
    public CertificateResponseDTO claimCertificate(Long courseId, String email) {
        Users user = getUserByEmail(email);
        Courses course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));

        Enrollments enrollment = enrollmentRepository.findByUser_IdAndCourse_CourseId(user.getId(), courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not enrolled in this course"));

        // If certificate already exists, return it
        Optional<Certificate> existing = certificateRepository
                .findByStudent_IdAndCourse_CourseId(user.getId(), courseId);
        if (existing.isPresent()) {
            return toDTO(existing.get());
        }

        // Verify course completion (100%)
        CourseProgressDTO progress = progressService.getCourseProgress(courseId, email);
        if (progress.progressPercentage() < 100.0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Course is not 100% completed yet. Current progress: " + progress.progressPercentage() + "%"
            );
        }

        // Calculate final score percentage
        double finalScore = 100.0;
        if (progress.totalPoints() != null && progress.totalPoints() > 0) {
            finalScore = Math.min(100.0, Math.round((progress.earnedPoints() / progress.totalPoints()) * 1000.0) / 10.0);
        }

        // Generate unique code e.g. DA-2026-03-4782
        String code = generateUniqueCertificateCode();

        Certificate certificate = Certificate.builder()
                .certificateCode(code)
                .student(user)
                .course(course)
                .finalScore(finalScore)
                .certificateUrl("/api/certificates/" + code + "/view")
                .build();

        Certificate saved = certificateRepository.save(certificate);

        // Ensure enrollment is marked completed
        enrollment.setStatus(EnrollmentStatus.COMPLETED);
        enrollmentRepository.save(enrollment);

        // Dispatches
        activityLogService.logActivity(user, "COURSE_COMPLETED", "Claimed certificate " + code + " for course: " + course.getTitle());
        notificationService.sendNotification(
                user,
                "Certificate Issued!",
                "Your certificate for " + course.getTitle() + " is now ready. Code: " + code,
                "CERTIFICATE",
                "/api/certificates/" + code + "/view"
        );

        return toDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<CertificateResponseDTO> getMyCertificates(String email) {
        return certificateRepository.findByStudent_EmailOrderByIssuedAtDesc(email)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public CertificateVerifyDTO verifyCertificate(String code) {
        Optional<Certificate> certOpt = certificateRepository.findByCertificateCode(code);
        if (certOpt.isEmpty()) {
            return CertificateVerifyDTO.builder()
                    .isValid(false)
                    .certificateCode(code)
                    .build();
        }

        Certificate c = certOpt.get();
        Users student = c.getStudent();
        String studentName = (student.getStudent() != null && student.getStudent().getFullName() != null)
                ? student.getStudent().getFullName()
                : student.getFullname();

        Courses course = c.getCourse();
        String instructorName = (course.getInstructor() != null)
                ? ((course.getInstructor().getInstructor() != null && course.getInstructor().getInstructor().getFullName() != null)
                ? course.getInstructor().getInstructor().getFullName()
                : course.getInstructor().getFullname())
                : "Lumen LMS Instructor";

        String duration = formatProgramDuration(course);

        return CertificateVerifyDTO.builder()
                .isValid(true)
                .certificateCode(c.getCertificateCode())
                .studentName(studentName)
                .courseTitle(course.getTitle())
                .instructorName(instructorName)
                .programDuration(duration)
                .finalScore(c.getFinalScore())
                .issuedAt(c.getIssuedAt())
                .verificationUrl("/api/certificates/verify/" + c.getCertificateCode())
                .issuedBy("Lumen LMS Learning Platform")
                .build();
    }

    @Transactional(readOnly = true)
    public String renderCertificateHtml(String code) {
        CertificateVerifyDTO verify = verifyCertificate(code);
        if (!Boolean.TRUE.equals(verify.isValid())) {
            return """
                <!DOCTYPE html>
                <html>
                <head><title>Certificate Not Found</title></head>
                <body style="font-family: sans-serif; text-align: center; padding: 50px;">
                    <h2>Certificate Invalid or Not Found</h2>
                    <p>The certificate code <strong>{{CODE}}</strong> could not be verified in our records.</p>
                </body>
                </html>
                """.replace("{{CODE}}", code != null ? code : "");
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH);
        String dateStr = verify.issuedAt() != null ? dateFormat.format(verify.issuedAt()) : dateFormat.format(new Date());
        String qrSvg = generateQrSvg(code, 120);

        return buildCertificateTemplateHtml(verify, dateStr, qrSvg);
    }

    private String buildCertificateTemplateHtml(CertificateVerifyDTO v, String dateStr, String qrSvg) {
        String courseCategory = v.courseTitle() != null ? v.courseTitle() : "Professional Studies";
        String instructorName = v.instructorName() != null ? v.instructorName() : "Dr. Michael Chen";
        String studentName = v.studentName() != null ? v.studentName() : "Student";
        String courseTitle = v.courseTitle() != null ? v.courseTitle() : "Course";
        String duration = v.programDuration() != null ? v.programDuration() : "16 Weeks (240 Hours)";
        String certCode = v.certificateCode() != null ? v.certificateCode() : "";

        String template = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Course Completion Certificate - {{STUDENT_NAME}}</title>
                <style>
                    @import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&family=Playfair+Display:ital,wght@0,700;1,400&family=Great+Vibes&display=swap');
                    
                    * {
                        box-sizing: border-box;
                        margin: 0;
                        padding: 0;
                    }
                    
                    body {
                        background-color: #f1f5f9;
                        font-family: 'Inter', sans-serif;
                        display: flex;
                        flex-direction: column;
                        align-items: center;
                        padding: 24px;
                        color: #1e293b;
                    }
                    
                    .toolbar {
                        display: flex;
                        gap: 12px;
                        margin-bottom: 20px;
                    }
                    
                    .btn {
                        padding: 10px 20px;
                        font-size: 14px;
                        font-weight: 600;
                        border-radius: 6px;
                        cursor: pointer;
                        border: none;
                        display: inline-flex;
                        align-items: center;
                        gap: 8px;
                        text-decoration: none;
                        box-shadow: 0 2px 4px rgba(0,0,0,0.1);
                        transition: all 0.2s ease;
                    }
                    
                    .btn-primary {
                        background-color: #1d4ed8;
                        color: white;
                    }
                    .btn-primary:hover {
                        background-color: #1e40af;
                    }
                    
                    .btn-secondary {
                        background-color: white;
                        color: #334155;
                        border: 1px solid #cbd5e1;
                    }
                    .btn-secondary:hover {
                        background-color: #f8fafc;
                    }
                    
                    /* Certificate Canvas */
                    .certificate-card {
                        width: 1040px;
                        height: 720px;
                        background: #ffffff;
                        position: relative;
                        display: flex;
                        box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1), 0 8px 10px -6px rgba(0, 0, 0, 0.1);
                        overflow: hidden;
                        border-radius: 4px;
                    }
                    
                    /* Main Area */
                    .main-content {
                        flex: 1;
                        padding: 48px 60px 40px 60px;
                        display: flex;
                        flex-direction: column;
                        justify-content: space-between;
                        position: relative;
                        z-index: 1;
                    }
                    
                    /* Header */
                    .header-row {
                        display: flex;
                        justify-content: space-between;
                        align-items: center;
                    }
                    
                    .logo-wrap {
                        display: flex;
                        align-items: center;
                        gap: 12px;
                    }
                    
                    .logo-icon {
                        width: 28px;
                        height: 28px;
                        border: 5px solid #38bdf8;
                        border-right-color: transparent;
                        border-bottom-color: transparent;
                        position: relative;
                    }
                    .logo-icon::after {
                        content: '';
                        position: absolute;
                        top: 2px;
                        right: 2px;
                        width: 8px;
                        height: 8px;
                        background: #38bdf8;
                    }
                    
                    .logo-text {
                        font-size: 13px;
                        font-weight: 700;
                        line-height: 1.15;
                        color: #1e293b;
                        letter-spacing: -0.2px;
                    }
                    
                    .bars-accent {
                        display: flex;
                        gap: 8px;
                    }
                    
                    .bar {
                        width: 12px;
                        height: 54px;
                        background: #38bdf8;
                        border-radius: 999px;
                    }
                    
                    /* Certificate Title */
                    .title-section {
                        margin-top: 14px;
                    }
                    
                    .cert-title {
                        font-size: 38px;
                        font-weight: 800;
                        color: #1e3a8a;
                        letter-spacing: 0.5px;
                        line-height: 1.1;
                    }
                    
                    .cert-subtitle {
                        font-size: 38px;
                        font-weight: 800;
                        color: #1e3a8a;
                        display: inline-block;
                        position: relative;
                        line-height: 1.1;
                    }
                    
                    .cert-subtitle::after {
                        content: '';
                        position: absolute;
                        left: 0;
                        bottom: -4px;
                        width: 100%;
                        height: 5px;
                        background: #38bdf8;
                        border-radius: 2px;
                    }
                    
                    /* Awardee */
                    .award-section {
                        margin-top: 24px;
                    }
                    
                    .award-to {
                        font-size: 13px;
                        color: #64748b;
                        margin-bottom: 8px;
                        font-weight: 500;
                    }
                    
                    .student-name {
                        font-size: 36px;
                        font-weight: 800;
                        color: #1e293b;
                        letter-spacing: -0.5px;
                        margin-bottom: 12px;
                    }
                    
                    .for-completion {
                        font-size: 15px;
                        color: #334155;
                        margin-bottom: 10px;
                    }
                    
                    .course-name {
                        font-weight: 700;
                        color: #0f172a;
                    }
                    
                    .meta-line {
                        font-size: 12px;
                        color: #64748b;
                        font-weight: 500;
                        padding-bottom: 12px;
                        border-bottom: 1px solid #f1f5f9;
                    }
                    
                    .meta-val {
                        font-weight: 700;
                        color: #334155;
                    }
                    
                    /* Body Paragraph */
                    .desc-text {
                        font-size: 12px;
                        line-height: 1.65;
                        color: #64748b;
                        max-width: 860px;
                        margin-top: 10px;
                    }
                    
                    .issued-by {
                        font-size: 11px;
                        font-weight: 600;
                        color: #334155;
                        margin-top: 10px;
                    }
                    
                    /* Signatures */
                    .signatures-row {
                        display: flex;
                        gap: 60px;
                        margin-top: 16px;
                        align-items: flex-end;
                    }
                    
                    .signature-box {
                        display: flex;
                        flex-direction: column;
                        width: 170px;
                    }
                    
                    .signature-draw {
                        font-family: 'Great Vibes', cursive;
                        font-size: 34px;
                        color: #1e3a8a;
                        line-height: 1;
                        height: 38px;
                        display: flex;
                        align-items: flex-end;
                    }
                    
                    .sig-line {
                        height: 1px;
                        background: #cbd5e1;
                        margin: 4px 0 6px 0;
                    }
                    
                    .sig-name {
                        font-size: 12px;
                        font-weight: 700;
                        color: #1e3a8a;
                    }
                    
                    .sig-role {
                        font-size: 10px;
                        color: #64748b;
                    }
                    
                    /* Pixel mosaic bottom left */
                    .pixel-mosaic {
                        position: absolute;
                        bottom: 24px;
                        left: 24px;
                        display: grid;
                        grid-template-columns: repeat(3, 12px);
                        grid-template-rows: repeat(3, 12px);
                        gap: 3px;
                    }
                    
                    .pixel {
                        background: #38bdf8;
                    }
                    .pixel-empty {
                        background: transparent;
                    }
                    
                    @media print {
                        body {
                            background: white;
                            padding: 0;
                        }
                        .toolbar {
                            display: none;
                        }
                        .certificate-card {
                            box-shadow: none;
                            border: none;
                            width: 100vw;
                            height: 100vh;
                            -webkit-print-color-adjust: exact;
                            print-color-adjust: exact;
                        }
                    }
                </style>
            </head>
            <body>
                <div class="toolbar">
                    <button onclick="window.print()" class="btn btn-primary">
                        <svg width="16" height="16" fill="currentColor" viewBox="0 0 16 16">
                            <path d="M2.5 8a.5.5 0 1 0 0-1 .5.5 0 0 0 0 1z"/>
                            <path d="M5 1a2 2 0 0 0-2 2v2H2a2 2 0 0 0-2 2v3a2 2 0 0 0 2 2h1v1a2 2 0 0 0 2 2h6a2 2 0 0 0 2-2v-1h1a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2h-1V3a2 2 0 0 0-2-2H5zM4 3a1 1 0 0 1 1-1h6a1 1 0 0 1 1 1v2H4V3zm1 5a2 2 0 0 0-2 2v1H2a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h12a1 1 0 0 1 1 1v3a1 1 0 0 1-1 1h-1v-1a2 2 0 0 0-2-2H5zm7 2v3a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1v-3a1 1 0 0 1 1-1h6a1 1 0 0 1 1 1z"/>
                        </svg>
                        Print / Save PDF
                    </button>
                    <a href="/api/certificates/verify/{{CERT_CODE}}" class="btn btn-secondary" target="_blank">
                        Verify Authenticity
                    </a>
                </div>
                
                <div class="certificate-card">
                    <!-- Main Certificate Body -->
                    <div class="main-content">
                        <!-- Header -->
                        <div class="header-row">
                            <div class="logo-wrap">
                                <div class="logo-icon"></div>
                                <div class="logo-text">Lumen<br>LMS</div>
                            </div>
                            <div class="bars-accent">
                                <div class="bar"></div>
                                <div class="bar"></div>
                                <div class="bar"></div>
                            </div>
                        </div>
                        
                        <!-- Titles -->
                        <div class="title-section">
                            <div class="cert-title">COURSE COMPLETION</div>
                            <div class="cert-subtitle">Certificate</div>
                        </div>
                        
                        <!-- Recipient and Course Details -->
                        <div class="award-section">
                            <div class="award-to">This certificate is awarded to</div>
                            <div class="student-name">{{STUDENT_NAME}}</div>
                            <div class="for-completion">For successful completion of <span class="course-name">{{COURSE_TITLE}}</span></div>
                            <div class="meta-line">
                                Program Duration: <span class="meta-val">{{DURATION}}</span> &nbsp;|&nbsp; 
                                Completed on: <span class="meta-val">{{DATE}}</span> &nbsp;|&nbsp; 
                                Certificate ID: <span class="meta-val">{{CERT_CODE}}</span>
                            </div>
                        </div>
                        
                        <!-- Description paragraph -->
                        <div class="desc-text">
                            This certifies that the above-named individual has successfully completed the qualification program, 
                            demonstrating verified hands-on competency in {{COURSE_CATEGORY}} in accordance with industry standards, 
                            including pressure testing, inspection compliance, and code-aligned system commissioning.
                        </div>
                        
                        <div class="issued-by">
                            Issued by: <strong>Lumen LMS</strong> Learning Platform
                        </div>
                        
                        <!-- Signatures -->
                        <div class="signatures-row">
                            <div class="signature-box">
                                <div class="signature-draw">{{INSTRUCTOR_NAME}}</div>
                                <div class="sig-line"></div>
                                <div class="sig-name">{{INSTRUCTOR_NAME}}</div>
                                <div class="sig-role">Program Director</div>
                            </div>
                            <div class="signature-box">
                                <div class="signature-draw">Jennifer Walsh</div>
                                <div class="sig-line"></div>
                                <div class="sig-name">Jennifer Walsh</div>
                                <div class="sig-role">Head of Curriculum</div>
                            </div>
                        </div>
                        
                        <!-- Pixel mosaic in bottom left -->
                        <div class="pixel-mosaic">
                            <div class="pixel"></div><div class="pixel"></div><div class="pixel-empty"></div>
                            <div class="pixel-empty"></div><div class="pixel"></div><div class="pixel-empty"></div>
                            <div class="pixel-empty"></div><div class="pixel"></div><div class="pixel"></div>
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """;

        return template
                .replace("{{STUDENT_NAME}}", studentName)
                .replace("{{COURSE_TITLE}}", courseTitle)
                .replace("{{DURATION}}", duration)
                .replace("{{DATE}}", dateStr)
                .replace("{{CERT_CODE}}", certCode)
                .replace("{{COURSE_CATEGORY}}", courseCategory)
                .replace("{{INSTRUCTOR_NAME}}", instructorName)
                .replace("{{QR_SVG}}", qrSvg);
    }

    private String formatProgramDuration(Courses course) {
        if (course.getOverallDuration() != null && !course.getOverallDuration().isBlank()) {
            return course.getOverallDuration();
        }
        return "16 Weeks (240 Hours)";
    }

    private String generateUniqueCertificateCode() {
        String code;
        do {
            int randomNum = 1000 + new Random().nextInt(9000);
            code = "DA-2026-03-" + randomNum;
        } while (certificateRepository.existsByCertificateCode(code));
        return code;
    }

    private Users getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private CertificateResponseDTO toDTO(Certificate c) {
        Users student = c.getStudent();
        String studentName = (student.getStudent() != null && student.getStudent().getFullName() != null)
                ? student.getStudent().getFullName()
                : student.getFullname();

        Courses course = c.getCourse();
        String instructorName = (course.getInstructor() != null)
                ? ((course.getInstructor().getInstructor() != null && course.getInstructor().getInstructor().getFullName() != null)
                ? course.getInstructor().getInstructor().getFullName()
                : course.getInstructor().getFullname())
                : null;

        return CertificateResponseDTO.builder()
                .certificateId(c.getCertificateId())
                .certificateCode(c.getCertificateCode())
                .courseId(course.getCourseId())
                .courseTitle(course.getTitle())
                .userId(student.getId())
                .studentName(studentName)
                .studentEmail(student.getEmail())
                .instructorName(instructorName)
                .finalScore(c.getFinalScore())
                .certificateUrl(c.getCertificateUrl())
                .issuedAt(c.getIssuedAt())
                .build();
    }

    /**
     * Generates a self-contained 21x21 SVG QR code matrix without external dependencies.
     */
    private String generateQrSvg(String content, int size) {
        int matrixSize = 21;
        boolean[][] matrix = new boolean[matrixSize][matrixSize];

        // 1. Finder patterns (Top-Left, Top-Right, Bottom-Left)
        drawFinderPattern(matrix, 0, 0);
        drawFinderPattern(matrix, matrixSize - 7, 0);
        drawFinderPattern(matrix, 0, matrixSize - 7);

        // 2. Timing patterns
        for (int i = 7; i < matrixSize - 7; i++) {
            matrix[6][i] = (i % 2 == 0);
            matrix[i][6] = (i % 2 == 0);
        }

        // 3. Deterministic pseudo-data modules based on MD5 hash of the content
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(content.getBytes(StandardCharsets.UTF_8));
            int bitIndex = 0;

            for (int r = 0; r < matrixSize; r++) {
                for (int c = 0; c < matrixSize; c++) {
                    // Skip finder and timing patterns
                    if (isFinderOrTiming(r, c, matrixSize)) {
                        continue;
                    }
                    byte b = hash[(bitIndex / 8) % hash.length];
                    boolean bit = ((b >> (bitIndex % 8)) & 1) == 1;
                    matrix[r][c] = bit;
                    bitIndex++;
                }
            }
        } catch (Exception ignored) {
        }

        // 4. Render SVG rects
        StringBuilder sb = new StringBuilder();
        sb.append("<svg width=\"").append(size).append("\" height=\"").append(size)
                .append("\" viewBox=\"0 0 ").append(matrixSize).append(" ").append(matrixSize)
                .append("\" xmlns=\"http://www.w3.org/2000/svg\">\n");
        sb.append("<rect width=\"100%\" height=\"100%\" fill=\"#ffffff\"/>\n");

        for (int r = 0; r < matrixSize; r++) {
            for (int c = 0; c < matrixSize; c++) {
                if (matrix[r][c]) {
                    sb.append("<rect x=\"").append(c).append("\" y=\"").append(r)
                            .append("\" width=\"1\" height=\"1\" fill=\"#000000\"/>\n");
                }
            }
        }
        sb.append("</svg>");
        return sb.toString();
    }

    private void drawFinderPattern(boolean[][] matrix, int startX, int startY) {
        for (int r = 0; r < 7; r++) {
            for (int c = 0; c < 7; c++) {
                if (r == 0 || r == 6 || c == 0 || c == 6) {
                    matrix[startY + r][startX + c] = true;
                } else if (r >= 2 && r <= 4 && c >= 2 && c <= 4) {
                    matrix[startY + r][startX + c] = true;
                } else {
                    matrix[startY + r][startX + c] = false;
                }
            }
        }
    }

    private boolean isFinderOrTiming(int r, int c, int size) {
        // Top-left finder
        if (r < 8 && c < 8) return true;
        // Top-right finder
        if (r < 8 && c >= size - 8) return true;
        // Bottom-left finder
        if (r >= size - 8 && c < 8) return true;
        // Timing lines
        if (r == 6 || c == 6) return true;
        return false;
    }
}
