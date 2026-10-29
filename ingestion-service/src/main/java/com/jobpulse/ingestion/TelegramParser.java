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

    private static final List<String> CHANNELS = List.of("qajobsoffers");
    private static final String WORKER_BASE = "https://telegram-proxy.moyst67.workers.dev/t.me/s/";

    public List<ParsedVacancy> parseVacancies() {
        List<ParsedVacancy> allVacancies = new ArrayList<>();

        for (String channel : CHANNELS) {
            try {
                String url = WORKER_BASE + channel;
                System.out.println(">>> Парсим Telegram-канал: " + channel);

                Document doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                        .header("Accept-Language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7")
                        .timeout(15000)
                        .get();

                Elements messages = doc.select(".tgme_widget_message_text");
                System.out.println(">>> Найдено сообщений: " + messages.size());

                for (Element msg : messages) {
                    String text = msg.text();
                    if (text.isBlank() || text.length() < 20) continue;

                    Element parent = msg.parent();
                    String dataPost = "";
                    while (parent != null && dataPost.isEmpty()) {
                        dataPost = parent.attr("data-post");
                        parent = parent.parent();
                    }

                    String link = dataPost.isEmpty() ? "https://t.me/" + channel : "https://t.me/" + dataPost;
                    String title = text.split("\n")[0];
                    if (title.length() > 100) title = title.substring(0, 100) + "...";
                    String description = text.length() > 2000 ? text.substring(0, 2000) : text;

                    allVacancies.add(new ParsedVacancy(title, "Telegram", "Не указана", link, description));
                }

                Thread.sleep(1000);

            } catch (Exception e) {
                System.err.println(">>> ОШИБКА ПАРСИНГА TELEGRAM (" + channel + "): " + e.getMessage());
            }
        }

        System.out.println(">>> ИТОГО собрано из Telegram: " + allVacancies.size());
        return allVacancies;
    }
}