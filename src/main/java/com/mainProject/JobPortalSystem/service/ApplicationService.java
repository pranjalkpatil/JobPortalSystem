package com.mainProject.JobPortalSystem.service;

import com.mainProject.JobPortalSystem.entity.Application;
import com.mainProject.JobPortalSystem.entity.Job;
import com.mainProject.JobPortalSystem.entity.User;
import com.mainProject.JobPortalSystem.repository.ApplicationRepository;
import com.mainProject.JobPortalSystem.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApplicationService {

    @Autowired
    private ApplicationRepository repo;

    @Autowired
    private JobRepository jobRepo;

    public Application apply(Application app){
        return repo.save(app);
    }
    public List<Application> getApplicationsByUser(User user){
        return repo.findByUser(user);
    }

    public boolean alreadyApplied(User user, Job job){
        return repo.existsByUserAndJob(user,job);
    }

    public List<Application> getApplicationsByJob(Job job){
        return repo.findByJob(job);
    }
    public Application getById(Long id){
        return repo.findById(id).orElse(null);
    }

    public long totalApplicants(User recruiter){

        long count = 0;

        List<Job> jobs = jobRepo.findByPostedBy(recruiter);

        for(Job job : jobs){
            count += repo.findByJob(job).size();
        }

        return count;
    }

    public long countApplicantsForRecruiter(User user){
        return repo.countByJobPostedBy(user);
    }
}
