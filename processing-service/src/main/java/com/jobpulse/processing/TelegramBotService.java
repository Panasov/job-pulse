package com.jobpulse.processing;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Service
public class TelegramBotService extends TelegramLongPollingBot implements ApplicationRunner {

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.chat-id}")
    private String chatId;

    private final VacancyRepository vacancyRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private long lastUpdateId = 0;

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

    // Направляем запросы через Cloudflare Worker
    @Override
    public String getBaseUrl() {
        return "https://telegram-proxy.moyst67.workers.dev";
    }

    // Отключаем удаление вебхука
    @Override
    public void clearWebhook() {
        System.out.println(">>> Пропускаем удаление вебхука (clearWebhook)");
    }

    @Override
    public void onUpdateReceived(Update update) {
        // Не используется, так как Long Polling отключен
    }

    @Override
    public void run(ApplicationArguments args) {
        // Ничего не делаем, чтобы не запускать Long Polling
        System.out.println(">>> Бот готов к отправке сообщений и чтению через getUpdates");
    }

    // 1. Отправка ежедневного топ-5
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
            encodedText = URLEncoder.encode(sb.toString(), "UTF-8");
        } catch (Exception e) {
            encodedText = sb.toString();
        }

        String url = "https://telegram-proxy.moyst67.workers.dev/bot" + botToken
                + "/sendMessage?chat_id=" + chatId + "&text=" + encodedText;

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                System.out.println(">>> Сообщение отправлено в Telegram!");
            } else {
                System.err.println(">>> Ошибка отправки: " + response.statusCode() + " " + response.body());
            }
        } catch (Exception e) {
            System.err.println(">>> Ошибка отправки: " + e.getMessage());
        }
    }

    // 2. Чтение сообщений из каналов через getUpdates (каждые 5 минут)
    @Scheduled(fixedRate = 300000)
    public void pollChannels() {
        String url = "https://telegram-proxy.moyst67.workers.dev/bot" + botToken
                + "/getUpdates?offset=" + (lastUpdateId + 1) + "&timeout=10";

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                TelegramResponse telegramResponse = objectMapper.readValue(response.body(), TelegramResponse.class);
                if (telegramResponse.isOk() && telegramResponse.getResult() != null) {
                    for (TelegramUpdate update : telegramResponse.getResult()) {
                        lastUpdateId = Math.max(lastUpdateId, update.getUpdateId());
                        if (update.getChannelPost() != null) {
                            String text = update.getChannelPost().getText();
                            if (text != null && !text.isBlank()) {
                                String title = text.split("\n")[0];
                                String link = "https://t.me/" + update.getChannelPost().getChat().getUsername()
                                        + "/" + update.getChannelPost().getMessageId();
                                ParsedVacancy vacancy = new ParsedVacancy(title, "Telegram", "Не указана", link);
                                if (!vacancyRepository.existsByLink(link)) {
                                    vacancyRepository.save(vacancy);
                                    System.out.println(">>> Сохранено из Telegram: " + title);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Тихо игнорируем ошибки (например, если нет новых сообщений)
        }
    }
}