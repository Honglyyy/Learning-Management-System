package com.ly.lmsbackend.repository;

import com.ly.lmsbackend.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<Users, Long> {
    Optional<Users> findByUsername(String username);
    Optional<Users> findByEmail(String email);
    Optional<Users> findByPhoneNumber(String phoneNumber);
    long countByRole(com.ly.lmsbackend.model.Roles role);
    long countByRoleIn(java.util.List<com.ly.lmsbackend.model.Roles> roles);
}
