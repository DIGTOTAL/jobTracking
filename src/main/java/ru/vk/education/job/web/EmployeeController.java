package ru.vk.education.job.web;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.vk.education.job.domain.Employee;
import ru.vk.education.job.service.EmployeeService;
import ru.vk.education.job.web.dto.CreateEmployeeRequest;
import ru.vk.education.job.web.dto.EmployeeResponse;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeResponse create(@Valid @RequestBody CreateEmployeeRequest request) {
        Employee employee = new Employee(request.name(), request.skills(), request.workExperience());
        return EmployeeResponse.from(employeeService.create(employee));
    }

    @PutMapping("/{name}")
    public EmployeeResponse upsert(@PathVariable String name, @Valid @RequestBody CreateEmployeeRequest request) {
        if (!name.equals(request.name())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Path name must match body name");
        }

        Employee employee = new Employee(request.name(), request.skills(), request.workExperience());
        employeeService.upsert(employee);
        return EmployeeResponse.from(employee);
    }

    @DeleteMapping("/{name}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String name) {
        boolean deleted = employeeService.deleteByName(name);
        if (!deleted) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
    }

    @GetMapping
    public List<EmployeeResponse> list() {
        return employeeService.list().stream()
                .sorted(Comparator.comparing(Employee::getName, String.CASE_INSENSITIVE_ORDER))
                .map(EmployeeResponse::from)
                .toList();
    }

    @GetMapping("/{name}")
    public EmployeeResponse get(@PathVariable String name) {
        return employeeService.findByName(name)
                .map(EmployeeResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
