package com.jobpulse.ingestion;

public record ParsedVacancy(
        String title,
        String company,
        String salary,
        String link,
        String description
) {}