package com.mainProject.JobPortalSystem.controller;

import com.mainProject.JobPortalSystem.entity.Application;
import com.mainProject.JobPortalSystem.entity.Job;
import com.mainProject.JobPortalSystem.entity.User;
import com.mainProject.JobPortalSystem.service.ApplicationService;
import com.mainProject.JobPortalSystem.service.JobService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import com.mainProject.JobPortalSystem.service.EmailService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
public class ApplicationController {

    @Autowired
    private ApplicationService appService;

    @Autowired
    private JobService jobService;

    @Autowired
    private EmailService emailService;

    @PostMapping("/apply/{jobId}")
    public String applyJob(
            @PathVariable Long jobId,
            @RequestParam("resume") MultipartFile file,
            RedirectAttributes ra,
            HttpSession session) {

        User user = (User) session.getAttribute("loggedInUser");

        if(user == null){
            return "redirect:/login";
        }

        Job job = jobService.getJobById(jobId);

        if(job == null){
            ra.addFlashAttribute("error","Job not found");
            return "redirect:/jobs";
        }

        if(appService.alreadyApplied(user, job)){
            ra.addFlashAttribute(
                    "error",
                    "You have already applied for this job."
            );
            return "redirect:/jobs";
        }

        if(file.isEmpty()){
            ra.addFlashAttribute("error","Please upload resume!");
            return "redirect:/jobs";
        }

        if (file.getContentType() == null || !file.getContentType().equals("application/pdf")) {
            ra.addFlashAttribute("error", "Only PDF allowed!");
            return "redirect:/jobs";
        }

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

        try {
            String uploadDir = "src/main/resources/static/resumes/";
            Path path = Paths.get(uploadDir);

            if(!Files.exists(path)){
                Files.createDirectories(path);
            }

            Files.write(path.resolve(fileName), file.getBytes());

        } catch (Exception e){
            e.printStackTrace();
            ra.addFlashAttribute("error","File upload failed!");
            return "redirect:/jobs";
        }

        Application app = new Application();
        app.setUser(user);
        app.setJob(job);
        app.setStatus("UNDER REVIEW");
        app.setResumeFileName(fileName);

        appService.apply(app);

        if (job.getPostedBy() != null && job.getPostedBy().getEmail() != null) {

            String recruiterEmail = job.getPostedBy().getEmail();

            String subject = "New Job Application";

            String message =
                    "Hello,\n\n" +
                            "You have received a new application for your job: " + job.getTitle() + "\n\n" +
                            "Applicant Name: " + user.getName() + "\n" +
                            "Applicant Email: " + user.getEmail() + "\n\n" +
                            "Login to view details.\n\n" +
                            "JobPortal System";

            try {
                emailService.sendEmail(recruiterEmail, subject, message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        ra.addFlashAttribute("success","Applied with Resume!");
        return "redirect:/jobs";
    }

    @GetMapping("/applied-jobs")
    public String viewAppliedJobs(Model model,
                                  HttpSession session) {

        User user = (User) session.getAttribute("loggedInUser");

        if(user == null){
            return "redirect:/login";
        }

        model.addAttribute("user", user);
        model.addAttribute(
                "applications",
                appService.getApplicationsByUser(user)
        );

        return "applied-jobs";
    }

    @GetMapping("/applicants/{jobId}")
    public String viewApplicants(@PathVariable Long jobId,
                                 Model model,
                                 HttpSession session){

        User user = (User) session.getAttribute("loggedInUser");

        if(user == null){
            return "redirect:/login";
        }

        Job job = jobService.getJobById(jobId);

        model.addAttribute("applications",
                appService.getApplicationsByJob(job));

        model.addAttribute("user", user);

        return "applicants";
    }

    @GetMapping("/download-resume/{appId}")
    public ResponseEntity<Resource> downloadResume(@PathVariable Long appId) {

        Application app = appService.getById(appId);

        if(app == null || app.getResumeFileName() == null){
            return ResponseEntity.notFound().build();
        }

        try {
            String path = "src/main/resources/static/resumes/";
            Path filePath = Paths.get(path + app.getResumeFileName());

            Resource resource = new UrlResource(filePath.toUri());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + app.getResumeFileName() + "\"")
                    .body(resource);

        } catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/application-status/{id}/{status}")
    public String updateApplicationStatus(@PathVariable Long id,
                                          @PathVariable String status) {

        Application app = appService.getById(id);

        if(app == null){
            return "redirect:/jobs";
        }

        app.setStatus(status);

        appService.apply(app);
        
        String candidateEmail = app.getUser().getEmail();
        String candidateName = app.getUser().getName();
        String jobTitle = app.getJob().getTitle();
        String companyName = app.getJob().getCompany();

        String subject = "";
        String message = "";

        if(status.equals("ACCEPTED")) {

            subject = "Job Application Accepted";

            message =
                    "Hello " + candidateName + ",\n\n" +
                            "Congratulations! 🎉\n\n" +
                            "Your application for the position:\n" +
                            jobTitle + "\n" +
                            "at " + companyName + " has been ACCEPTED.\n\n" +
                            "The recruiter may contact you soon.\n\n" +
                            "Best wishes,\n" +
                            "JobPortal Team";

        }

        else if(status.equals("REJECTED")) {

            subject = "Job Application Update";

            message =
                    "Hello " + candidateName + ",\n\n" +
                            "Thank you for applying for:\n" +
                            jobTitle + "\n" +
                            "at " + companyName + ".\n\n" +
                            "Unfortunately, your application was not selected this time.\n\n" +
                            "Keep applying and stay motivated.\n\n" +
                            "Best wishes,\n" +
                            "JobPortal Team";
        }

        else if(status.equals("UNDER REVIEW")) {

            subject = "Application Under Review";

            message =
                    "Hello " + candidateName + ",\n\n" +
                            "Your application for:\n" +
                            jobTitle + "\n" +
                            "at " + companyName + " is currently UNDER REVIEW.\n\n" +
                            "We will update you soon.\n\n" +
                            "Regards,\n" +
                            "JobPortal Team";
        }

        try {
            emailService.sendEmail(candidateEmail, subject, message);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "redirect:/applicants/" + app.getJob().getId();
    }
}