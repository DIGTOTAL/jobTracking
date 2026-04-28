package ru.vk.education.job.repository;

import org.springframework.stereotype.Repository;
import ru.vk.education.job.domain.Vacancy;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
public class VacancyRepository {
    private final CopyOnWriteArrayList<Vacancy> vacancies = new CopyOnWriteArrayList<>();

    /**
     * Create. Если вакансия с таким именем уже есть — игнорируем.
     */
    public void add(Vacancy vacancy) {
        boolean isExist = vacancies.stream()
                .anyMatch(v -> v.getVacancyName().equals(vacancy.getVacancyName()));
        if (!isExist) {
            vacancies.add(vacancy);
        }
    }

    public List<Vacancy> list() {
        return Collections.unmodifiableList(vacancies);
    }

    public Optional<Vacancy> findByName(String vacancyName) {
        return vacancies.stream().filter(v -> v.getVacancyName().equals(vacancyName)).findFirst();
    }

    /**
     * Update/Upsert: заменяет вакансию по vacancyName. Возвращает true, если существующая была обновлена.
     */
    public boolean upsert(Vacancy vacancy) {
        for (int i = 0; i < vacancies.size(); i++) {
            if (vacancies.get(i).getVacancyName().equals(vacancy.getVacancyName())) {
                vacancies.set(i, vacancy);
                return true;
            }
        }
        vacancies.add(vacancy);
        return false;
    }

    /**
     * Delete. Возвращает true, если вакансия была удалена.
     */
    public boolean deleteByName(String vacancyName) {
        return vacancies.removeIf(v -> v.getVacancyName().equals(vacancyName));
    }
}
