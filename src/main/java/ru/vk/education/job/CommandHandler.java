package ru.vk.education.job;

import ru.vk.education.job.domain.Employee;
import ru.vk.education.job.domain.Vacancy;
import ru.vk.education.job.repository.EmployeeRepository;
import ru.vk.education.job.repository.VacancyRepository;
import ru.vk.education.job.service.SuggestionService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class CommandHandler {
    private static final int VACANCY_LIMIT = 2;

    private final EmployeeRepository employeeRepository;
    private final VacancyRepository vacancyRepository;
    private final SuggestionService suggestionService;

    public CommandHandler(EmployeeRepository employeeRepository, VacancyRepository vacancyRepository, SuggestionService suggestionService) {
        this.employeeRepository = employeeRepository;
        this.vacancyRepository = vacancyRepository;
        this.suggestionService = suggestionService;
    }

    public void handleUser(String[] args) {
        if (args.length < 2) {
//            System.out.println("Invalid command. Usage: user <name> --<skills> --<exp>");
            return;
        }
        String name = args[1];

        String skillsRaw = null;
        String workExperienceRaw = null;

        for (int i = 2; i < args.length; i++) {
            String part = args[i];
            if (part.startsWith("--skills=")) {
                skillsRaw = part.substring(part.indexOf("=") + 1);
            } else if (part.startsWith("--exp=")) {
                workExperienceRaw = part.substring(part.indexOf("=") + 1);
            }
        }

        int experience = 0;
        try {
            if (workExperienceRaw != null && !workExperienceRaw.isEmpty()) {
                experience = Integer.parseInt(workExperienceRaw);
            }
        } catch (NumberFormatException e) {
//            System.out.println("Invalid work experience value: " + workExperienceRaw);
        }

        if (skillsRaw == null) {
//            System.out.println("Skills not provided for user: " + name);
            return;
        }
        List<String> skills = Arrays.stream(skillsRaw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(TreeSet::new))
                .stream().toList();

        Employee employee = new Employee(name, skills, experience);
        employeeRepository.add(employee);
    }

    public void handleUserList() {
        for (Employee e : employeeRepository.list()) {
            String skills = String.join(",", e.getSkills());
            System.out.println(e.getName() + " " + skills + " " + e.getWorkExperience());
        }
    }

    public void handleJob(String[] parts) {
        if (parts.length < 2) return;
        String title = parts[1];

        String company = "";
        String tagsRaw = "";
        String expRaw = null;
        for (int i = 2; i < parts.length; i++) {
            String p = parts[i];
            if (p.startsWith("--company=")) company = p.substring(p.indexOf('=') + 1);
            else if (p.startsWith("--tags=")) tagsRaw = p.substring(p.indexOf('=') + 1);
            else if (p.startsWith("--exp=")) expRaw = p.substring(p.indexOf('=') + 1);
        }

        int exp = 0;
        try {
            if (expRaw != null && !expRaw.isEmpty()) exp = Integer.parseInt(expRaw);
        } catch (NumberFormatException nfe) {
//            System.out.println("Invalid experience value: " + expRaw);
        }

        List<String> tags = Arrays.stream(tagsRaw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty()).distinct().sorted().collect(Collectors.toList());

        Vacancy v = new Vacancy(title, company, tags, exp);
        vacancyRepository.add(v);
    }

    public void handleJobList() {
        vacancyRepository.list().stream()
                .map(v -> v.getVacancyName() + " at " + v.getCompanyName())
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .forEach(System.out::println);
    }

    public void handleSuggest(String[] parts) {
        if (parts.length < 2) return;
        String username = parts[1];

        Optional<Employee> opt = employeeRepository.list().stream()
                .filter(e -> e.getName().equals(username))
                .findFirst();
        if (opt.isEmpty()) return;

        Employee user = opt.get();
        List<Vacancy> suggestions = suggestionService.suggestVacancies(user, vacancyRepository.list(), VACANCY_LIMIT);
        for (Vacancy v : suggestions) {
            System.out.println(v.getVacancyName() + " at " + v.getCompanyName());
        }
    }

    public void handleStat(String[] parts) {
        if (parts == null || parts.length < 3) return;

        String statType = parts[1];
        int statValue;
        try {
            statValue = Integer.parseInt(parts[2]);
        } catch (NumberFormatException e) {
            return;
        }
        if (statValue < 0) return;

        switch (statType) {
            case "--exp" -> vacancyRepository.list().stream()
                    .filter(v -> v.getRequiredWorkExperience() >= statValue)
                    .map(v -> v.getVacancyName() + " at " + v.getCompanyName())
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .forEach(System.out::println);

            case "--match" -> {
                List<Vacancy> allVacancies = vacancyRepository.list();

                employeeRepository.list().stream()
                        .filter(user -> suggestionService
                                .suggestVacancies(user, allVacancies, Integer.MAX_VALUE)
                                .size() >= statValue)
                        .map(e -> {
                            String skills = String.join(",", e.getSkills());
                            int expToPrint = Math.min(e.getWorkExperience(), 5);
                            return e.getName() + " " + skills + " " + expToPrint;
                        })
                        .sorted(String.CASE_INSENSITIVE_ORDER)
                        .forEach(System.out::println);
            }

            case "--top-skills" -> {
                java.util.Map<String, Long> freq = employeeRepository.list().stream()
                        .flatMap(e -> e.getSkills().stream())
                        .collect(Collectors.groupingBy(s -> s, Collectors.counting()));

                java.util.List<String> topSkills = freq.entrySet().stream()
                        .sorted(java.util.Comparator.<java.util.Map.Entry<String, Long>>comparingLong(java.util.Map.Entry::getValue)
                                .reversed()
                                .thenComparing(java.util.Map.Entry::getKey, String.CASE_INSENSITIVE_ORDER))
                        .limit(statValue)
                        .map(java.util.Map.Entry::getKey)
                        .sorted(String.CASE_INSENSITIVE_ORDER)
                        .toList();

                topSkills.forEach(System.out::println);
            }
        }
    }
}
