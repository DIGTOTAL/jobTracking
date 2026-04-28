package ru.vk.education.job.domain;

import java.util.List;

public class Employee {
    private final String name;
    private final List<String> skills;
    private final int workExperience;

    public Employee(String name, List<String> skills, int workExperience) {
        this.name = name;
        this.skills = skills;
        this.workExperience = workExperience;
    }

    public String getName() {
        return name;
    }

    public List<String> getSkills() {
        return skills;
    }

    public int getWorkExperience() {
        return workExperience;
    }
}
