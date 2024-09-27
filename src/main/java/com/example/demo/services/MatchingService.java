package com.example.demo.services;

import com.example.demo.models.User;
import java.util.List;

public interface MatchingService {
    List<User> matchMentees(Long mentorId);
}
