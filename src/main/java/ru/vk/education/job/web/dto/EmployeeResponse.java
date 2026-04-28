package ru.vk.education.job.web.dto;

import ru.vk.education.job.domain.Employee;

import java.util.List;

public record EmployeeResponse(String name, List<String> skills, int workExperience) {

    public static EmployeeResponse from(Employee employee) {
        return new EmployeeResponse(employee.getName(), employee.getSkills(), employee.getWorkExperience());
    }
}
