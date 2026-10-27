package com.jobpulse.processing.controller;

import com.jobpulse.processing.CompanyCountDto;
import com.jobpulse.processing.MatchScoreCalculator;
import com.jobpulse.processing.ParsedVacancy;
import com.jobpulse.processing.SalaryParser;
import com.jobpulse.processing.SkillCountDto;
import com.jobpulse.processing.SkillParser;
import com.jobpulse.processing.UserProfile;
import com.jobpulse.processing.UserProfileRepository;
import com.jobpulse.processing.VacancyRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/api")
public class VacancyController {

    private final VacancyRepository vacancyRepository;
    private final UserProfileRepository userProfileRepository;
    private final MatchScoreCalculator matchScoreCalculator;

    public VacancyController(VacancyRepository vacancyRepository,
                             UserProfileRepository userProfileRepository,
                             MatchScoreCalculator matchScoreCalculator) {
        this.vacancyRepository = vacancyRepository;
        this.userProfileRepository = userProfileRepository;
        this.matchScoreCalculator = matchScoreCalculator;
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

    // НОВЫЙ ЭНДПОИНТ: Match Score
    @GetMapping("/match/{telegramId}")
    @ResponseBody
    public List<Map<String, Object>> getMatchedVacancies(@PathVariable String telegramId) {
        UserProfile profile = userProfileRepository.findByTelegramId(telegramId);
        if (profile == null) {
            return List.of(Map.of("error", "Профиль не найден"));
        }

        List<ParsedVacancy> all = vacancyRepository.findAll();

        return all.stream()
                .map(v -> Map.of(
                        "vacancy", (Object) v,
                        "score", (Object) matchScoreCalculator.calculateScore(profile, v)
                ))
                .filter(m -> (int) m.get("score") > 0)
                .sorted((a, b) -> Integer.compare((int) b.get("score"), (int) a.get("score")))
                .limit(5)
                .collect(Collectors.toList());
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }
}