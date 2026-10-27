package com.jobpulse.ingestion;

import java.util.List;

public record HhResponse(
        List<HhVacancy> items
) {
    public record HhVacancy(
            String id,
            String name,
            Employer employer,
            Area area,
            Salary salary,
            Snippet snippet
    ) {}
    public record Employer(String name) {}
    public record Area(String name) {}
    public record Salary(Integer from, Integer to, String currency) {}
    public record Snippet(String requirement, String responsibility) {}
}