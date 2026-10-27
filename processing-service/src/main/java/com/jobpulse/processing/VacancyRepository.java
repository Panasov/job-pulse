package com.jobpulse.processing;

import org.springframework.data.jpa.repository.JpaRepository;

public interface VacancyRepository extends JpaRepository<ParsedVacancy, Long> {
}