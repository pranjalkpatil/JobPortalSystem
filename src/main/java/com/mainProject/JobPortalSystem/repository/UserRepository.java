package com.mainProject.JobPortalSystem.repository;

import com.mainProject.JobPortalSystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);

    void deleteById(Long id);

    User findByResetToken(String resetToken);
}
