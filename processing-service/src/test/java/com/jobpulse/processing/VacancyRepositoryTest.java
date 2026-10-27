package com.jobpulse.processing;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest // Поднимает только JPA и H2 (без Kafka и реальной БД)
class VacancyRepositoryTest {

    @Autowired
    private VacancyRepository vacancyRepository;

    @Test
    void testSaveVacancy() {
        ParsedVacancy vacancy = new ParsedVacancy("Java Dev", "Test Company", "100k", "http://test.com");
        vacancyRepository.save(vacancy);

        assertEquals(1, vacancyRepository.findAll().size());
        assertEquals("Java Dev", vacancyRepository.findAll().get(0).getTitle());
    }
}