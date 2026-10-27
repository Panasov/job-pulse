package com.jobpulse.ingestion;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class HhHtmlParserTest {

    @Test
    void testParseVacancies() {
        HhHtmlParser parser = new HhHtmlParser();

        var vacancies = parser.parseVacancies();

        assertNotNull(vacancies, "Список вакансий не должен быть null");
        System.out.println("Тест парсера прошел. Найдено вакансий: " + vacancies.size());
    }
}