package com.jobpulse.ingestion;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class HhHtmlParser {

    private static final int PAGES_TO_PARSE = 5;

    public List<ParsedVacancy> parseVacancies() {
        List<ParsedVacancy> allVacancies = new ArrayList<>();
        String searchQuery = "автотестирование Java";

        try {
            String encodedQuery = URLEncoder.encode(searchQuery, StandardCharsets.UTF_8);

            for (int page = 0; page < PAGES_TO_PARSE; page++) {
                try {
                    String url = "https://hh.ru/search/vacancy?text=" + encodedQuery + "&area=113&page=" + page;
                    System.out.println(">>> Парсим страницу hh: " + (page + 1));

                    Document doc = Jsoup.connect(url)
                            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                            .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                            .header("Accept-Language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7")
                            .timeout(15000)
                            .get();

                    Elements vacancyElements = doc.select("[data-qa='vacancy-serp__vacancy']");
                    System.out.println(">>> Найдено вакансий на странице: " + vacancyElements.size());
                    if (vacancyElements.isEmpty()) break;

                    for (Element element : vacancyElements) {
                        Element titleElement = element.selectFirst("[data-qa='serp-item__title']");
                        String title = titleElement != null ? titleElement.text() : "Не указано";
                        String link = titleElement != null ? titleElement.attr("href") : "";
                        link = link.replace("smolensk.hh.ru", "hh.ru");

                        Element companyElement = element.selectFirst("[data-qa='vacancy-serp__vacancy-employer']");
                        String company = companyElement != null ? companyElement.text() : "Не указана";

                        String salary = "Не указана";
                        Elements salaryBlocks = element.select("span:has(data)");
                        for (Element block : salaryBlocks) {
                            String blockText = block.text();
                            if (blockText.contains("₽") || blockText.contains("$") || blockText.contains("€") || blockText.contains("руб")) {
                                salary = blockText;
                                break;
                            }
                        }

                        // description намеренно пустой — hh.ru блокирует загрузку страниц вакансий
                        allVacancies.add(new ParsedVacancy(title, company, salary, link, ""));
                    }

                    Thread.sleep(3000);
                } catch (Exception e) {
                    System.err.println(">>> ОШИБКА ПАРСИНГА СТРАНИЦЫ " + (page + 1) + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println(">>> ОШИБКА hh: " + e.getMessage());
        }

        System.out.println(">>> ИТОГО собрано с hh.ru: " + allVacancies.size());
        return allVacancies;
    }
}