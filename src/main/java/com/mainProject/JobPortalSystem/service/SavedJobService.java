package com.mainProject.JobPortalSystem.service;

import com.mainProject.JobPortalSystem.entity.Job;
import com.mainProject.JobPortalSystem.entity.SavedJob;
import com.mainProject.JobPortalSystem.entity.User;
import com.mainProject.JobPortalSystem.repository.SavedJobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SavedJobService {

    @Autowired
    private SavedJobRepository repo;

    public void saveJob(User user, Job job) {

        if(repo.existsByUserAndJob(user, job)){
            return;
        }

        SavedJob saved = new SavedJob();
        saved.setUser(user);
        saved.setJob(job);

        repo.save(saved);
    }

    public boolean isAlreadySaved(User user, Job job) {
        return repo.existsByUserAndJob(user, job);
    }

    public List<SavedJob> getSavedJobs(User user) {
        return repo.findByUser(user);
    }

    public void removeSavedJob(User user, Job job) {
        repo.deleteByUserAndJob(user, job);
    }

    public long countBookmarksForRecruiter(User user) {
        return repo.countByJobPostedBy(user);
    }
}