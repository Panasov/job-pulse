package com.jobpulse.processing.controller;

import com.jobpulse.processing.ParsedVacancy;
import com.jobpulse.processing.SalaryParser;
import com.jobpulse.processing.SkillParser;
import com.jobpulse.processing.VacancyRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/api")
public class VacancyController {

    private final VacancyRepository vacancyRepository;

    public VacancyController(VacancyRepository vacancyRepository) {
        this.vacancyRepository = vacancyRepository;
    }

    @GetMapping("/vacancies")
    @ResponseBody
    public List<ParsedVacancy> getAllVacancies() {
        return vacancyRepository.findAll();
    }

    @GetMapping("/analytics/top-companies")
    @ResponseBody
    public List<Map.Entry<String, Long>> getTopCompanies() {
        List<ParsedVacancy> all = vacancyRepository.findAll();

        return all.stream()
                .filter(v -> v.getCompany() != null && !v.getCompany().isBlank())
                .collect(Collectors.groupingBy(ParsedVacancy::getCompany, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toList());
    }

    @GetMapping("/analytics/average-salary")
    @ResponseBody
    public Map<String, Object> getAverageSalary() {
        List<ParsedVacancy> all = vacancyRepository.findAll();

        List<Integer> salaries = all.stream()
                .map(v -> SalaryParser.parseSalary(v.getSalary()))
                .filter(s -> s != null)
                .toList();

        if (salaries.isEmpty()) {
            return Map.of("message", "Нет данных о зарплатах");
        }

        double average = salaries.stream().mapToInt(Integer::intValue).average().orElse(0);
        return Map.of(
                "averageSalary", (int) average,
                "currency", "RUB",
                "vacanciesWithSalary", salaries.size(),
                "totalVacancies", all.size()
        );
    }

    // НОВЫЙ ЭНДПОИНТ: Топ-навыки
    @GetMapping("/analytics/top-skills")
    @ResponseBody
    public List<Map.Entry<String, Long>> getTopSkills() {
        List<ParsedVacancy> all = vacancyRepository.findAll();

        // Собираем все навыки из названий вакансий и считаем их количество
        return all.stream()
                .flatMap(v -> SkillParser.extractSkills(v.getTitle()).stream())
                .collect(Collectors.groupingBy(skill -> skill, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toList());
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }
}