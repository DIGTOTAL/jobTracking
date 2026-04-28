package ru.vk.education.job.service;

import org.springframework.stereotype.Service;
import ru.vk.education.job.domain.Vacancy;
import ru.vk.education.job.repository.VacancyRepository;

import java.util.List;
import java.util.Optional;

@Service
public class VacancyService {

    private final VacancyRepository vacancyRepository;

    public VacancyService(VacancyRepository vacancyRepository) {
        this.vacancyRepository = vacancyRepository;
    }

    /**
     * Create. Если вакансия уже существует — репозиторий игнорирует.
     */
    public Vacancy create(Vacancy vacancy) {
        vacancyRepository.add(vacancy);
        return vacancy;
    }

    public List<Vacancy> list() {
        return vacancyRepository.list();
    }

    public Optional<Vacancy> findByName(String vacancyName) {
        return vacancyRepository.findByName(vacancyName);
    }

    /**
     * Upsert. Возвращает true, если был именно update (а не insert).
     */
    public boolean upsert(Vacancy vacancy) {
        return vacancyRepository.upsert(vacancy);
    }

    public boolean deleteByName(String vacancyName) {
        return vacancyRepository.deleteByName(vacancyName);
    }
}
