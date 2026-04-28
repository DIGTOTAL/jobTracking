package ru.vk.education.job.web.dto;

import ru.vk.education.job.domain.Vacancy;

import java.util.List;

public record VacancyResponse(String vacancyName, String companyName, List<String> tags, int requiredWorkExperience) {

    public static VacancyResponse from(Vacancy vacancy) {
        return new VacancyResponse(vacancy.getVacancyName(), vacancy.getCompanyName(), vacancy.getTags(), vacancy.getRequiredWorkExperience());
    }
}
