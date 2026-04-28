package ru.vk.education.job;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Точка входа для запуска Spring Boot режима (HTTP + scheduler).
 *
 * CLI режим остаётся в {@link Main}.
 */
@SpringBootApplication
@EnableScheduling
public class TestApp {

    public static void main(String[] args) {
        SpringApplication.run(TestApp.class, args);
    }
}
