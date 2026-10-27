package com.jobpulse.processing.controller;

import com.jobpulse.processing.UserProfile;
import com.jobpulse.processing.UserProfileRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class UserProfileController {

    private final UserProfileRepository userProfileRepository;

    public UserProfileController(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    @PostMapping
    public UserProfile saveProfile(@RequestBody UserProfile profile) {
        // Если профиль с таким telegramId уже есть — обновляем
        UserProfile existing = userProfileRepository.findByTelegramId(profile.getTelegramId());
        if (existing != null) {
            profile.setId(existing.getId());
        }
        return userProfileRepository.save(profile);
    }

    @GetMapping("/{telegramId}")
    public UserProfile getProfile(@PathVariable String telegramId) {
        return userProfileRepository.findByTelegramId(telegramId);
    }
}