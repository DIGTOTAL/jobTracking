package ru.vk.education.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.vk.education.job.domain.Employee;
import ru.vk.education.job.domain.Vacancy;
import ru.vk.education.job.repository.EmployeeRepository;
import ru.vk.education.job.repository.VacancyRepository;
import ru.vk.education.job.service.SuggestionService;

import java.util.List;

@Component
public class BestOfferScheduler implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(BestOfferScheduler.class);

    private final EmployeeRepository employeeRepository;
    private final VacancyRepository vacancyRepository;
    private final SuggestionService suggestionService;

    public BestOfferScheduler(
            EmployeeRepository employeeRepository,
            VacancyRepository vacancyRepository,
            SuggestionService suggestionService
    ) {
        this.employeeRepository = employeeRepository;
        this.vacancyRepository = vacancyRepository;
        this.suggestionService = suggestionService;
    }

    @Override
    public void run() {
        List<Employee> employees = employeeRepository.list();
        List<Vacancy> vacancies = vacancyRepository.list();

        for (Employee employee : employees) {
            List<Vacancy> best = suggestionService.suggestVacancies(employee, vacancies, 1);

            if (best.isEmpty()) {
                log.info("{} — нет подходящих вакансий", employee.getName());
                continue;
            }

            Vacancy v = best.get(0);
            log.info("{}, лучшее предложение — {} в {}", employee.getName(), v.getVacancyName(), v.getCompanyName());
        }
    }
}