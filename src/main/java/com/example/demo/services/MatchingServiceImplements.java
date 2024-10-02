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

    private boolean matches(User mentor, User mentee) {
        // Match based on availability, meeting type, coding languages, etc.
        return mentor.getAvailability().stream().anyMatch(mentee.getAvailability()::contains) &&
                mentor.getMeetingType().equals(mentee.getMeetingType()) &&
                !mentor.getCodingLanguage().stream().noneMatch(mentee.getCodingLanguage()::contains);
    }
}
