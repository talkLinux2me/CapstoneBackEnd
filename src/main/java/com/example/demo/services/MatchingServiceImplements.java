package com.example.demo.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.demo.dto.MatchDTO;
import com.example.demo.models.User;
import com.example.demo.repository.UserRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchingServiceImplements implements MatchingService {

    private final UserRepository userRepository;

    @Override
    public List<User> matchMentees(Long mentorId) {
        Optional<User> mentorOpt = userRepository.findById(mentorId);
        if (mentorOpt.isPresent()) {
            User mentor = mentorOpt.get();
            return userRepository.findAll().stream()
                    .filter(mentee -> matches(mentor, mentee))
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    @Override
    public List<User> findMatchesForUser(User user) {
        return List.of();
    }

    // Matches method for comparing mentor and mentee
    private boolean matches(User mentor, User mentee) {
        return mentor.getAvailability().stream().anyMatch(mentee.getAvailability()::contains) &&
                mentor.getMeetingType().equals(mentee.getMeetingType()) &&
                !mentor.getCodingLanguage().stream().noneMatch(mentee.getCodingLanguage()::contains);
    }

    @Override
    public List<MatchDTO> getMatchesForMentee(Long menteeId) {
        Optional<User> menteeOpt = userRepository.findById(menteeId);
        if (menteeOpt.isPresent()) {
            User mentee = menteeOpt.get();
            return userRepository.findAll().stream()
                    .filter(mentor -> matches(mentor, mentee))
                    .map(this::convertToMatchDTO)
                    .collect(Collectors.toList());
        }
        return List.of(); // Return empty list if not found
    }

    @Override
    public List<MatchDTO> findMatchesForUsers(Long menteeId) {
        List<MatchDTO> matches = new ArrayList<>();
        matches.addAll(getMatchesForMentee(menteeId));

        List<User> mentors = userRepository.findMentorsByMenteeId(menteeId);
        for (User mentor : mentors) {
            matches.addAll(getMatchesForUser(mentor));
        }

        return matches;
    }

    @Override
    public List<MatchDTO> getMatchesForUser(User user) {
        return userRepository.findAll().stream()
                .filter(otherUser -> matches(user, otherUser)) // Ensure you're passing both users
                .map(this::convertToMatchDTO)
                .collect(Collectors.toList());
    }

    private MatchDTO convertToMatchDTO(User user) {
        return new MatchDTO(user.getAvailability(), Collections.singletonList(user.getMeetingType()), user.getCodingLanguage());
    }

}
