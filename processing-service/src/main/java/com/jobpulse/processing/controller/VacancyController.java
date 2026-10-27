package com.jobpulse.processing.controller;

import com.jobpulse.processing.CompanyCountDto;
import com.jobpulse.processing.ParsedVacancy;
import com.jobpulse.processing.SalaryParser;
import com.jobpulse.processing.SkillCountDto;
import com.jobpulse.processing.SkillParser;
import com.jobpulse.processing.VacancyRepository;
import org.springframework.cache.annotation.Cacheable;
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
    @Cacheable(value = "topCompanies")
    public List<CompanyCountDto> getTopCompanies() {
        List<ParsedVacancy> all = vacancyRepository.findAll();

        return all.stream()
                .filter(v -> v.getCompany() != null && !v.getCompany().isBlank())
                .collect(Collectors.groupingBy(ParsedVacancy::getCompany, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(entry -> new CompanyCountDto(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    @GetMapping("/analytics/average-salary")
    @ResponseBody
    public Map<String, Object> getAverageSalary() {
        Double average = vacancyRepository.getAverageSalary();

        if (average == null) {
            return Map.of("message", "Нет данных о зарплатах");
        }

        long total = vacancyRepository.count();
        long withSalary = vacancyRepository.countBySalaryIsNotNullAndSalaryNot("Не указана");

        return Map.of(
                "averageSalary", average.intValue(),
                "currency", "RUB",
                "vacanciesWithSalary", withSalary,
                "totalVacancies", total
        );
    }

    @GetMapping("/analytics/top-skills")
    @ResponseBody
    @Cacheable(value = "topSkills")
    public List<SkillCountDto> getTopSkills() {
        List<ParsedVacancy> all = vacancyRepository.findAll();

        return all.stream()
                .flatMap(v -> SkillParser.extractSkills(v.getTitle()).stream())
                .collect(Collectors.groupingBy(skill -> skill, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .map(entry -> new SkillCountDto(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }
}