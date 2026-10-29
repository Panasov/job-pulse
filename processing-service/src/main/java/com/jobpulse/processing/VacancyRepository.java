package com.jobpulse.processing;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface VacancyRepository extends JpaRepository<ParsedVacancy, Long> {

    boolean existsByLink(String link);

    List<ParsedVacancy> findByCreatedAtAfter(LocalDateTime since);

    @Query(value = "SELECT AVG(CAST(salary_num AS INTEGER)) FROM (" +
            "  SELECT SUBSTRING(REGEXP_REPLACE(salary, '[^0-9]', '', 'g'), 1, 7) AS salary_num " +
            "  FROM vacancies " +
            "  WHERE salary IS NOT NULL AND salary != 'Не указана' " +
            "    AND LENGTH(REGEXP_REPLACE(salary, '[^0-9]', '', 'g')) BETWEEN 4 AND 7 " +
            ") sub " +
            "WHERE CAST(salary_num AS INTEGER) BETWEEN 10000 AND 1000000",
            nativeQuery = true)
    Double getAverageSalary();

    long countBySalaryIsNotNullAndSalaryNot(String salary);
}