package com.mainProject.JobPortalSystem.service;

import com.mainProject.JobPortalSystem.entity.User;
import com.mainProject.JobPortalSystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService{

    @Autowired
    private UserRepository repo;

    public User saveUser(User user){
        return repo.save(user);
    }

    public User findByEmail(String email){
        return repo.findByEmail(email);
    }

    public List<User> getAllUsers(){
        return repo.findAll();
    }

    public void deleteUser(Long id){
        repo.deleteById(id);
    }

    public User findByResetToken(String token){
        return repo.findByResetToken(token);
    }

    public User getUserById(Long id) {
        return repo.findById(id).orElse(null);
    }
}
