package com.example.demo.services;

import java.util.List;
import java.util.Optional;

import com.example.demo.models.User;

public interface UserServices {
    List<User> getAllUsers();

    Optional<User> getUserById(Long id);

    void deleteUserById(Long id);

    Optional<User> saveUser(User newUser);

    Optional<User> findByEmail(String email);

    Optional<User> loginUser(String email, String password);

    Optional<User> addMentee(Long mentorId, Long menteeId);

    Optional<User> removeMentee(Long mentorId, Long menteeId);

    List<User> getAllMentees(Long mentorId);

    List<User> getAllMentors(Long menteeId);
}
