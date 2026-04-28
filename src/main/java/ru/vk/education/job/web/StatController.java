package ru.vk.education.job.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.vk.education.job.service.StatService;

import java.util.List;

@RestController
@RequestMapping("/api/stat")
public class StatController {

    private final StatService statService;

    public StatController(StatService statService) {
        this.statService = statService;
    }

    @GetMapping("/exp")
    public List<String> byExperience(@RequestParam(name = "min") int min) {
        if (min < 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "min must be >= 0");
        return statService.byExperience(min);
    }

    @GetMapping("/match")
    public List<String> byMatch(@RequestParam(name = "min") int min) {
        if (min < 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "min must be >= 0");
        return statService.byMatch(min);
    }

    @GetMapping("/top-skills")
    public List<String> topSkills(@RequestParam(name = "limit") int limit) {
        if (limit < 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "limit must be >= 0");
        return statService.topSkills(limit);
    }
}
