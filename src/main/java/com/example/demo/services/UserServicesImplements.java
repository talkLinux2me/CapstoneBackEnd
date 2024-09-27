package com.example.demo.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.example.demo.models.User;
import com.example.demo.repository.UserRepository;

import java.util.List;
import java.util.Optional;
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
                        !mentor.getCodingLanguages().stream().noneMatch(menteeUser.getCodingLanguages()::contains) &&
                        !mentor.getExpertise().stream().noneMatch(menteeUser.getExpertise()::contains))
                .collect(Collectors.toList());
    }
}
