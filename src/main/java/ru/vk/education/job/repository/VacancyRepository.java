package ru.vk.education.job.repository;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.vk.education.job.domain.Vacancy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class VacancyRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public VacancyRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Create. Если вакансия с таким именем уже есть — игнорируем.
     */
    @Transactional
    public void add(Vacancy vacancy) {
        if (vacancy == null) return;

        try {
            int inserted = jdbc.update(
                    "INSERT INTO vacancies(vacancy_name, company_name, required_work_experience) " +
                            "VALUES(:vacancyName, :companyName, :requiredWorkExperience)",
                    Map.of(
                            "vacancyName", vacancy.getVacancyName(),
                            "companyName", vacancy.getCompanyName(),
                            "requiredWorkExperience", vacancy.getRequiredWorkExperience()
                    )
            );

            if (inserted > 0) {
                replaceTags(vacancy.getVacancyName(), vacancy.getTags());
            }
        } catch (DuplicateKeyException ignore) {
            // игнорируем дубль по PK
        }
    }

    public List<Vacancy> list() {
        List<VacancyRow> rows = jdbc.query(
                "SELECT vacancy_name, company_name, required_work_experience FROM vacancies ORDER BY vacancy_name",
                (rs, rowNum) -> new VacancyRow(
                        rs.getString("vacancy_name"),
                        rs.getString("company_name"),
                        rs.getInt("required_work_experience")
                )
        );

        if (rows.isEmpty()) return List.of();

        List<String> names = rows.stream().map(r -> r.vacancyName).toList();
        Map<String, List<String>> tagsByVacancy = loadTagsByVacancyNames(names);

        List<Vacancy> result = new ArrayList<>(rows.size());
        for (VacancyRow r : rows) {
            List<String> tags = tagsByVacancy.getOrDefault(r.vacancyName, List.of());
            result.add(new Vacancy(r.vacancyName, r.companyName, tags, r.requiredWorkExperience));
        }

        return Collections.unmodifiableList(result);
    }

    public Optional<Vacancy> findByName(String vacancyName) {
        if (vacancyName == null) return Optional.empty();

        List<VacancyRow> rows = jdbc.query(
                "SELECT vacancy_name, company_name, required_work_experience FROM vacancies WHERE vacancy_name = :name",
                Map.of("name", vacancyName),
                (rs, rowNum) -> new VacancyRow(
                        rs.getString("vacancy_name"),
                        rs.getString("company_name"),
                        rs.getInt("required_work_experience")
                )
        );

        if (rows.isEmpty()) return Optional.empty();

        List<String> tags = jdbc.query(
                "SELECT tag FROM vacancy_tags WHERE vacancy_name = :name ORDER BY tag",
                Map.of("name", vacancyName),
                (rs, rowNum) -> rs.getString("tag")
        );

        VacancyRow r = rows.get(0);
        return Optional.of(new Vacancy(r.vacancyName, r.companyName, tags, r.requiredWorkExperience));
    }

    /**
     * Update/Upsert: заменяет вакансию по vacancyName. Возвращает true, если существующая была обновлена.
     */
    @Transactional
    public boolean upsert(Vacancy vacancy) {
        if (vacancy == null) return false;

        int updated = jdbc.update(
                "UPDATE vacancies SET company_name = :companyName, required_work_experience = :requiredWorkExperience " +
                        "WHERE vacancy_name = :vacancyName",
                Map.of(
                        "vacancyName", vacancy.getVacancyName(),
                        "companyName", vacancy.getCompanyName(),
                        "requiredWorkExperience", vacancy.getRequiredWorkExperience()
                )
        );

        if (updated > 0) {
            replaceTags(vacancy.getVacancyName(), vacancy.getTags());
            return true;
        }

        try {
            jdbc.update(
                    "INSERT INTO vacancies(vacancy_name, company_name, required_work_experience) " +
                            "VALUES(:vacancyName, :companyName, :requiredWorkExperience)",
                    Map.of(
                            "vacancyName", vacancy.getVacancyName(),
                            "companyName", vacancy.getCompanyName(),
                            "requiredWorkExperience", vacancy.getRequiredWorkExperience()
                    )
            );
            replaceTags(vacancy.getVacancyName(), vacancy.getTags());
            return false;
        } catch (DuplicateKeyException e) {
            // гонка: кто-то вставил — считаем как update
            replaceTags(vacancy.getVacancyName(), vacancy.getTags());
            return true;
        }
    }

    /**
     * Delete. Возвращает true, если вакансия была удалена.
     */
    @Transactional
    public boolean deleteByName(String vacancyName) {
        if (vacancyName == null) return false;
        int deleted = jdbc.update("DELETE FROM vacancies WHERE vacancy_name = :name", Map.of("name", vacancyName));
        // vacancy_tags удалятся каскадом
        return deleted > 0;
    }

    private void replaceTags(String vacancyName, List<String> tags) {
        jdbc.update(
                "DELETE FROM vacancy_tags WHERE vacancy_name = :name",
                Map.of("name", vacancyName)
        );

        if (tags == null || tags.isEmpty()) return;

        List<MapSqlParameterSource> batch = new ArrayList<>(tags.size());
        for (String tag : tags) {
            if (tag == null) continue;
            String trimmed = tag.trim();
            if (trimmed.isEmpty()) continue;

            batch.add(new MapSqlParameterSource()
                    .addValue("name", vacancyName)
                    .addValue("tag", trimmed));
        }
        if (batch.isEmpty()) return;

        jdbc.batchUpdate(
                "INSERT INTO vacancy_tags(vacancy_name, tag) VALUES(:name, :tag)",
                batch.toArray(MapSqlParameterSource[]::new)
        );
    }

    private Map<String, List<String>> loadTagsByVacancyNames(List<String> names) {
        List<Map.Entry<String, String>> rows = jdbc.query(
                "SELECT vacancy_name, tag FROM vacancy_tags WHERE vacancy_name IN (:names) ORDER BY vacancy_name, tag",
                Map.of("names", names),
                (rs, rowNum) -> Map.entry(rs.getString("vacancy_name"), rs.getString("tag"))
        );

        java.util.HashMap<String, List<String>> map = new java.util.HashMap<>();
        for (Map.Entry<String, String> r : rows) {
            map.computeIfAbsent(r.getKey(), k -> new ArrayList<>()).add(r.getValue());
        }
        map.replaceAll((k, v) -> List.copyOf(v));
        return map;
    }

    private record VacancyRow(String vacancyName, String companyName, int requiredWorkExperience) {
    }
}
