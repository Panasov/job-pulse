package com.jobpulse.processing;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import java.util.List;

@Service
public class TelegramBotService extends TelegramLongPollingBot implements ApplicationRunner {

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.chat-id}")
    private String chatId;

    private final VacancyRepository vacancyRepository;

    public TelegramBotService(VacancyRepository vacancyRepository) {
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

    // Направляем запросы через Cloudflare Worker (для отправки сообщений)
    @Override
    public String getBaseUrl() {
        return "https://telegram-proxy.moyst67.workers.dev";
    }

    // Отключаем удаление вебхука, чтобы не было ошибки
    @Override
    public void clearWebhook() {
        System.out.println(">>> Пропускаем удаление вебхука (clearWebhook)");
    }

    @Override
    public void onUpdateReceived(Update update) {
        // Бот не обрабатывает входящие сообщения
    }

    @Override
    public void run(ApplicationArguments args) {
        // Не регистрируем бота в TelegramBotsApi, чтобы не запускать Long Polling
        System.out.println(">>> Бот готов к отправке сообщений (Long Polling отключен)");
    }

    @Scheduled(cron = "0 0 10 * * *")
    public void sendDailyTop() {
        List<ParsedVacancy> top = vacancyRepository.findAll().stream()
                .limit(5)
                .toList();

        StringBuilder sb = new StringBuilder("🔥 Топ-5 вакансий дня:\n\n");
        for (ParsedVacancy v : top) {
            sb.append("• ").append(v.getTitle()).append(" — ").append(v.getCompany()).append("\n");
            sb.append(v.getLink()).append("\n\n");
        }

        String encodedText;
        try {
            encodedText = java.net.URLEncoder.encode(sb.toString(), "UTF-8");
        } catch (Exception e) {
            encodedText = sb.toString();
        }

        String url = "https://telegram-proxy.moyst67.workers.dev/bot" + botToken
                + "/sendMessage?chat_id=" + chatId + "&text=" + encodedText;

        // ВЫВОДИМ URL В КОНСОЛЬ, ЧТОБЫ ПРОВЕРИТЬ
        System.out.println(">>> URL для отправки: " + url);

        try {
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(url))
                    .GET()
                    .build();

            java.net.http.HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                System.out.println(">>> Сообщение отправлено в Telegram!");
            } else {
                System.err.println(">>> Ошибка отправки: " + response.statusCode() + " " + response.body());
            }
        } catch (Exception e) {
            System.err.println(">>> Ошибка отправки: " + e.getMessage());
        }
    }
}