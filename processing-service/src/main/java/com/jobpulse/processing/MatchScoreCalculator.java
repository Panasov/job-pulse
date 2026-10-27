package com.jobpulse.processing;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Component
public class MatchScoreCalculator {

    public int calculateScore(UserProfile profile, ParsedVacancy vacancy) {
        // 1. Получаем навыки из профиля (разбиваем по запятой, убираем пробелы)
        List<String> userSkills = Arrays.stream(profile.getSkills().split(","))
                .map(String::trim)
                .map(s -> s.toLowerCase(Locale.ROOT))
                .toList();

        // 2. Собираем весь текст вакансии (название + компания)
        String vacancyText = (vacancy.getTitle() + " " + vacancy.getCompany()).toLowerCase(Locale.ROOT);

        // 3. Считаем, сколько навыков из профиля встречаются в тексте вакансии
        long matchedSkills = userSkills.stream()
                .filter(vacancyText::contains)
                .count();

        // 4. Если в профиле нет навыков или в вакансии ничего не совпало — 0%
        if (userSkills.isEmpty() || matchedSkills == 0) {
            return 0;
        }

        // 5. Считаем процент совпадения (от 0 до 100)
        return (int) ((matchedSkills * 100) / userSkills.size());
    }
}