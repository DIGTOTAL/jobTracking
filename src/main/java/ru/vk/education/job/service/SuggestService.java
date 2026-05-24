package ru.vk.education.job.service;

import org.springframework.stereotype.Service;
import ru.vk.education.job.domain.Employee;
import ru.vk.education.job.domain.Vacancy;

import java.util.List;

@Service
public class SuggestService {

    private static final int DEFAULT_LIMIT = 2;

    private final EmployeeService employeeService;
    private final VacancyService vacancyService;
    private final SuggestionService suggestionService;

    public SuggestService(EmployeeService employeeService,
                          VacancyService vacancyService,
                          SuggestionService suggestionService) {
        this.employeeService = employeeService;
        this.vacancyService = vacancyService;
        this.suggestionService = suggestionService;
    }

    /**
     * Контракт ДЗ: на вход только имя пользователя.
     */
    public List<Vacancy> suggest(String userName) {
        return suggest(userName, DEFAULT_LIMIT);
    }

    public List<Vacancy> suggest(String userName, int limit) {
        Employee user = employeeService.findByName(userName).orElse(null);
        if (user == null) {
            return List.of();
        }
        return suggestionService.suggestVacancies(user, vacancyService.list(), limit);
    }
}
