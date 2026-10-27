package com.jobpulse.processing;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Service
public class TelegramChannelListener extends TelegramLongPollingBot {

    @Value("${telegram.bot.token}")
    private String botToken;

    private final VacancyRepository vacancyRepository;

    public TelegramChannelListener(VacancyRepository vacancyRepository) {
        this.vacancyRepository = vacancyRepository;
    }

    @Override
    public String getBotUsername() {
        return "JobPulseVacancyBot";
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        // Проверяем, что сообщение пришло из канала (channel_post)
        if (update.hasChannelPost()) {
            var message = update.getChannelPost();
            String text = message.getText();

            if (text != null && !text.isBlank()) {
                System.out.println(">>> Получено сообщение из канала: " + text);

                // Извлекаем название вакансии (первая строка)
                String title = text.split("\n")[0];
                String link = "https://t.me/" + message.getChat().getUserName() + "/" + message.getMessageId();

                // Сохраняем в базу (или отправляем в Kafka)
                ParsedVacancy vacancy = new ParsedVacancy(title, "Telegram", "Не указана", link);
                vacancyRepository.save(vacancy);
                System.out.println(">>> Сохранено в БД из Telegram!");
            }
        }
    }
}