package com.ly.lmsbackend.service;

import com.ly.lmsbackend.dto.authdtos.ChangePasswordRequest;
import com.ly.lmsbackend.dto.authdtos.RegisterRequest;
import com.ly.lmsbackend.dto.authdtos.UserResponseDTO;
import com.ly.lmsbackend.mapper.UserMapper;
import com.ly.lmsbackend.model.Genders;
import com.ly.lmsbackend.model.Instructors;
import com.ly.lmsbackend.model.Roles;
import com.ly.lmsbackend.model.Students;
import com.ly.lmsbackend.model.Users;
import com.ly.lmsbackend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Year;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final EmailService emailService;
    private final QuizAttemptRepository quizAttemptRepository;
    private final PaymentRepository paymentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseReviewRepository courseReviewRepository;
    private final CourseRepository courseRepository;
    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;
    private final QuizRepository quizRepository;
    private final LessonRepository lessonRepository;
    private final SectionRepository sectionRepository;
    private final StudentRepository studentRepository;
    private final InstructorRepository instructorRepository;

    @Transactional
    public UserResponseDTO register(RegisterRequest request){
        if(userRepository.findByEmail(request.email()).isPresent()){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        Roles role = request.role();
        if (role == null) {
            role = Roles.STUDENT;
        }

        Users users = new Users();
        users.setUsername(request.username());
        users.setPassword(passwordEncoder.encode(request.password()));
        users.setEmail(request.email());
        users.setFullname(request.fullName());
        users.setPhoneNumber(request.phoneNumber());
        users.setUserId(UUID.randomUUID().toString());
        users.setRole(role);

        if (role == Roles.STUDENT || role == Roles.USER) {
            int year = Year.now().getValue();
            long count = studentRepository.count() + 1;
            String code;
            do {
                code = String.format("STU-%d-%04d", year, count++);
            } while (studentRepository.existsByStudentCode(code));

            Students student = Students.builder()
                    .user(users)
                    .studentCode(code)
                    .fullName(request.fullName())
                    .phoneNumber(request.phoneNumber())
                    .gender(Genders.NOT_SPECIFIC)
                    .build();
            users.setStudent(student);
        } else if (role == Roles.INSTRUCTOR) {
            Instructors instructor = Instructors.builder()
                    .user(users)
                    .fullName(request.fullName())
                    .phoneNumber(request.phoneNumber())
                    .averageRating(5.0)
                    .build();
            users.setInstructor(instructor);
        }

        Users saved = userRepository.save(users);

        sendVerificationOtp(saved.getEmail());
        return userMapper.dto(saved);
    }

    public void sendVerificationOtp(String email){
        String otp = generateOtp();
        Long expireAt = System.currentTimeMillis() + 10 * 60 * 1000; //10mins
        Users existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if(existingUser.getIsVerified()){
            return;
        }

        existingUser.setOtp(otp);
        existingUser.setVerifyOtpExpireAt(expireAt);

        userRepository.save(existingUser);

        try{
            emailService.sendOtp(existingUser.getEmail(),otp);
        }
        catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    public UserResponseDTO verifyOtp(String email,String otp){
        Users existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if(existingUser.getVerifyOtpExpireAt() < System.currentTimeMillis()){
            throw new RuntimeException("Otp expired");
        }

        if(!existingUser.getOtp().equals(otp)){
            throw new RuntimeException("Invalid Otp");
        }

        existingUser.setIsVerified(true);
        existingUser.setOtp(null);
        existingUser.setVerifyOtpExpireAt(0L);

        userRepository.save(existingUser);
        emailService.successOtp(existingUser.getEmail());
        return  userMapper.dto(existingUser);
    }

    public void sendResetOtp(String email){
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String otp = generateOtp();
        Long expireAt = System.currentTimeMillis() + 10 * 60 * 1000;

        user.setResetOtp(otp);
        user.setResetOtpExpireAt(expireAt);

        userRepository.save(user);

        try{
            emailService.sendResetOtp(user.getEmail(), otp);
        }

        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void resetPassword(String otp, String email, String password){
        Users user =  userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if(user.getResetOtpExpireAt() < System.currentTimeMillis()){
            throw new RuntimeException("Otp expired");
        }
        if(!user.getResetOtp().equals(otp)||user.getResetOtp() == null){
            throw new RuntimeException("Invalid Otp");
        }

        user.setPassword(passwordEncoder.encode(password));
        user.setResetOtpExpireAt(0L);
        user.setResetOtp(null);

        userRepository.save(user);
    }

    public String generateOtp(){
        return String.valueOf(ThreadLocalRandom.current().nextInt(100000,1000000));
    }

    public java.util.List<UserResponseDTO> getAllUsers(){
        return userRepository.findAll().stream()
                .map(userMapper::dto)
                .toList();
    }

    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password does not match");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    @Transactional
    public UserResponseDTO updateUserRole(
            Long id,
            Roles role
    ) {
        Users user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        user.setRole(role);

        if ((role == Roles.STUDENT || role == Roles.USER) && user.getStudent() == null && studentRepository.findByUser_Id(id).isEmpty()) {
            int year = Year.now().getValue();
            long count = studentRepository.count() + 1;
            String code;
            do {
                code = String.format("STU-%d-%04d", year, count++);
            } while (studentRepository.existsByStudentCode(code));

            Students student = Students.builder()
                    .user(user)
                    .studentCode(code)
                    .fullName(user.getFullname() != null ? user.getFullname() : user.getUsername())
                    .phoneNumber(user.getPhoneNumber())
                    .gender(Genders.NOT_SPECIFIC)
                    .build();
            studentRepository.save(student);
            user.setStudent(student);
        } else if (role == Roles.INSTRUCTOR && user.getInstructor() == null && instructorRepository.findByUser_Id(id).isEmpty()) {
            Instructors instructor = Instructors.builder()
                    .user(user)
                    .fullName(user.getFullname() != null ? user.getFullname() : user.getUsername())
                    .phoneNumber(user.getPhoneNumber())
                    .averageRating(5.0)
                    .build();
            instructorRepository.save(instructor);
            user.setInstructor(instructor);
        }

        userRepository.save(user);

        return userMapper.dto(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        Users user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        studentRepository.deleteByUser_Id(id);
        instructorRepository.deleteByUser_Id(id);
        quizAttemptRepository.deleteAllForUserRemoval(id);
        paymentRepository.deleteAllForUserRemoval(id);
        enrollmentRepository.deleteAllForUserRemoval(id);
        courseReviewRepository.deleteAllForUserRemoval(id);
        answerRepository.deleteAllForUserRemoval(id);
        questionRepository.deleteAllForUserRemoval(id);
        quizRepository.deleteAllForUserRemoval(id);
        lessonRepository.deleteAllForUserRemoval(id);
        sectionRepository.deleteAllForUserRemoval(id);
        courseRepository.deleteAll(courseRepository.findByInstructor_Id(id));
        userRepository.delete(user);
    }
}
