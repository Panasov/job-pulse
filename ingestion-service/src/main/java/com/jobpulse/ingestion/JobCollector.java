package com.jobpulse.ingestion;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class JobCollector {

    private final HhHtmlParser hhHtmlParser;
    private final TelegramParser telegramParser; // <-- ДОБАВИЛИ ПОЛЕ
    private final KafkaTemplate<String, Object> kafkaTemplate;

    // Spring Boot сам внедрит оба парсера и KafkaTemplate
    public JobCollector(HhHtmlParser hhHtmlParser,
                        TelegramParser telegramParser,
                        KafkaTemplate<String, Object> kafkaTemplate) {
        this.hhHtmlParser = hhHtmlParser;
        this.telegramParser = telegramParser;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedRate = 3600000, initialDelay = 5000)
    public void collectJobs() {
        System.out.println(">>> Начинаю сбор вакансий...");

        // 1. Собираем с hh.ru
        var hhVacancies = hhHtmlParser.parseVacancies();
        if (!hhVacancies.isEmpty()) {
            System.out.println(">>> Получено с hh.ru: " + hhVacancies.size());
            hhVacancies.forEach(v -> kafkaTemplate.send("raw-vacancies", v));
        }

        // 2. Собираем из Telegram
        var tgVacancies = telegramParser.parseVacancies();
        if (!tgVacancies.isEmpty()) {
            System.out.println(">>> Получено из Telegram: " + tgVacancies.size());
            tgVacancies.forEach(v -> kafkaTemplate.send("raw-vacancies", v));
        }

        System.out.println(">>> Все вакансии отправлены в Kafka!");
    }
}