package com.jobpulse.processing;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class SkillParser {

    // Расширенный список навыков и ключевых слов
    private static final List<String> SKILLS = Arrays.asList(
            "Java", "Spring", "Kafka", "Docker", "SQL", "Hibernate",
            "Kubernetes", "Microservices", "REST", "MongoDB", "RabbitMQ",
            "Backend", "Fullstack", "Frontend", "React", "Angular",
            "QA", "AQA", "Тестировщик", "Разработчик", "Developer",
            "Senior", "Middle", "Junior", "Lead", "Architect"
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