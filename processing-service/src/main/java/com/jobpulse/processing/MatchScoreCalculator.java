package com.jobpulse.processing;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Component
public class MatchScoreCalculator {

    public int calculateScore(UserProfile profile, ParsedVacancy vacancy) {
        if (profile.getSkills() == null || profile.getSkills().isBlank()) {
            return 0;
        }

        List<String> userSkills = Arrays.stream(profile.getSkills().split(","))
                .map(String::trim)
                .map(s -> s.toLowerCase(Locale.ROOT))
                .toList();

        // Ищем навыки в названии, компании и описании вакансии
        String vacancyText = (vacancy.getTitle() + " " +
                vacancy.getCompany() + " " +
                (vacancy.getDescription() != null ? vacancy.getDescription() : ""))
                .toLowerCase(Locale.ROOT);

        long matchedSkills = userSkills.stream()
                .filter(vacancyText::contains)
                .count();

        if (userSkills.isEmpty() || matchedSkills == 0) {
            return 0;
        }

        return (int) ((matchedSkills * 100) / userSkills.size());
    }
}