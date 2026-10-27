# JobPulse 🚀

Микросервисное приложение для сбора, аналитики и визуализации IT-вакансий с hh.ru. 
Проект собирает вакансии по автотестированию на Java, сохраняет их в PostgreSQL, 
кэширует данные в Redis и присылает ежедневные уведомления в Telegram.

## 🛠 Технологии
* **Java 17**, **Spring Boot 3**
* **Apache Kafka** (Event-Driven архитектура)
* **PostgreSQL** (хранение данных)
* **Redis** (кэширование)
* **Docker** (контейнеризация)
* **Jsoup** (парсинг HTML hh.ru)
* **Thymeleaf + Chart.js** (визуализация)
* **Telegram Bot API** (уведомления)
* **Cloudflare Worker** (обход блокировок для Telegram)
* **Swagger** (документация API)

## 🏗 Архитектура
1. **Ingestion Service** — парсит вакансии с hh.ru (пагинация, обход блокировок) и отправляет их в Kafka.
2. **Processing Service** — читает из Kafka, сохраняет в PostgreSQL, кэширует в Redis и предоставляет REST API.
3. **Telegram Bot** — ежедневно присылает топ-5 вакансий через Telegram (работает через Cloudflare Worker).

## 🚀 Как запустить локально
1. `docker-compose up -d` (поднять Kafka, PostgreSQL, Redis)
2. Запустить `IngestionApplication` и `ProcessingApplication`.
3. Открыть дашборд: `http://localhost:8082/api/dashboard`
4. Открыть Swagger: `http://localhost:8082/swagger-ui.html`

## 📊 Аналитика
* **Средняя зарплата** по вакансиям (с учетом указанных вилок).
* **Топ-5 компаний** по количеству вакансий.
* **Топ-10 навыков** (Selenium, RestAssured, JUnit, Kafka, Docker и т.д.).
* **Telegram-бот** для ежедневных уведомлений (топ-5 вакансий в 10:00).

## 🧪 Тестирование
* Юнит-тесты для парсера.
* Интеграционные тесты для базы данных (H2, `@DataJpaTest`).
