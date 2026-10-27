package com.jobpulse.processing;

public class SalaryParser {

    public static Integer parseSalary(String salaryStr) {
        if (salaryStr == null || salaryStr.equalsIgnoreCase("Не указана") || salaryStr.isBlank()) {
            return null;
        }

        // Убираем ВСЕ пробелы (включая неразрывные) и все нецифровые символы, кроме дефиса
        // "от 60 000 ₽ за месяц" -> "60000"
        String clean = salaryStr.replaceAll("[^0-9]", ""); // оставляем только цифры

        // Если цифр нет - возвращаем null
        if (clean.isEmpty()) return null;

        // Если получилось очень длинное число (например, склеились 60 и 000 в 60000 - это ок)
        // Но если склеились год и зарплата (например, 2026200000) - это плохо.
        // Поэтому ограничим длину: зарплата в рублях редко больше 10 000 000.
        if (clean.length() > 8) {
            // Если число слишком длинное, попробуем отрезать первые 4 цифры (год)
            // Но это сложно. Проще вернуть null, чтобы не портить статистику.
            return null;
        }

        try {
            return Integer.parseInt(clean);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}