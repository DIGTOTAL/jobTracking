package ru.vk.education.job.web;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.vk.education.job.domain.Vacancy;
import ru.vk.education.job.service.VacancyService;
import ru.vk.education.job.web.dto.CreateVacancyRequest;
import ru.vk.education.job.web.dto.VacancyResponse;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/vacancies")
public class VacancyController {

    private final VacancyService vacancyService;

    public VacancyController(VacancyService vacancyService) {
        this.vacancyService = vacancyService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VacancyResponse create(@Valid @RequestBody CreateVacancyRequest request) {
        Vacancy vacancy = new Vacancy(request.vacancyName(), request.companyName(), request.tags(), request.requiredWorkExperience());
        return VacancyResponse.from(vacancyService.create(vacancy));
    }

    @PutMapping("/{vacancyName}")
    public VacancyResponse upsert(@PathVariable String vacancyName, @Valid @RequestBody CreateVacancyRequest request) {
        if (!vacancyName.equals(request.vacancyName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Path vacancyName must match body vacancyName");
        }

        Vacancy vacancy = new Vacancy(request.vacancyName(), request.companyName(), request.tags(), request.requiredWorkExperience());
        vacancyService.upsert(vacancy);
        return VacancyResponse.from(vacancy);
    }

    @DeleteMapping("/{vacancyName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String vacancyName) {
        boolean deleted = vacancyService.deleteByName(vacancyName);
        if (!deleted) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Vacancy not found");
        }
    }

    @GetMapping
    public List<VacancyResponse> list() {
        return vacancyService.list().stream()
                .sorted(Comparator.comparing(Vacancy::getVacancyName, String.CASE_INSENSITIVE_ORDER))
                .map(VacancyResponse::from)
                .toList();
    }

    @GetMapping("/{vacancyName}")
    public VacancyResponse get(@PathVariable String vacancyName) {
        return vacancyService.findByName(vacancyName)
                .map(VacancyResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vacancy not found"));
    }
}
