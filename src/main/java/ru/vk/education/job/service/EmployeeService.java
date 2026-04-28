package ru.vk.education.job.service;

import org.springframework.stereotype.Service;
import ru.vk.education.job.domain.Employee;
import ru.vk.education.job.repository.EmployeeRepository;

import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    /**
     * Create. Если пользователь уже существует — репозиторий игнорирует.
     */
    public Employee create(Employee employee) {
        employeeRepository.add(employee);
        return employee;
    }

    public List<Employee> list() {
        return employeeRepository.list();
    }

    public Optional<Employee> findByName(String name) {
        return employeeRepository.findByName(name);
    }

    /**
     * Upsert. Возвращает true, если был именно update (а не insert).
     */
    public boolean upsert(Employee employee) {
        return employeeRepository.upsert(employee);
    }

    public boolean deleteByName(String name) {
        return employeeRepository.deleteByName(name);
    }
}
