package com.example.aicreditanalyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.aicreditanalyzer.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
}
