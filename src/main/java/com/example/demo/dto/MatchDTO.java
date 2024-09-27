package com.example.demo.dto;

import lombok.Data;

import java.util.List;

@Data
public class MatchDTO {
    private Long mentorId;
    private Long menteeId;
    private String mentorName;
    private String menteeName;
    private List<String> commonAvailability;
    private String meetingType;
    private List<String> commonCodingLanguages;
    private String interests;
}

