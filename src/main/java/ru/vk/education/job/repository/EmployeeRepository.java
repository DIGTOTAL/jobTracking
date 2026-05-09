package ru.vk.education.job.repository;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.vk.education.job.domain.Employee;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class EmployeeRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public EmployeeRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Create. Если пользователь с таким именем уже есть — игнорируем.
     */
    @Transactional
    public void add(Employee employee) {
        if (employee == null) return;

        try {
            int inserted = jdbc.update(
                    "INSERT INTO employees(name, work_experience) VALUES(:name, :workExperience)",
                    Map.of(
                            "name", employee.getName(),
                            "workExperience", employee.getWorkExperience()
                    )
            );

            if (inserted > 0) {
                replaceSkills(employee.getName(), employee.getSkills());
            }
        } catch (DuplicateKeyException ignore) {
            // игнорируем дубль по PK
        }
    }

    public List<Employee> list() {
        List<EmployeeRow> rows = jdbc.query(
                "SELECT name, work_experience FROM employees ORDER BY name",
                (rs, rowNum) -> new EmployeeRow(rs.getString("name"), rs.getInt("work_experience"))
        );

        if (rows.isEmpty()) return List.of();

        List<String> names = rows.stream().map(r -> r.name).toList();
        Map<String, List<String>> skillsByName = loadSkillsByEmployeeNames(names);

        List<Employee> result = new ArrayList<>(rows.size());
        for (EmployeeRow r : rows) {
            List<String> skills = skillsByName.getOrDefault(r.name, List.of());
            result.add(new Employee(r.name, skills, r.workExperience));
        }
        return Collections.unmodifiableList(result);
    }

    public Optional<Employee> findByName(String name) {
        if (name == null) return Optional.empty();

        List<EmployeeRow> rows = jdbc.query(
                "SELECT name, work_experience FROM employees WHERE name = :name",
                Map.of("name", name),
                (rs, rowNum) -> new EmployeeRow(rs.getString("name"), rs.getInt("work_experience"))
        );

        if (rows.isEmpty()) return Optional.empty();

        List<String> skills = jdbc.query(
                "SELECT skill FROM employee_skills WHERE employee_name = :name ORDER BY skill",
                Map.of("name", name),
                (rs, rowNum) -> rs.getString("skill")
        );

        EmployeeRow r = rows.get(0);
        return Optional.of(new Employee(r.name, skills, r.workExperience));
    }

    /**
     * Update/Upsert: заменяет пользователя по имени. Возвращает true, если существующий был обновлён.
     */
    @Transactional
    public boolean upsert(Employee employee) {
        if (employee == null) return false;

        int updated = jdbc.update(
                "UPDATE employees SET work_experience = :workExperience WHERE name = :name",
                Map.of(
                        "name", employee.getName(),
                        "workExperience", employee.getWorkExperience()
                )
        );

        if (updated > 0) {
            replaceSkills(employee.getName(), employee.getSkills());
            return true;
        }

        // не было — пытаемся вставить
        try {
            jdbc.update(
                    "INSERT INTO employees(name, work_experience) VALUES(:name, :workExperience)",
                    Map.of(
                            "name", employee.getName(),
                            "workExperience", employee.getWorkExperience()
                    )
            );
            replaceSkills(employee.getName(), employee.getSkills());
            return false;
        } catch (DuplicateKeyException e) {
            // редкая гонка: кто-то вставил между UPDATE и INSERT, тогда считаем как update
            replaceSkills(employee.getName(), employee.getSkills());
            return true;
        }
    }

    /**
     * Delete. Возвращает true, если пользователь был удалён.
     */
    @Transactional
    public boolean deleteByName(String name) {
        if (name == null) return false;
        int deleted = jdbc.update("DELETE FROM employees WHERE name = :name", Map.of("name", name));
        // employee_skills удалятся каскадом
        return deleted > 0;
    }

    private void replaceSkills(String employeeName, List<String> skills) {
        jdbc.update(
                "DELETE FROM employee_skills WHERE employee_name = :name",
                Map.of("name", employeeName)
        );

        if (skills == null || skills.isEmpty()) return;

        List<MapSqlParameterSource> batch = new ArrayList<>(skills.size());
        for (String skill : skills) {
            if (skill == null) continue;
            String trimmed = skill.trim();
            if (trimmed.isEmpty()) continue;

            batch.add(new MapSqlParameterSource()
                    .addValue("name", employeeName)
                    .addValue("skill", trimmed));
        }
        if (batch.isEmpty()) return;

        jdbc.batchUpdate(
                "INSERT INTO employee_skills(employee_name, skill) VALUES(:name, :skill)",
                batch.toArray(MapSqlParameterSource[]::new)
        );
    }

    private Map<String, List<String>> loadSkillsByEmployeeNames(List<String> names) {
        List<Map.Entry<String, String>> rows = jdbc.query(
                "SELECT employee_name, skill FROM employee_skills WHERE employee_name IN (:names) ORDER BY employee_name, skill",
                Map.of("names", names),
                (rs, rowNum) -> Map.entry(rs.getString("employee_name"), rs.getString("skill"))
        );

        java.util.HashMap<String, List<String>> map = new java.util.HashMap<>();
        for (Map.Entry<String, String> r : rows) {
            map.computeIfAbsent(r.getKey(), k -> new ArrayList<>()).add(r.getValue());
        }
        map.replaceAll((k, v) -> List.copyOf(v));
        return map;
    }

    private record EmployeeRow(String name, int workExperience) {
    }
}
