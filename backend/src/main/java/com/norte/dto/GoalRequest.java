package com.norte.dto;

import jakarta.validation.constraints.NotBlank;

public class GoalRequest {
    @NotBlank
    private String description;

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
