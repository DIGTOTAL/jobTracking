package ru.vk.education.job.web;

import org.springframework.web.bind.annotation.*;
import ru.vk.education.job.service.BestOfferService;

import java.util.List;

@RestController
@RequestMapping("/api/best-offer")
public class BestOfferController {

    private final BestOfferService bestOfferService;

    public BestOfferController(BestOfferService bestOfferService) {
        this.bestOfferService = bestOfferService;
    }

    /**
     * Ручной запуск поиска лучшего предложения для всех пользователей.
     */
    @PostMapping("/run")
    public List<BestOfferService.BestOfferResult> run() {
        return bestOfferService.calculateBestOffers();
    }
}
