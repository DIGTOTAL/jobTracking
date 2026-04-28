package ru.vk.education.job.service;

import org.springframework.stereotype.Service;
import ru.vk.education.job.domain.Employee;
import ru.vk.education.job.domain.Vacancy;
import ru.vk.education.job.repository.EmployeeRepository;
import ru.vk.education.job.repository.VacancyRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class BestOfferService {

    public record BestOfferResult(String employeeName, String vacancyName, String companyName) {
    }

    private final EmployeeRepository employeeRepository;
    private final VacancyRepository vacancyRepository;
    private final SuggestionService suggestionService;

    public BestOfferService(EmployeeRepository employeeRepository,
                            VacancyRepository vacancyRepository,
                            SuggestionService suggestionService) {
        this.employeeRepository = employeeRepository;
        this.vacancyRepository = vacancyRepository;
        this.suggestionService = suggestionService;
    }

    /**
     * Возвращает лучшие предложения для всех пользователей (по 1 вакансии).
     */
    public List<BestOfferResult> calculateBestOffers() {
        List<Employee> employees = employeeRepository.list();
        List<Vacancy> vacancies = vacancyRepository.list();

        List<BestOfferResult> result = new ArrayList<>();
        for (Employee employee : employees) {
            List<Vacancy> best = suggestionService.suggestVacancies(employee, vacancies, 1);
            if (best.isEmpty()) {
                continue;
            }
            Vacancy v = best.get(0);
            result.add(new BestOfferResult(employee.getName(), v.getVacancyName(), v.getCompanyName()));
        }
        return result;
    }
}
