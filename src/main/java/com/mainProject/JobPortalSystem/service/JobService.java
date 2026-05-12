package com.mainProject.JobPortalSystem.service;

import com.mainProject.JobPortalSystem.entity.Job;
import com.mainProject.JobPortalSystem.entity.User;
import com.mainProject.JobPortalSystem.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

@Service
public class JobService {

    @Autowired
    private JobRepository repo;

    @Autowired
    private ApplicationService appService;

    public Job saveJob(Job job) {
        return repo.save(job);
    }

    public Job getJobById(Long id) {
        return repo.findById(id).orElse(null);
    }

    public List<Job> getAllJobs() {
        return repo.findAll();
    }

    public List<Job> getJobsByRecruiter(User user) {
        return repo.findByPostedBy(user);
    }

    public List<Job> searchByTitle(String title) {
        return repo.findByTitleContainingIgnoreCase(title);
    }

    public List<Job> searchByCategory(String category) {
        return repo.findByCategoryContainingIgnoreCase(category);
    }

    public void deleteJob(Long id) {
        repo.deleteById(id);
    }

    public long totalJobs(User user) {
        return repo.countByPostedBy(user);
    }

    public long activeJobs(User user) {
        return repo.countByPostedByAndDeadlineAfter(user, LocalDate.now());
    }

    public long expiredJobs(User user) {
        return repo.countByPostedByAndDeadlineBefore(user, LocalDate.now());
    }

    public List<Job> filterJobs(String location,
                                String jobType,
                                double salary) {

        return repo
                .findByLocationContainingIgnoreCaseAndJobTypeContainingIgnoreCaseAndSalaryLessThanEqual(
                        location,
                        jobType,
                        salary
                );
    }

    public Page<Job> getJobsPage(Pageable pageable) {
        return repo.findAll(pageable);
    }

    public long countJobsByRecruiter(User user) {
        return repo.findByPostedBy(user).size();
    }

    public List<Job> getRecommendedJobs(User user) {

        List<Job> appliedJobs =
                appService.getApplicationsByUser(user)
                        .stream()
                        .map(app -> app.getJob())
                        .toList();

        if (appliedJobs.isEmpty()) {
            return List.of();
        }

        String category = appliedJobs.get(0).getCategory();

        return repo.findByCategory(category);
    }
}