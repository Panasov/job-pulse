package com.jobpulse.processing;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumer {

    private final VacancyRepository vacancyRepository;

    public KafkaConsumer(VacancyRepository vacancyRepository) {
        this.vacancyRepository = vacancyRepository;
    }

    @KafkaListener(topics = "raw-vacancies", groupId = "processing-group")
    public void consume(ParsedVacancy vacancy) {
        System.out.println(">>> Получена вакансия из Kafka: " + vacancy.getTitle());
        vacancyRepository.save(vacancy);
        System.out.println(">>> Сохранено в БД!");
    }
}