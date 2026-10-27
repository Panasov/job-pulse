package com.jobpulse.processing;

public class CompanyCountDto {
    private String company;
    private Long count;

    public CompanyCountDto() {} // Пустой конструктор для Jackson

    public CompanyCountDto(String company, Long count) {
        this.company = company;
        this.count = count;
    }

    public String getCompany() { return company; }
    public Long getCount() { return count; }
}