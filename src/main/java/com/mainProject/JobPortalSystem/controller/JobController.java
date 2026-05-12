package com.mainProject.JobPortalSystem.controller;

import com.mainProject.JobPortalSystem.entity.Job;
import com.mainProject.JobPortalSystem.entity.User;
import com.mainProject.JobPortalSystem.service.ApplicationService;
import com.mainProject.JobPortalSystem.service.JobService;
import com.mainProject.JobPortalSystem.service.SavedJobService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class JobController {

    @Autowired
    private JobService jobService;

    @Autowired
    private ApplicationService appService;

    @Autowired
    private SavedJobService savedJobService;

    @GetMapping("/add-job")
    public String showAddJobForm(Model model, HttpSession session) {

        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("job", new Job());
        model.addAttribute("user", user);

        return "add-job";
    }

    @PostMapping("/save-job")
    public String saveJob(
            @Valid @ModelAttribute("job") Job job,
            BindingResult result,
            @RequestParam("logoFile") MultipartFile file,
            HttpSession session,
            Model model
    ) {

        User recruiter = (User) session.getAttribute("loggedInUser");

        if (recruiter == null) {
            return "redirect:/login";
        }

        if (!recruiter.getRole().equals("RECRUITER")) {
            return "redirect:/jobs";
        }

        if (result.hasErrors()) {
            model.addAttribute("user", recruiter);
            return "add-job";
        }

        job.setPostedBy(recruiter);

        if (!file.isEmpty()) {
            try {
                String fileName =
                        System.currentTimeMillis()
                                + "_"
                                + file.getOriginalFilename();

                String uploadDir =
                        "src/main/resources/static/company-logos/";

                Path path = Paths.get(uploadDir);

                if (!Files.exists(path)) {
                    Files.createDirectories(path);
                }

                Files.write(
                        path.resolve(fileName),
                        file.getBytes()
                );

                job.setCompanyLogo(fileName);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        jobService.saveJob(job);

        return "redirect:/jobs";
    }

    @GetMapping("/jobs")
    public String viewJobs(
            @RequestParam(defaultValue = "0") int page,
            Model model,
            HttpSession session
    ) {

        User user =
                (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/login";
        }

        Pageable pageable =
                PageRequest.of(page, 5);

        Page<Job> jobPage =
                jobService.getJobsPage(pageable);

        model.addAttribute(
                "jobs",
                jobPage.getContent()
        );

        model.addAttribute(
                "currentPage",
                page
        );

        model.addAttribute(
                "totalPages",
                jobPage.getTotalPages()
        );

        model.addAttribute(
                "recommendedJobs",
                jobService.getRecommendedJobs(user)
        );

        List<Long> appliedJobIds =
                appService.getApplicationsByUser(user)
                        .stream()
                        .map(app -> app.getJob().getId())
                        .collect(Collectors.toList());

        model.addAttribute(
                "appliedJobIds",
                appliedJobIds
        );

        model.addAttribute("user", user);

        return "jobs";
    }

    @GetMapping("/recruiter/jobs")
    public String recruiterJobs(
            Model model,
            HttpSession session
    ) {

        User recruiter =
                (User) session.getAttribute("loggedInUser");

        if (recruiter == null) {
            return "redirect:/login";
        }

        model.addAttribute(
                "jobs",
                jobService.getJobsByRecruiter(recruiter)
        );

        model.addAttribute("user", recruiter);

        model.addAttribute(
                "totalJobs",
                jobService.countJobsByRecruiter(recruiter)
        );

        model.addAttribute(
                "totalApplicants",
                appService.countApplicantsForRecruiter(recruiter)
        );

        model.addAttribute(
                "totalBookmarks",
                savedJobService.countBookmarksForRecruiter(recruiter)
        );

        return "recruiter-jobs";
    }

    @GetMapping("/search")
    public String searchJobs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false, defaultValue = "") String location,
            @RequestParam(required = false, defaultValue = "") String jobType,
            @RequestParam(required = false, defaultValue = "99999999") double salary,
            Model model,
            HttpSession session
    ) {

        User user =
                (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/login";
        }

        List<Job> jobs;

        if (!location.isEmpty()
                || !jobType.isEmpty()
                || salary < 99999999) {

            jobs =
                    jobService.filterJobs(
                            location,
                            jobType,
                            salary
                    );

        } else if (keyword != null
                && !keyword.isEmpty()) {

            jobs =
                    jobService.searchByTitle(keyword);

        } else if (category != null
                && !category.isEmpty()) {

            jobs =
                    jobService.searchByCategory(category);

        } else {
            jobs =
                    jobService.getAllJobs();
        }

        model.addAttribute("jobs", jobs);
        model.addAttribute("user", user);

        return "jobs";
    }

    @GetMapping("/job/{id}")
    public String jobDetails(
            @PathVariable Long id,
            Model model,
            HttpSession session
    ) {

        User user =
                (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/login";
        }

        Job job =
                jobService.getJobById(id);

        if (job == null) {
            return "redirect:/jobs";
        }

        model.addAttribute("job", job);
        model.addAttribute("user", user);

        return "job-details";
    }

    @GetMapping("/edit-job/{id}")
    public String editJob(
            @PathVariable Long id,
            Model model,
            HttpSession session
    ) {

        User recruiter =
                (User) session.getAttribute("loggedInUser");

        if (recruiter == null) {
            return "redirect:/login";
        }

        Job job =
                jobService.getJobById(id);

        if (job == null) {
            return "redirect:/recruiter/jobs";
        }

        model.addAttribute("job", job);
        model.addAttribute("user", recruiter);

        return "add-job";
    }

    @GetMapping("/delete-job/{id}")
    public String deleteJob(
            @PathVariable Long id,
            HttpSession session
    ) {

        User recruiter =
                (User) session.getAttribute("loggedInUser");

        if (recruiter == null) {
            return "redirect:/login";
        }

        jobService.deleteJob(id);

        return "redirect:/recruiter/jobs";
    }
}