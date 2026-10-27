package com.jobpulse.processing;

public class SkillCountDto {
    private String skill;
    private Long count;

    public SkillCountDto() {}

    public SkillCountDto(String skill, Long count) {
        this.skill = skill;
        this.count = count;
    }

    public String getSkill() { return skill; }
    public Long getCount() { return count; }
}