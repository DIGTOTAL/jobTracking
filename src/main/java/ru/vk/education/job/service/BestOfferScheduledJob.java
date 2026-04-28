package ru.vk.education.job.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.vk.education.job.BestOfferScheduler;

/**
 * Запуск BestOfferScheduler через Spring scheduling.
 */
@Component
public class BestOfferScheduledJob {

    private final BestOfferScheduler bestOfferScheduler;

    public BestOfferScheduledJob(BestOfferScheduler bestOfferScheduler) {
        this.bestOfferScheduler = bestOfferScheduler;
    }

    @Scheduled(
            fixedDelayString = "${job.best-offer.fixed-delay-ms:60000}",
            initialDelayString = "${job.best-offer.initial-delay-ms:0}"
    )
    public void run() {
        bestOfferScheduler.run();
    }
}
