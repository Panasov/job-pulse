package com.jobpulse.processing;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "vacancies")
public class ParsedVacancy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String company;
    private String salary;

    @Column(unique = true)
    private String link;

    @Column(columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public ParsedVacancy() {}

    public ParsedVacancy(String title, String company, String salary, String link, String description) {
        this.title = title;
        this.company = company;
        this.salary = salary;
        this.link = link;
        this.description = description;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getCompany() { return company; }
    public String getSalary() { return salary; }
    public String getLink() { return link; }
    public String getDescription() { return description; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setId(Long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setCompany(String company) { this.company = company; }
    public void setSalary(String salary) { this.salary = salary; }
    public void setLink(String link) { this.link = link; }
    public void setDescription(String description) { this.description = description; }
}