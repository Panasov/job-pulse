package com.jobpulse.ingestion;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class HhHtmlParser {

    private static final int PAGES_TO_PARSE = 5;

    public List<ParsedVacancy> parseVacancies() {
        List<ParsedVacancy> allVacancies = new ArrayList<>();

        for (int page = 0; page < PAGES_TO_PARSE; page++) {
            try {
                String url = "https://hh.ru/search/vacancy?text=Java&area=1&page=" + page;
                System.out.println(">>> Парсим страницу: " + (page + 1));

                Document doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                        .timeout(10000)
                        .get();

                Elements vacancyElements = doc.select("[data-qa='vacancy-serp__vacancy']");
                System.out.println(">>> Найдено вакансий на странице: " + vacancyElements.size());

                if (vacancyElements.isEmpty()) break;

                for (Element element : vacancyElements) {
                    Element titleElement = element.selectFirst("[data-qa='serp-item__title']");
                    String title = titleElement != null ? titleElement.text() : "Не указано";
                    String link = titleElement != null ? titleElement.attr("href") : "";

                    Element companyElement = element.selectFirst("[data-qa='vacancy-serp__vacancy-employer']");
                    String company = companyElement != null ? companyElement.text() : "Не указана";

                    // Ищем блок с зарплатой. Он содержит теги <data> и символ валюты.
                    String salary = "Не указана";
                    // Ищем все элементы, внутри которых есть тег <data> с валютой
                    Elements salaryBlocks = element.select("span:has(data)");
                    for (Element block : salaryBlocks) {
                        String blockText = block.text();
                        // Если в тексте блока есть символ валюты, это зарплата
                        if (blockText.contains("₽") || blockText.contains("$") || blockText.contains("€") || blockText.contains("руб")) {
                            salary = blockText;
                            break;
                        }
                    }

                    allVacancies.add(new ParsedVacancy(title, company, salary, link));
                }

                Thread.sleep(1000);

            } catch (Exception e) {
                System.err.println(">>> ОШИБКА ПАРСИНГА СТРАНИЦЫ " + (page + 1) + ": " + e.getMessage());
            }
        }

        System.out.println(">>> ИТОГО собрано вакансий: " + allVacancies.size());
        return allVacancies;
    }
}