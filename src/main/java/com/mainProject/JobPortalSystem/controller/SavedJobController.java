package com.mainProject.JobPortalSystem.controller;

import com.mainProject.JobPortalSystem.entity.Job;
import com.mainProject.JobPortalSystem.entity.User;
import com.mainProject.JobPortalSystem.service.JobService;
import com.mainProject.JobPortalSystem.service.SavedJobService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class SavedJobController {

    @Autowired
    private SavedJobService savedService;

    @Autowired
    private JobService jobService;

    @GetMapping("/save/{jobId}")
    public String saveJob(@PathVariable Long jobId,
                          HttpSession session,
                          RedirectAttributes ra) {

        User user = (User) session.getAttribute("loggedInUser");

        if(user == null){
            return "redirect:/login";
        }

        Job job = jobService.getJobById(jobId);

        if(job == null){
            ra.addFlashAttribute("error", "Job not found.");
            return "redirect:/jobs";
        }

        if(savedService.isAlreadySaved(user, job)){
            ra.addFlashAttribute(
                    "error",
                    "Job already saved."
            );
            return "redirect:/jobs";
        }

        savedService.saveJob(user, job);

        ra.addFlashAttribute(
                "success",
                "Job saved successfully!"
        );

        return "redirect:/jobs";
    }

    @GetMapping("/saved-jobs")
    public String viewSavedJobs(HttpSession session,
                                Model model) {

        User user = (User) session.getAttribute("loggedInUser");

        if(user == null){
            return "redirect:/login";
        }

        model.addAttribute(
                "savedJobs",
                savedService.getSavedJobs(user)
        );

        model.addAttribute("user", user);

        return "savedJob";
    }

    @GetMapping("/unsave-job/{jobId}")
    public String removeSavedJob(@PathVariable Long jobId,
                                 HttpSession session,
                                 RedirectAttributes ra) {

        User user = (User) session.getAttribute("loggedInUser");

        if(user == null){
            return "redirect:/login";
        }

        Job job = jobService.getJobById(jobId);

        if(job == null){
            return "redirect:/savedJob";
        }

        savedService.removeSavedJob(user, job);

        ra.addFlashAttribute(
                "success",
                "Saved job removed."
        );

        return "redirect:/savedJob";
    }
}