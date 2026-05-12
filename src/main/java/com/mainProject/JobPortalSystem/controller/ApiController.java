package com.mainProject.JobPortalSystem.controller;

import com.mainProject.JobPortalSystem.entity.Job;
import com.mainProject.JobPortalSystem.entity.User;
import com.mainProject.JobPortalSystem.service.JobService;
import com.mainProject.JobPortalSystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ApiController {

    @Autowired
    private JobService jobService;

    @Autowired
    private UserService userService;

    @GetMapping("/jobs")
    public List<Job> getAllJobs() {
        return jobService.getAllJobs();
    }

    @GetMapping("/jobs/{id}")
    public Job getJobById(@PathVariable Long id) {
        return jobService.getJobById(id);
    }

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/users/{id}")
    public User getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }
}