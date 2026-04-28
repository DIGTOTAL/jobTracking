package ru.vk.education.job.service;

import org.springframework.stereotype.Service;
import ru.vk.education.job.domain.Employee;
import ru.vk.education.job.domain.Vacancy;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SuggestionService {

    //Возвращает limit(шт) лучших вакансий, отсортированный по релевантности для user.

    public List<Vacancy> suggestVacancies(Employee user, List<Vacancy> vacancies, int limit) {
        if (user == null || vacancies == null || limit < 0) {
            return Collections.emptyList();
        }

        Set<String> userSkills = user.getSkills() == null ? Collections.emptySet() : Set.copyOf(user.getSkills());
        return vacancies.stream()
                .map(vacancy -> {
                    long matchScore = userSkills.stream()
                            .filter(Objects::nonNull)
                            .filter(tag -> vacancy.getTags() != null && vacancy.getTags().contains(tag))
                            .count();
                    int score = (int) matchScore;
                    if (user.getWorkExperience() < vacancy.getRequiredWorkExperience()) {
                        score = score / 2;
                    }
                    return new AbstractMap.SimpleEntry<>(vacancy, score);
                })
                .filter(entry -> entry.getValue() > 0)
                .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
