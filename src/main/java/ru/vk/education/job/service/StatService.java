package ru.vk.education.job.service;

import org.springframework.stereotype.Service;
import ru.vk.education.job.domain.Vacancy;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StatService {

    private final EmployeeService employeeService;
    private final VacancyService vacancyService;
    private final SuggestionService suggestionService;

    public StatService(EmployeeService employeeService,
                       VacancyService vacancyService,
                       SuggestionService suggestionService) {
        this.employeeService = employeeService;
        this.vacancyService = vacancyService;
        this.suggestionService = suggestionService;
    }

    public List<String> byExperience(int min) {
        return vacancyService.list().stream()
                .filter(v -> v.getRequiredWorkExperience() >= min)
                .map(v -> v.getVacancyName() + " at " + v.getCompanyName())
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    public List<String> byMatch(int min) {
        List<Vacancy> allVacancies = vacancyService.list();
        return employeeService.list().stream()
                .filter(user -> suggestionService.suggestVacancies(user, allVacancies, Integer.MAX_VALUE).size() >= min)
                .map(e -> {
                    String skills = String.join(",", e.getSkills());
                    int expToPrint = Math.min(e.getWorkExperience(), 5);
                    return e.getName() + " " + skills + " " + expToPrint;
                })
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    public List<String> topSkills(int limit) {
        Map<String, Long> freq = employeeService.list().stream()
                .flatMap(e -> e.getSkills().stream())
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()));

        return freq.entrySet().stream()
                .sorted(Comparator.<Map.Entry<String, Long>>comparingLong(Map.Entry::getValue)
                        .reversed()
                        .thenComparing(Map.Entry::getKey, String.CASE_INSENSITIVE_ORDER))
                .limit(limit)
                .map(Map.Entry::getKey)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }
}
