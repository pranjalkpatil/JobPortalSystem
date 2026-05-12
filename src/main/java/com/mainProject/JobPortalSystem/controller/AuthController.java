package com.mainProject.JobPortalSystem.controller;

import com.mainProject.JobPortalSystem.entity.User;
import com.mainProject.JobPortalSystem.service.EmailService;
import com.mainProject.JobPortalSystem.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Controller
public class AuthController {

    @Autowired
    private UserService service;

    @Autowired
    private EmailService emailService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, Model model) {

        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("USER");
        }

        user.setEmail(user.getEmail().toLowerCase().trim());

        user.setPassword(
                passwordEncoder.encode(user.getPassword())
        );

        service.saveUser(user);

        model.addAttribute("msg", "Registration successful! Please login");
        return "login";
    }

    @PostMapping("/login")
    public String loginUser(@RequestParam String email,
                            @RequestParam String password,
                            HttpSession session,
                            Model model) {

        User user = service.findByEmail(email);

        if (user == null) {
            model.addAttribute("error", "User not found");
            return "login";
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            model.addAttribute("error", "Wrong password");
            return "login";
        }

        session.setAttribute("loggedInUser", user);

        if ("ADMIN".equals(user.getRole())) {
            return "redirect:/admin";
        } else if ("RECRUITER".equals(user.getRole())) {
            return "redirect:/recruiter/jobs";
        } else {
            return "redirect:/jobs";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/home")
    public String homePage() {
        return "home";
    }

    @GetMapping("/profile")
    public String profilePage(HttpSession session, Model model) {

        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);
        return "profile";
    }

    @PostMapping("/update-profile")
    public String updateProfile(@ModelAttribute User updatedUser,
                                @RequestParam("profileFile") MultipartFile file,
                                @RequestParam("resumeFile") MultipartFile resumeFile,
                                HttpSession session) {

        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/login";
        }

        user.setName(updatedUser.getName());
        user.setEmail(updatedUser.getEmail());
        user.setPhone(updatedUser.getPhone());
        user.setLocation(updatedUser.getLocation());
        user.setHeadline(updatedUser.getHeadline());
        user.setLinkedin(updatedUser.getLinkedin());
        user.setAboutMe(updatedUser.getAboutMe());
        user.setDegree(updatedUser.getDegree());
        user.setCollege(updatedUser.getCollege());
        user.setGraduationYear(updatedUser.getGraduationYear());
        user.setPercentage(updatedUser.getPercentage());
        user.setSkills(updatedUser.getSkills());
        user.setExperience(updatedUser.getExperience());

        if (!file.isEmpty()) {
            try {
                String fileName = System.currentTimeMillis()
                        + "_" + file.getOriginalFilename();

                String uploadDir = "src/main/resources/static/profile-pics/";
                Path path = Paths.get(uploadDir);

                if (!Files.exists(path)) {
                    Files.createDirectories(path);
                }

                Files.write(path.resolve(fileName), file.getBytes());

                user.setProfilePic(fileName);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (!resumeFile.isEmpty()) {
            try {
                String resumeName = System.currentTimeMillis()
                        + "_" + resumeFile.getOriginalFilename();

                String resumeDir = "src/main/resources/static/resumes/";
                Path resumePath = Paths.get(resumeDir);

                if (!Files.exists(resumePath)) {
                    Files.createDirectories(resumePath);
                }

                Files.write(
                        resumePath.resolve(resumeName),
                        resumeFile.getBytes()
                );

                user.setResume(resumeName);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        service.saveUser(user);

        session.setAttribute("loggedInUser", user);

        return "redirect:/profile";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String email,
                                        Model model) {

        User user = service.findByEmail(email);

        if (user == null) {
            model.addAttribute("error", "Email not found!");
            return "forgot-password";
        }

        String token = UUID.randomUUID().toString();

        user.setResetToken(token);
        service.saveUser(user);

        String resetLink =
                "http://localhost:8080/reset-password?token=" + token;

        String subject = "Password Reset Request";

        String message =
                "Hello " + user.getName() + ",\n\n" +
                        "Click below link to reset your password:\n\n" +
                        resetLink + "\n\n" +
                        "If you did not request this, ignore this email.\n\n" +
                        "JobPortal Team";

        emailService.sendEmail(user.getEmail(), subject, message);

        model.addAttribute("success",
                "Password reset link sent to your email!");

        return "forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam String token,
                                    Model model) {

        User user = service.findByResetToken(token);

        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("token", token);

        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String saveNewPassword(@RequestParam String token,
                                  @RequestParam String password,
                                  Model model) {

        User user = service.findByResetToken(token);

        if (user == null) {
            return "redirect:/login";
        }

        user.setPassword(
                passwordEncoder.encode(password)
        );

        user.setResetToken(null);

        service.saveUser(user);

        model.addAttribute("success",
                "Password changed successfully!");

        return "login";
    }
}