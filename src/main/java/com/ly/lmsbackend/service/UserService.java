package com.ly.lmsbackend.service;

import com.ly.lmsbackend.dto.RegisterRequest;
import com.ly.lmsbackend.dto.UserResponseDTO;
import com.ly.lmsbackend.mapper.UserMapper;
import com.ly.lmsbackend.model.Roles;
import com.ly.lmsbackend.model.Users;
import com.ly.lmsbackend.repository.UserRepository;
import jdk.jshell.spi.ExecutionControl;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final EmailService emailService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, UserMapper userMapper, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.emailService = emailService;
    }

    public UserResponseDTO register(RegisterRequest request){
        if(userRepository.findByEmail(request.email()).isPresent()){
            throw new RuntimeException("Email already exists");
        }

        Users users = new Users();
        users.setUsername(request.username());
        users.setPassword(passwordEncoder.encode(request.password()));
        users.setEmail(request.email());
        users.setUserId(UUID.randomUUID().toString());
        users.setRole(Roles.valueOf(request.role()));

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
}
