package com.mainProject.JobPortalSystem.repository;

import com.mainProject.JobPortalSystem.entity.Job;
import com.mainProject.JobPortalSystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long> {

    List<Job> findByPostedBy(User user);

    List<Job> findByTitleContainingIgnoreCase(String title);

    List<Job> findByCategoryContainingIgnoreCase(String category);

    long countByPostedBy(User user);

    long countByPostedByAndDeadlineAfter(User user, LocalDate date);

    long countByPostedByAndDeadlineBefore(User user, LocalDate date);

    List<Job> findByLocationContainingIgnoreCaseAndJobTypeContainingIgnoreCaseAndSalaryLessThanEqual(
            String location,
            String jobType,
            double salary
    );

    List<Job> findByCategory(String category);

    List<Job> findTop5ByCategory(String category);
}