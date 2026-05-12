package com.mainProject.JobPortalSystem.repository;

import com.mainProject.JobPortalSystem.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SavedJobRepository extends JpaRepository<SavedJob, Long> {

    List<SavedJob> findByUser(User user);

    boolean existsByUserAndJob(User user, Job job);

    void deleteByUserAndJob(User user, Job job);

    long countByJobPostedBy(User user);
}