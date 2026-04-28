package ru.vk.education.job.domain;

import java.util.List;

public class Vacancy {

    private final String vacancyName;
    private final String companyName;
    private final List<String> tags;
    private final int requiredWorkExperience;

    public Vacancy(String vacancyName, String companyName, List<String> tags, int requiredWorkExperience) {
        this.vacancyName = vacancyName;
        this.companyName = companyName;
        this.tags = tags;
        this.requiredWorkExperience = requiredWorkExperience;
    }

    public String getVacancyName() {
        return vacancyName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public List<String> getTags() {
        return tags;
    }

    public int getRequiredWorkExperience() {
        return requiredWorkExperience;
    }

}
