package com.example.demo.services;

import com.example.demo.dto.MatchDTO;
import com.example.demo.models.User;
import java.util.List;

public interface MatchingService {
    List<User> matchMentees(Long mentorId);

    List<User> findMatchesForUser(User user);

    List<MatchDTO> getMatchesForMentee(Long menteeId);

    // New method to get matches for both mentee and their mentors
    List<MatchDTO> findMatchesForUsers(Long menteeId);

    List<MatchDTO> getMatchesForUser(User user);
}

