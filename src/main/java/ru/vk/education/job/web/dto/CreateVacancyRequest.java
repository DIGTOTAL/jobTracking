package ru.vk.education.job.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateVacancyRequest(
        @NotBlank String vacancyName,
        @NotBlank String companyName,
        @NotNull List<@NotBlank String> tags,
        @Min(0) int requiredWorkExperience
) {
}
