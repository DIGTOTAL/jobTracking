package ru.vk.education.job;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.ConfigurableApplicationContext;
import ru.vk.education.job.repository.EmployeeRepository;
import ru.vk.education.job.repository.VacancyRepository;
import ru.vk.education.job.service.SuggestionService;

import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // Поднимаем Spring контекст в headless/CLI режиме (без встроенного веб-сервера)
        ConfigurableApplicationContext ctx = new SpringApplicationBuilder(TestApp.class)
                .web(WebApplicationType.NONE)
                .run(args);

        // Получаем бины из контекста — теперь CLI и Web будут работать с одними и теми же репозиториями
        EmployeeRepository employeeRepository = ctx.getBean(EmployeeRepository.class);
        VacancyRepository vacancyRepository = ctx.getBean(VacancyRepository.class);
        SuggestionService suggestionService = ctx.getBean(SuggestionService.class);

        CommandHandler commandHandler = new CommandHandler(employeeRepository, vacancyRepository, suggestionService);
        FileService fileService = new FileService();

        // Регистрируем hook для закрытия Spring-контекста при завершении программы
        Runtime.getRuntime().addShutdownHook(new Thread(ctx::close));

        // Выполнить команды, сохранённые ранее (только создание сущностей)
        List<String> savedCommands = fileService.readCommands();
        for (String cmdLine : savedCommands) {
            if (cmdLine == null || cmdLine.trim().isEmpty()) continue;
            String trimmed = cmdLine.trim();

            String[] tokens = trimmed.split("\\s+");
            if (tokens.length == 0) continue;
            String firstToken = tokens[0];

            if (!"user".equals(firstToken) && !"job".equals(firstToken)) continue;

            boolean shouldExit = executeLine(trimmed, commandHandler, fileService);
            if (shouldExit) {
                ctx.close();
                return;
            }
        }

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                if (!scanner.hasNextLine()) break;
                String raw = scanner.nextLine();
                if (raw == null) break;
                raw = raw.trim();
                if (raw.isEmpty()) continue;

                // Разбиваем одну вставку/строку на несколько команд по ключевым словам
                String[] commands = raw.split("(?=\\buser\\b|\\bjob\\b|\\buser-list\\b|\\bjob-list\\b|\\bsuggest\\b|\\bhistory\\b|\\bexit\\b)");

                boolean shouldExitOverall = false;
                for (String cmdPart : commands) {
                    String cmd = cmdPart == null ? "" : cmdPart.trim();
                    if (cmd.isEmpty()) continue;

                    String[] parts = cmd.split("\\s+");
                    if (parts.length == 0) continue;
                    String first = parts[0];

                    // Если команда exit — сразу выйти, не сохранять
                    if ("exit".equals(first)) {
                        shouldExitOverall = true;
                        break;
                    }

                    // Выполнить команду и сохранить её (saveCommand игнорирует exit)
                    boolean shouldExit = executeLine(cmd, commandHandler, fileService);
                    fileService.saveCommand(cmd);
                    if (shouldExit) {
                        shouldExitOverall = true;
                        break;
                    }
                }

                if (shouldExitOverall) break;
            }
        }

        ctx.close();
    }

    private static boolean executeLine(String line, CommandHandler commandHandler, FileService fileService) {
        String[] parts = line.split("\\s+");
        if (parts.length == 0) return false;
        String command = parts[0];

        switch (command) {
            case "user" -> commandHandler.handleUser(parts);
            case "user-list" -> commandHandler.handleUserList();
            case "job" -> commandHandler.handleJob(parts);
            case "job-list" -> commandHandler.handleJobList();
            case "suggest" -> commandHandler.handleSuggest(parts);
            case "stat" -> commandHandler.handleStat(parts);
            case "history" -> {
                List<String> history = fileService.readCommands();
                for (String h : history) {
                    System.out.println(h);
                }
            }
            case "exit" -> {
                return true;
            }
            default -> {
                // unknown command — игнорируем
            }
        }
        return false;
    }
}