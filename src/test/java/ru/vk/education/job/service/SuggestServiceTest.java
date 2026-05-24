package ru.vk.education.job.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.vk.education.job.domain.Employee;
import ru.vk.education.job.domain.Vacancy;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SuggestServiceTest {

    @Mock
    EmployeeService employeeService;

    @Mock
    VacancyService vacancyService;

    @Mock
    SuggestionService suggestionService;

    @InjectMocks
    SuggestService suggestService;

    @Test
    void emptyVacanciesTest() {
        Employee user = new Employee("Alice", List.of("java"), 3);
        when(employeeService.findByName("Alice")).thenReturn(Optional.of(user));
        when(vacancyService.list()).thenReturn(List.of());
        when(suggestionService.suggestVacancies(user, List.of(), 2)).thenReturn(List.of());

        assertThat(suggestService.suggest("Alice", 2)).isEmpty();

        verify(employeeService).findByName("Alice");
        verify(vacancyService).list();
        verify(suggestionService).suggestVacancies(user, List.of(), 2);
        verifyNoMoreInteractions(employeeService, vacancyService, suggestionService);
    }

    @Test
    void singleVacancyTest() {
        Employee user = new Employee("Alice", List.of("java"), 3);
        Vacancy v1 = new Vacancy("JavaDev", "ACME", List.of("java"), 0);

        when(employeeService.findByName("Alice")).thenReturn(Optional.of(user));
        when(vacancyService.list()).thenReturn(List.of(v1));
        when(suggestionService.suggestVacancies(user, List.of(v1), 2)).thenReturn(List.of(v1));

        assertThat(suggestService.suggest("Alice", 2)).containsExactly(v1);

        verify(employeeService).findByName("Alice");
        verify(vacancyService).list();
        verify(suggestionService).suggestVacancies(user, List.of(v1), 2);
        verifyNoMoreInteractions(employeeService, vacancyService, suggestionService);
    }

    @Test
    void suggest_whenUserNotFound_returnsEmpty_andDoesNotCallRanking() {
        when(employeeService.findByName("NoUser")).thenReturn(Optional.empty());

        assertThat(suggestService.suggest("NoUser", 2)).isEmpty();

        verify(employeeService).findByName("NoUser");
        verifyNoInteractions(vacancyService);
        verifyNoInteractions(suggestionService);
    }

    @Test
    void suggest_passesLimitToRankingService() {
        Employee user = new Employee("Bob", List.of("java"), 3);
        Vacancy v1 = new Vacancy("V1", "C1", List.of("java"), 0);
        List<Vacancy> all = List.of(v1);

        when(employeeService.findByName("Bob")).thenReturn(Optional.of(user));
        when(vacancyService.list()).thenReturn(all);
        when(suggestionService.suggestVacancies(user, all, 5)).thenReturn(all);

        assertThat(suggestService.suggest("Bob", 5)).containsExactly(v1);

        verify(employeeService).findByName("Bob");
        verify(vacancyService).list();
        verify(suggestionService).suggestVacancies(user, all, 5);
        verifyNoMoreInteractions(employeeService, vacancyService, suggestionService);
    }
}
