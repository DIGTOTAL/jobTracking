package ru.vk.education.job.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.vk.education.job.domain.Employee;
import ru.vk.education.job.domain.Vacancy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@Testcontainers
@SpringBootTest
class SuggestServiceIntegrationTest {

    private static final boolean dockerAvailable;

    static final PostgreSQLContainer<?> postgres;

    static {
        boolean available;
        try {
            available = DockerClientFactory.instance().isDockerAvailable();
        } catch (Throwable t) {
            available = false;
        }
        dockerAvailable = available;

        if (dockerAvailable) {
            postgres = new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("job_tracking")
                    .withUsername("job_user")
                    .withPassword("job_password");
            postgres.start();
        } else {
            postgres = null;
        }
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        if (!dockerAvailable) {
            return;
        }
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
    }

    @Autowired
    SuggestService suggestService;

    @Autowired
    EmployeeService employeeService;

    @Autowired
    VacancyService vacancyService;

    @Test
    void suggest_readsFromRealDb_andReturnsMatchedVacancies() {
        assumeTrue(dockerAvailable, "Docker недоступен — пропускаем Testcontainers тест");

        String userName = "it_user";
        String v1Name = "it_vacancy_java";
        String v2Name = "it_vacancy_python";

        employeeService.deleteByName(userName);
        vacancyService.deleteByName(v1Name);
        vacancyService.deleteByName(v2Name);

        employeeService.create(new Employee(userName, java.util.List.of("java", "sql"), 3));
        vacancyService.create(new Vacancy(v1Name, "ACME", java.util.List.of("java"), 0));
        vacancyService.create(new Vacancy(v2Name, "ACME", java.util.List.of("python"), 0));

        var result = suggestService.suggest(userName, 10);

        assertThat(result)
                .extracting(Vacancy::getVacancyName)
                .contains(v1Name)
                .doesNotContain(v2Name);
    }
}
