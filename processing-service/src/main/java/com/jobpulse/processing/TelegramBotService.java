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
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TelegramBotService extends TelegramLongPollingBot implements ApplicationRunner {

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.chat-id}")
    private String chatId;

    private final VacancyRepository vacancyRepository;
    private final UserProfileRepository userProfileRepository;
    private final MatchScoreCalculator matchScoreCalculator;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private long lastUpdateId = 0;
    private LocalDateTime lastMatchRun = LocalDateTime.now().minusHours(1);

    public TelegramBotService(VacancyRepository vacancyRepository,
                              UserProfileRepository userProfileRepository,
                              MatchScoreCalculator matchScoreCalculator) {
        this.vacancyRepository = vacancyRepository;
        this.userProfileRepository = userProfileRepository;
        this.matchScoreCalculator = matchScoreCalculator;
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

    // 1. Отправка ежедневного топ-5 (в 10 утра)
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

        sendMessage(chatId, sb.toString());
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
                                ParsedVacancy vacancy = new ParsedVacancy(title, "Telegram", "Не указана", link, text);
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

    // 3. Персональные уведомления: Match Score > 80% (раз в час)
    @Scheduled(fixedRate = 3600000, initialDelay = 60000)
    public void sendPersonalMatches() {
        LocalDateTime since = lastMatchRun;
        lastMatchRun = LocalDateTime.now();

        List<ParsedVacancy> fresh = vacancyRepository.findByCreatedAtAfter(since);
        if (fresh.isEmpty()) {
            System.out.println(">>> Новых вакансий для матчинга нет");
            return;
        }

        List<UserProfile> profiles = userProfileRepository.findAll();
        for (UserProfile profile : profiles) {
            if (profile.getSkills() == null || profile.getSkills().isBlank()) continue;
            if (profile.getTelegramId() == null || profile.getTelegramId().isBlank()) continue;

            List<ParsedVacancy> matched = fresh.stream()
                    .filter(v -> matchScoreCalculator.calculateScore(profile, v) > 80)
                    .limit(5)
                    .toList();

            if (matched.isEmpty()) continue;

            StringBuilder sb = new StringBuilder("🎯 Персональные вакансии для вас (Match > 80%):\n\n");
            for (ParsedVacancy v : matched) {
                int score = matchScoreCalculator.calculateScore(profile, v);
                sb.append("• ").append(v.getTitle()).append(" — ").append(v.getCompany())
                        .append(" (").append(score).append("%)\n")
                        .append(v.getLink()).append("\n\n");
            }

            sendMessage(profile.getTelegramId(), sb.toString());
        }
    }

    // Общий метод отправки сообщения через Cloudflare Worker
    private void sendMessage(String targetChatId, String text) {
        String encodedText;
        try {
            encodedText = URLEncoder.encode(text, "UTF-8");
        } catch (Exception e) {
            encodedText = text;
        }

        String url = "https://telegram-proxy.moyst67.workers.dev/bot" + botToken
                + "/sendMessage?chat_id=" + targetChatId + "&text=" + encodedText;

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                System.out.println(">>> Сообщение отправлено в Telegram (chat_id=" + targetChatId + ")");
            } else {
                System.err.println(">>> Ошибка отправки: " + response.statusCode() + " " + response.body());
            }
        } catch (Exception e) {
            System.err.println(">>> Ошибка отправки: " + e.getMessage());
        }
    }
}