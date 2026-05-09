package ru.vk.education.job;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.vk.education.job.repository.EmployeeRepository;
import ru.vk.education.job.repository.VacancyRepository;
import ru.vk.education.job.service.SuggestionService;

/**
 * Точка входа для запуска Spring Boot режима (HTTP + scheduler).
 * Чтобы запустить как CLI через bootRun/bootJar:
 *   --app.cli=true
 */
@SpringBootApplication
@EnableScheduling
public class TestApp {

    public static void main(String[] args) {
        SpringApplication.run(TestApp.class, args);
    }

    @Bean
    @ConditionalOnProperty(name = "app.cli", havingValue = "true")
    ApplicationRunner cliRunner(EmployeeRepository employeeRepository,
                                VacancyRepository vacancyRepository,
                                SuggestionService suggestionService) {
        return args -> Main.runCli(employeeRepository, vacancyRepository, suggestionService);
    }
}
