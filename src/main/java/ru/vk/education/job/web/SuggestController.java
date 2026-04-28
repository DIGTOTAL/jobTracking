package ru.vk.education.job.web;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import ru.vk.education.job.domain.Employee;
import ru.vk.education.job.service.SuggestionService;
import ru.vk.education.job.domain.Vacancy;
import ru.vk.education.job.service.EmployeeService;
import ru.vk.education.job.service.VacancyService;
import ru.vk.education.job.web.dto.VacancyResponse;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SuggestController {

    private final EmployeeService employeeService;
    private final VacancyService vacancyService;
    private final SuggestionService suggestionService;

    public SuggestController(EmployeeService employeeService,
                             VacancyService vacancyService,
                             SuggestionService suggestionService) {
        this.employeeService = employeeService;
        this.vacancyService = vacancyService;
        this.suggestionService = suggestionService;
    }

    @GetMapping("/suggest/{userName}")
    public List<VacancyResponse> suggest(@PathVariable String userName,
                                        @RequestParam(defaultValue = "2") int limit) {
        if (limit < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "limit must be >= 0");
        }

        Employee user = employeeService.findByName(userName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        List<Vacancy> suggestions = suggestionService.suggestVacancies(user, vacancyService.list(), limit);
        return suggestions.stream().map(VacancyResponse::from).toList();
    }
}
