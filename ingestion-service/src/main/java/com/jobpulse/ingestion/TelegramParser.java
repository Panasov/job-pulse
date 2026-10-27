package com.jobpulse.ingestion;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TelegramParser {

    private static final List<String> CHANNELS = List.of(
            "qajobsoffers"
            // Добавь сюда 1-2 проверенных канала, если найдёшь
    );

    public List<ParsedVacancy> parseVacancies() {
        List<ParsedVacancy> allVacancies = new ArrayList<>();

        for (String channel : CHANNELS) {
            try {
                // Используем твой Cloudflare Worker как прокси для t.me
                String url = "https://telegram-proxy.moyst67.workers.dev/t.me/s/" + channel;
                System.out.println(">>> Парсим Telegram-канал: " + channel);

                Document doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                        .timeout(15000)
                        .get();

                Elements posts = doc.select(".tgme_widget_message_wrap");

                for (Element post : posts) {
                    Element textElement = post.selectFirst(".tgme_widget_message_text");
                    if (textElement == null) continue;

                    String text = textElement.text();
                    if (text.isBlank()) continue;

                    String dataPost = post.selectFirst(".tgme_widget_message") != null
                            ? post.selectFirst(".tgme_widget_message").attr("data-post")
                            : "";
                    String messageId = dataPost.contains("/") ? dataPost.split("/")[1] : "";

                    String link = "https://t.me/" + channel + "/" + messageId;

                    String title = text.split("\n")[0];
                    if (title.length() > 100) title = title.substring(0, 100) + "...";

                    allVacancies.add(new ParsedVacancy(title, "Telegram", "Не указана", link));
                }

                System.out.println(">>> Найдено постов в канале " + channel + ": " + posts.size());
                Thread.sleep(2000);

            } catch (Exception e) {
                System.err.println(">>> ОШИБКА ПАРСИНГА TELEGRAM (" + channel + "): " + e.getMessage());
            }
        }

        System.out.println(">>> ИТОГО собрано из Telegram: " + allVacancies.size());
        return allVacancies;
    }
}