package com.ly.lmsbackend.util;

import com.ly.lmsbackend.model.Roles;
import com.ly.lmsbackend.model.Users;
import com.ly.lmsbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminUserSeeder {
    @Value("${ADMIN_NAME}")
    private String name;
    @Value("${ADMIN_EMAIL}")
    private String email;
    @Value("${ADMIN_PASS}")
    private String password;

    @Bean
    public CommandLineRunner createAdmin(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ){
        return args -> {
            if(userRepository.findByUsername(name).isEmpty()){
                Users admin = new Users();
                admin.setUsername(name);
                admin.setPassword(passwordEncoder.encode(password));
                admin.setRole(Roles.ADMIN);
                admin.setEmail(email);
                admin.setIsVerified(true);

                userRepository.save(admin);
            }

        };
    }
}
