package com.jobpulse.ingestion;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class JobCollector {

    private final HhHtmlParser hhHtmlParser;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public JobCollector(HhHtmlParser hhHtmlParser, KafkaTemplate<String, Object> kafkaTemplate) {
        this.hhHtmlParser = hhHtmlParser;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedRate = 3600000, initialDelay = 5000)
    public void collectJobs() {
        System.out.println(">>> Начинаю сбор вакансий с hh.ru (HTML)...");

        var vacancies = hhHtmlParser.parseVacancies();

        if (!vacancies.isEmpty()) {
            System.out.println(">>> Получено вакансий: " + vacancies.size());
            vacancies.forEach(vacancy -> {
                kafkaTemplate.send("raw-vacancies", vacancy);
            });
            System.out.println(">>> Все вакансии отправлены в Kafka!");
        } else {
            System.out.println(">>> Вакансий не найдено (возможно, hh.ru изменил верстку).");
        }
    }
}