# JobPulse

Агрегатор IT-вакансий с персональным матчингом, аналитикой и уведомлениями в Telegram.

## 🏗 Архитектура

Проект состоит из двух микросервисов, связанных через Kafka:

1. **ingestion-service** (порт 8081) — сбор вакансий с hh.ru и Telegram, отправка в Kafka (топик `raw-vacancies`).
2. **processing-service** (порт 8082) — чтение из Kafka, сохранение в PostgreSQL, кэширование аналитики в Redis, REST API, дашборд и Telegram-бот.

**Инфраструктура:**
- **Kafka** — брокер сообщений.
- **PostgreSQL** — база данных (таблицы `vacancies`, `user_profiles`).
- **Redis** — кэш для эндпоинтов аналитики.
- **Cloudflare Worker** — прокси для Telegram API и парсинга каналов.

## 🛠 Стек технологий

- **Java 21**, Spring Boot 3.2.3
- **Spring Kafka**, Spring Data JPA, Spring Data Redis
- **PostgreSQL**, **Redis**
- **Jsoup** (парсинг HTML)
- **Thymeleaf + Chart.js** (дашборд)
- **SpringDoc OpenAPI** (Swagger)
- **telegrambots** (Java библиотека для бота)

## 🚀 Запуск

### 1. Инфраструктура (Docker)

    # Kafka
    docker run -d --name kafka -p 9092:9092 apache/kafka:latest

    # PostgreSQL
    docker run -d --name postgres -p 5433:5432 -e POSTGRES_DB=jobpulse_db -e POSTGRES_USER=jobpulse -e POSTGRES_PASSWORD=postgres postgres:16

    # Redis
    docker run -d --name redis -p 6379:6379 redis:7

### 2. Сервисы

    # Ingestion Service
    mvn -pl ingestion-service spring-boot:run

    # Processing Service
    mvn -pl processing-service spring-boot:run

## 📡 API Endpoints

| Метод | Эндпоинт | Описание |
|---|---|---|
| GET | `/api/vacancies` | Список всех вакансий |
| GET | `/api/analytics/top-companies` | Топ-5 компаний по числу вакансий (кэш Redis) |
| GET | `/api/analytics/top-skills` | Топ-10 навыков по упоминаниям (кэш Redis) |
| GET | `/api/analytics/average-salary` | Средняя зарплата по вакансиям с указанной ЗП |
| GET | `/api/match/{telegramId}` | Топ-5 вакансий по Match Score для профиля |
| POST | `/api/profile` | Создать/обновить профиль пользователя |
| GET | `/api/profile/{telegramId}` | Получить профиль пользователя |
| GET | `/api/dashboard` | Дашборд с графиками (Chart.js) |
| GET | `/swagger-ui.html` | Swagger UI |

### Пример создания профиля

**POST** `/api/profile`

    {
      "telegramId": "123456789",
      "skills": "Java, Selenium, Kafka, Docker, SQL",
      "experience": "Middle",
      "expectedSalary": 150000
    }

## 🤖 Telegram-бот

Бот (`@JobPulseVacancyBot`) выполняет три задачи:

1. **Ежедневный топ-5** — каждый день в 10:00 отправляет 5 вакансий в чат `telegram.bot.chat-id`.
2. **Персональные уведомления** — раз в час проверяет новые вакансии (за последний час) и отправляет пользователям те, где Match Score > 80% (до 5 штук).
3. **Резервный сбор** — раз в 5 минут читает посты из Telegram-каналов через `getUpdates` (если основной парсер не сработал).

Все запросы к Telegram API идут через **Cloudflare Worker**, чтобы обходить блокировки.

### Cloudflare Worker

Worker проксирует два типа запросов:
- `/bot<TOKEN>/...` → `api.telegram.org` (для бота).
- `/t.me/s/<channel>` → `t.me` (для парсинга каналов).

## 🕷 Парсинг вакансий

### hh.ru
Используется HTML-парсинг страницы поиска (`https://hh.ru/search/vacancy?...`). Собираются: название, компания, зарплата, ссылка.

> **Ограничение:** hh.ru блокирует загрузку страниц отдельных вакансий (DDoS-Guard), поэтому поле `description` для hh-вакансий остаётся пустым. Match Score для них считается только по `title` и `company`.

### Telegram
Парсинг публичных каналов (например, `qajobsoffers`) через Cloudflare Worker. Собираются: текст поста, ссылка, дата. `description` заполняется полным текстом поста.

## 🎯 Match Score

Алгоритм сравнивает навыки из профиля пользователя с текстом вакансии:
- Для **Telegram** — сравнивает с `title + company + description`.
- Для **hh.ru** — сравнивается только с `title` и `company` (из-за ограничения выше).

Возвращает процент совпадений (0–100). Уведомления отправляются при score > 80%.

## 📊 Дашборд

Доступен по адресу `http://localhost:8082/api/dashboard`. Показывает два графика (Chart.js):
- Топ-5 компаний по числу вакансий.
- Топ-10 навыков по упоминаниям.

## ⚠️ Известные ограничения

- hh.ru не отдаёт `description` для вакансий (блокировка DDoS-Guard).
- Telegram-парсинг зависит от Cloudflare Worker; если Worker недоступен — данные из Telegram не собираются.
- Match Score для hh-вакансий менее точный из-за отсутствия описания.