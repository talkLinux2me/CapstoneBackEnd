package com.example.demo.dto;

import lombok.Data;

import java.util.List;

@Data
public class EditUserDTO {
    private String location;
    private String personalStatement;
    private List<String> interests;
    private List <String> skills;
}