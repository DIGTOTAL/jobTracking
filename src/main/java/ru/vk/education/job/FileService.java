package ru.vk.education.job;

import java.io.IOException;
import java.nio.file.*;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileService {
    private final Path file;

    public FileService() {
        this(Paths.get("commands.txt"));
    }

    public FileService(Path file) {
        this.file = file;
        Path parent = file.getParent() == null ? Paths.get(".") : file.getParent();
        try {
            if (Files.notExists(this.file)) {
                Files.createDirectories(parent);
                Files.createFile(this.file);
            }
        } catch (IOException e) {
            System.err.println("Failed to initialize commands file: " + this.file + " - " + e.getMessage());
            throw new IllegalStateException("Failed to initialize commands file: " + this.file, e);
        }
    }

    // Сохраняет (дописывает) одну выполненную команду в файл, но никогда не сохраняет "exit"
    public synchronized void saveCommand(String command) {
        if (command == null || command.trim().isEmpty()) return;
        String trimmed = command.trim();

        // Проверяем первый токен — учтёт случаи "exit", "exit now" и т.п.
        String firstToken = trimmed.split("\\s+")[0];
        if ("exit".equals(firstToken)) return; // не записываем exit в файл

        try {
            // Убедиться, что файл и директория существуют
            Path parent = file.getParent() == null ? Paths.get(".") : file.getParent();
            if (Files.notExists(parent)) Files.createDirectories(parent);
            if (Files.notExists(file)) Files.createFile(file);

            // Проверить последний символ файла — если нет перевода строки, добавить его перед записью
            boolean needsPrefixNewline = false;
            if (Files.size(file) > 0) {
                byte[] all = Files.readAllBytes(file);
                char last = (char) all[all.length - 1];
                needsPrefixNewline = last != '\n' && last != '\r';
            }

            String toWrite = (needsPrefixNewline ? System.lineSeparator() : "") + trimmed + System.lineSeparator();
            Files.writeString(file, toWrite, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Failed to save command: " + e.getMessage());
        }
    }

    // Возвращает список ранее введённых команд (пустой список при ошибке)
    public List<String> readCommands() {
        try {
            if (Files.notExists(file)) return Collections.emptyList();
            try (Stream<String> lines = Files.lines(file)) {
                return lines
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList());
            }
        } catch (IOException e) {
            System.err.println("Failed to read commands: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}