package com.jobpulse.processing;

import jakarta.persistence.*;

@Entity
@Table(name = "user_profiles")
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String telegramId;
    private String skills;
    private String experience;
    private Integer expectedSalary;

    public UserProfile() {}

    public UserProfile(String telegramId, String skills, String experience, Integer expectedSalary) {
        this.telegramId = telegramId;
        this.skills = skills;
        this.experience = experience;
        this.expectedSalary = expectedSalary;
    }

    public Long getId() { return id; }
    public String getTelegramId() { return telegramId; }
    public String getSkills() { return skills; }
    public String getExperience() { return experience; }
    public Integer getExpectedSalary() { return expectedSalary; }

    // Сеттеры (нужны для JSON-десериализации и обновления профиля)
    public void setId(Long id) { this.id = id; }
    public void setTelegramId(String telegramId) { this.telegramId = telegramId; }
    public void setSkills(String skills) { this.skills = skills; }
    public void setExperience(String experience) { this.experience = experience; }
    public void setExpectedSalary(Integer expectedSalary) { this.expectedSalary = expectedSalary; }
}