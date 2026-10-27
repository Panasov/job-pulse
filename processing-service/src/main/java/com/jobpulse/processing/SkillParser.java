package com.jobpulse.processing;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class SkillParser {

    // Расширенный список навыков и ключевых слов
    private static final List<String> SKILLS = Arrays.asList(
            "Java", "Selenium", "Selenide", "RestAssured", "JUnit", "TestNG",
            "Appium", "Playwright", "Cucumber", "Allure", "Kafka", "Docker",
            "SQL", "Hibernate", "Spring", "Kubernetes", "Microservices",
            "Backend", "Fullstack", "QA", "AQA", "Тестировщик", "Разработчик",
            "Senior", "Middle", "Junior", "Lead"
    );

    public static List<String> extractSkills(String title) {
        if (title == null || title.isBlank()) {
            return List.of();
        }

        String lowerTitle = title.toLowerCase(Locale.ROOT);
        return SKILLS.stream()
                .filter(skill -> lowerTitle.contains(skill.toLowerCase(Locale.ROOT)))
                .toList();
    }
}