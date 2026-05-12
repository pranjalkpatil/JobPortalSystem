package com.mainProject.JobPortalSystem.repository;

import com.mainProject.JobPortalSystem.entity.Application;
import com.mainProject.JobPortalSystem.entity.Job;
import com.mainProject.JobPortalSystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByUser(User user);

    boolean existsByUserAndJob(User user, Job job);

    List<Application> findByJob(Job job);

    long countByJobPostedBy(User user);
}
