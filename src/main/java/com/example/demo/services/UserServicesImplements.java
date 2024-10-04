package com.example.demo.services;

import com.example.demo.dto.EditUserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.example.demo.models.User;
import com.example.demo.repository.UserRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServicesImplements implements UserServices {

    private final UserRepository userRepository;

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public void deleteUserById(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public Optional<User> saveUser(User newUser) {
        return Optional.of(userRepository.save(newUser));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public Optional<User> loginUser(String email, String password) {
        Optional<User> user = userRepository.findByEmail(email);

        if (user.isPresent()) {
            User foundUser = user.get();
            if (foundUser.getPassword().equals(password)) {
                return Optional.of(foundUser);
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> addMentee(Long mentorId, Long menteeId) {
        Optional<User> mentor = userRepository.findById(mentorId);
        Optional<User> mentee = userRepository.findById(menteeId);

        if (mentor.isPresent() && mentee.isPresent()) {
            User mentorUser = mentor.get();
            User menteeUser = mentee.get();
            if (!mentorUser.getMentees().contains(menteeId) && !menteeUser.getMentors().contains(mentorId)) {
                mentorUser.getMentees().add(menteeId);
                menteeUser.getMentors().add(mentorId);
            } else {
                System.out.println("Mentor and mentee are already assigned");
            }
            userRepository.save(mentorUser);
            userRepository.save(menteeUser);

            return mentor;
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> removeMentee(Long mentorId, Long menteeId) {
        Optional<User> mentor = userRepository.findById(mentorId);
        Optional<User> mentee = userRepository.findById(menteeId);

        if (mentor.isPresent() && mentee.isPresent()) {
            mentor.get().getMentees().remove(menteeId);
            mentee.get().getMentors().remove(mentorId);
            userRepository.save(mentor.get());
            userRepository.save(mentee.get());

            return mentor;
        }
        return Optional.empty();
    }

    @Override
    public List<User> getAllMentees(Long mentorId) {
        Optional<User> mentor = userRepository.findById(mentorId);
        if (mentor.isPresent()) {
            List<Long> menteeIds = mentor.get().getMentees();
            return menteeIds.stream()
                    .map(userRepository::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    @Override
    public List<User> getAllMentors(Long menteeId) {
        Optional<User> mentee = userRepository.findById(menteeId);
        if (mentee.isPresent()) {
            List<Long> mentorIds = mentee.get().getMentors();
            return mentorIds.stream()
                    .map(userRepository::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    public List<User> matchMentees(Long menteeId) {
        Optional<User> mentee = userRepository.findById(menteeId);
        if (!mentee.isPresent()) {
            return List.of();
        }
        User menteeUser = mentee.get();

        return userRepository.findAll().stream()
                .filter(mentor -> !mentor.getId().equals(menteeId) &&
                        mentor.getAvailability().stream().anyMatch(menteeUser.getAvailability()::contains) &&
                        mentor.getMeetingType().equals(menteeUser.getMeetingType()) &&
                        !mentor.getCodingLanguage().stream().noneMatch(menteeUser.getCodingLanguage()::contains) &&
                        !mentor.getExpertise().stream().noneMatch(menteeUser.getExpertise()::contains))
                .collect(Collectors.toList());
    }




    public List<User> getAllFreeMentors(){
            List<User> allUsers = userRepository.findAll();
            List<User> mentors = new ArrayList<>();

            for (User user : allUsers) {
                if ("mentor".equalsIgnoreCase(user.getRole())) {
                    mentors.add(user);
                }
            }

       return mentors;

    }

    public List<User> getAllFreeMentees(){
        List<User> allUsers = userRepository.findAll();
        List<User> mentees = new ArrayList<>();

        for (User user : allUsers) {

         if ("mentee".equalsIgnoreCase(user.getRole())) {
            mentees.add(user);
        }
        }
        return mentees;
    }


    public Optional <User> editUserProfile(Long userId, EditUserDTO newDetails){

        Optional<User> foundUser = userRepository.findById(userId);

        if(foundUser.isPresent()){
            foundUser.get().setLocation(newDetails.getLocation());
            foundUser.get().setMeetingType(newDetails.getMeetingType());
            foundUser.get().setCertifications(newDetails.getCertifications());
            foundUser.get().setPersonalStatement(newDetails.getPersonalStatement());
            foundUser.get().setYearsOfExperience(newDetails.getYearsOfExperience());

            return Optional.of(userRepository.save(foundUser.get()));
        }

        return Optional.empty();

    }


    public User matchMenteeWithMentor(User mentee, List<User> mentors) {
        // Create a map to store mentors and their matching scores
        Map<User, Integer> mentorScores = new HashMap<>();

        // Iterate through mentors and calculate scores
        for (User mentor : mentors) {
            int score = calculateMatchScore(mentee, mentor);
            if (score > 0) {
                mentorScores.put(mentor, score);
            }
        }

        // If no mentors match, return null
        if (mentorScores.isEmpty()) {
            return null;
        }

        // Find the maximum score
        int maxScore = Collections.max(mentorScores.values());

        // Get all mentors with the maximum score
        List<User> bestMatches = mentorScores.entrySet().stream()
                .filter(entry -> entry.getValue() == maxScore)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // Return a random mentor from the best matches
        Random random = new Random();
        return bestMatches.get(random.nextInt(bestMatches.size()));
    }

    public int calculateMatchScore(User mentee, User mentor) {
        int score = 0;

        // Check if meeting type matches
        if (mentor.getMeetingType().equals(mentee.getMeetingType())) {
            score++;
        }

        // Check for matching availability
        List<String> commonAvailability = new ArrayList<>(mentee.getAvailability());
        commonAvailability.retainAll(mentor.getAvailability());
        if (!commonAvailability.isEmpty()) {
            score++;
        }

        // Check for matching coding languages
        List<String> commonCodingLanguages = new ArrayList<>(mentee.getCodingLanguage());
        commonCodingLanguages.retainAll(mentor.getCodingLanguage());
        if (!commonCodingLanguages.isEmpty()) {
            score++;
        }

        return score; // Return the total score
    }
}
