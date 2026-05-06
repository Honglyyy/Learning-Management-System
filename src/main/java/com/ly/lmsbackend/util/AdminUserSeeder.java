package com.ly.lmsbackend.util;

import com.ly.lmsbackend.model.Roles;
import com.ly.lmsbackend.model.Users;
import com.ly.lmsbackend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminUserSeeder {

    @Bean
    public CommandLineRunner createAdmin(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ){
        return args -> {
            if(userRepository.findByUsername("admin").isEmpty()){
                Users admin = new Users();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole(Roles.ADMIN);
                admin.setEmail("admin1234@test.com");
                admin.setIsVerified(true);

                userRepository.save(admin);

                System.out.println(admin);
            }

        };
    }
}
