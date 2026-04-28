package ru.vk.education.job.repository;

import org.springframework.stereotype.Repository;
import ru.vk.education.job.domain.Employee;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
public class EmployeeRepository {
    private final CopyOnWriteArrayList<Employee> employees = new CopyOnWriteArrayList<>();

    /**
     * Create. Если пользователь с таким именем уже есть — игнорируем.
     */
    public void add(Employee employee) {
        boolean isExist = employees.stream()
                .anyMatch(e -> e.getName().equals(employee.getName()));
        if (!isExist) {
            employees.add(employee);
        }
    }

    public List<Employee> list() {
        return Collections.unmodifiableList(employees);
    }

    public Optional<Employee> findByName(String name) {
        return employees.stream().filter(e -> e.getName().equals(name)).findFirst();
    }

    /**
     * Update/Upsert: заменяет пользователя по имени. Возвращает true, если существующий был обновлён.
     */
    public boolean upsert(Employee employee) {
        for (int i = 0; i < employees.size(); i++) {
            if (employees.get(i).getName().equals(employee.getName())) {
                employees.set(i, employee);
                return true;
            }
        }
        employees.add(employee);
        return false;
    }

    /**
     * Delete. Возвращает true, если пользователь был удалён.
     */
    public boolean deleteByName(String name) {
        return employees.removeIf(e -> e.getName().equals(name));
    }
}
