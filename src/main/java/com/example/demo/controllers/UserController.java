package com.example.demo.controllers;

import com.example.demo.dto.EditUserDTO;
import com.example.demo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.MatchDTO;
import com.example.demo.models.User;
import com.example.demo.services.UserServices;
import com.example.demo.services.MatchingService;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserServices userServices;

    @Autowired
    private MatchingService matchingService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User newUser) {
        logger.info("Registering user: {}", newUser.getEmail());
        Optional<User> existingUser = userServices.findByEmail(newUser.getEmail());

        logger.info(String.valueOf(existingUser));
        if (existingUser.isPresent()) {
            logger.warn("Email already in use: {}", newUser.getEmail());
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email is already in use.");
        }

        Optional<User> registeredUser = userServices.saveUser(newUser);
        if (registeredUser.isPresent()) {
            logger.info("User registered successfully: {}", registeredUser.get().getEmail());

            // Fetch matches for the newly registered user
            List<User> matches = matchingService.findMatchesForUser(registeredUser.get());

            // Return the user and their matches in the response
            HashMap<String, Object> response = new HashMap<>();
            response.put("user", registeredUser.get());
            response.put("matches", matches);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            logger.error("An error occurred during registration for user: {}", newUser.getEmail());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error has occurred during registration.");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
        logger.info("User login attempt: {}", loginRequest.getEmail());
        Optional<User> user = userServices.loginUser(loginRequest.getEmail(), loginRequest.getPassword());

        if (user.isPresent()) {
            HashMap response = new HashMap<>();
            response.put("token", "granted");
            response.put("userID", user.get().getId());
            response.put("role", user.get().getRole());
            logger.info("User logged in successfully: {}", loginRequest.getEmail());
            return ResponseEntity.ok().body(response);
        } else {
            logger.warn("Login failed for user: {}", loginRequest.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Email or Password is Incorrect");
        }
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        logger.info("Fetching all users");
        List<User> users = userServices.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        logger.info("Fetching user by ID: {}", id);
        return userServices.getUserById(id)
                .map(user -> {
                    logger.info("User found: {}", user.getEmail());
                    return ResponseEntity.ok().body(user);
                })
                .orElseGet(() -> {
                    logger.warn("User not found with ID: {}", id);
                    return ResponseEntity.notFound().build();
                });
    }

    @PostMapping("/{mentorId}/add-mentee/{menteeId}")
    public ResponseEntity<?> addMentee(@PathVariable Long mentorId, @PathVariable Long menteeId) {
        logger.info("Adding mentee with ID: {} to mentor with ID: {}", menteeId, mentorId);
        Optional<User> result = userServices.addMentee(mentorId, menteeId);
        return result.isPresent()
                ? ResponseEntity.ok(result.get())
                : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{mentorId}/remove-mentee/{menteeId}")
    public ResponseEntity<?> removeMentee(@PathVariable Long mentorId, @PathVariable Long menteeId) {
        logger.info("Removing mentee with ID: {} from mentor with ID: {}", menteeId, mentorId);
        Optional<User> result = userServices.removeMentee(mentorId, menteeId);
        return result.isPresent()
                ? ResponseEntity.ok(result.get())
                : ResponseEntity.notFound().build();
    }

    @GetMapping("/{mentorId}/mentees")
    public ResponseEntity<?> getMentees(@PathVariable Long mentorId) {
        logger.info("Fetching mentees for mentor with ID: {}", mentorId);
        List<User> mentees = userServices.getAllMentees(mentorId);
        return !mentees.isEmpty()
                ? ResponseEntity.ok().body(mentees)
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body("User has no mentees");
    }

    @GetMapping("/{menteeId}/mentors")
    public ResponseEntity<?> getMentors(@PathVariable Long menteeId) {
        logger.info("Fetching mentors for mentee with ID: {}", menteeId);
        List<User> mentors = userServices.getAllMentors(menteeId);
        return !mentors.isEmpty()
                ? ResponseEntity.ok().body(mentors)
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body("User has no mentors");
    }

    @GetMapping("/{menteeId}/matches")
    public ResponseEntity<List<MatchDTO>> getMatches(@PathVariable Long menteeId) {
        logger.info("Fetching matches for mentee with ID: {}", menteeId);
        List<MatchDTO> matches = matchingService.findMatchesForUsers(menteeId);

        return matches.isEmpty()
                ? ResponseEntity.status(HttpStatus.NOT_FOUND).body(List.of())
                : ResponseEntity.ok(matches);
    }



    @GetMapping("/getAllMentors")
    public ResponseEntity<?> getAllMentors() {
        List<User> allMentors = userServices.getAllFreeMentors();
        return ResponseEntity.ok().body(allMentors);
    }

    @GetMapping("/getAllMentees")
    public ResponseEntity<?> getAllMentees() {
        List<User> allMentees = userServices.getAllFreeMentees();
        return ResponseEntity.ok().body(allMentees);
    }

    @PutMapping("/edit/{userID}")
    public ResponseEntity<?> editUser(@PathVariable Long userID, @RequestBody EditUserDTO newDetails) {
        Optional<User> foundUser = userServices.editUserProfile(userID, newDetails);

        if (foundUser.isPresent()) {
            return ResponseEntity.ok().body(foundUser.get());
        }

        return ResponseEntity.ok().body("Oops! Something happened");
    }

    @GetMapping("/match/{menteeId}")
    public ResponseEntity<User> matchMenteeWithMentor(@PathVariable Long menteeId) {
        Optional<User> mentee = userRepository.findById(menteeId);

        if (!mentee.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        List<User> mentors = userServices.getAllFreeMentors();
        User matchedMentor = userServices.matchMenteeWithMentor(mentee.get(), mentors);

        if (matchedMentor != null) {
            return ResponseEntity.ok(matchedMentor);
        } else {
            return ResponseEntity.noContent().build();
        }
    }
}
