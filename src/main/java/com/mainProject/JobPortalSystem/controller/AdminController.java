package com.mainProject.JobPortalSystem.controller;

import com.mainProject.JobPortalSystem.entity.User;
import com.mainProject.JobPortalSystem.service.JobService;
import com.mainProject.JobPortalSystem.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private JobService jobService;


    @GetMapping("/admin")
    public String adminDashboard(Model model,
                                 HttpSession session){

        User user = (User) session.getAttribute("loggedInUser");

        if(user == null || !user.getRole().equals("ADMIN")){
            return "redirect:/login";
        }

        model.addAttribute("user", user);

        return "admin-dashboard";
    }


    @GetMapping("/admin/users")
    public String viewUsers(Model model,
                            HttpSession session){

        User user = (User) session.getAttribute("loggedInUser");

        if(user == null || !user.getRole().equals("ADMIN")){
            return "redirect:/login";
        }

        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("user", user);

        return "admin-users";
    }


    @GetMapping("/admin/jobs")
    public String viewJobs(Model model,
                           HttpSession session){

        User user = (User) session.getAttribute("loggedInUser");

        if(user == null || !user.getRole().equals("ADMIN")){
            return "redirect:/login";
        }

        model.addAttribute("jobs", jobService.getAllJobs());
        model.addAttribute("user", user);

        return "admin-jobs";
    }


    @GetMapping("/admin/delete-user/{id}")
    public String deleteUser(@PathVariable Long id){
        userService.deleteUser(id);
        return "redirect:/admin/users";
    }


    @GetMapping("/admin/delete-job/{id}")
    public String deleteJob(@PathVariable Long id){
        jobService.deleteJob(id);
        return "redirect:/admin/jobs";
    }
}