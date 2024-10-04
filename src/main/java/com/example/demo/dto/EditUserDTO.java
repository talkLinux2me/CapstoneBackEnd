package com.example.demo.dto;

import lombok.Data;

import java.util.List;

@Data
public class EditUserDTO {
    private String location;
    private String personalStatement;
    private String certifications;
    private int yearsOfExperience;
    private String meetingType;
}