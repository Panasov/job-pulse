package com.jobpulse.processing;

import jakarta.persistence.*;

@Entity
@Table(name = "vacancies")
public class ParsedVacancy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String company;
    private String salary;

    @Column(unique = true) // <-- ДОБАВИЛИ ЭТУ СТРОКУ
    private String link;

    // Пустой конструктор нужен для JPA
    public ParsedVacancy() {}

    public ParsedVacancy(String title, String company, String salary, String link) {
        this.title = title;
        this.company = company;
        this.salary = salary;
        this.link = link;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getCompany() { return company; }
    public String getSalary() { return salary; }
    public String getLink() { return link; }
}