package ru.vk.education.job.web;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import ru.vk.education.job.service.SuggestService;
import ru.vk.education.job.domain.Vacancy;
import ru.vk.education.job.web.dto.VacancyResponse;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SuggestController {

    private final SuggestService suggestService;

    public SuggestController(SuggestService suggestService) {
        this.suggestService = suggestService;
    }

    @GetMapping("/suggest/{userName}")
    public List<VacancyResponse> suggest(@PathVariable String userName,
                                        @RequestParam(defaultValue = "2") int limit) {
        if (limit < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "limit must be >= 0");
        }

        List<Vacancy> suggestions = suggestService.suggest(userName, limit);
        if (suggestions.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        return suggestions.stream().map(VacancyResponse::from).toList();
    }
}
