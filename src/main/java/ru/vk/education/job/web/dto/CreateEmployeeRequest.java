package ru.vk.education.job.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateEmployeeRequest(
        @NotBlank String name,
        @NotNull List<@NotBlank String> skills,
        @Min(0) int workExperience
) {
}
