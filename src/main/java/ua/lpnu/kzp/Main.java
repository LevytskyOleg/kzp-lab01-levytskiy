package ua.lpnu.kzp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Програма для обробки даних готелю (Варіант 14).
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        // Обробка аргументів командного рядка
        if (args.length > 0) {
            if ("--help".equals(args[0])) {
                System.out.printf("Використання: java -jar lab01.jar [--input <файл>] [--output <файл>]%n");
                return;
            } else if ("--version".equals(args[0])) {
                System.out.printf("v1.0.0%n");
                return;
            }
        }

        // Шляхи за замовчуванням
        Path inputPath = Path.of("data", "input.csv");
        Path outputPath = Path.of("out", "report.txt");

        List<String> lines;
        try {
            // Читання файлу у фіксованому кодуванні UTF-8
            lines = Files.readAllLines(inputPath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.printf("Помилка читання файлу: %s%n", inputPath.toAbsolutePath());
            return;
        }

        List<String> errors = new ArrayList<>();
        int validCount = 0;
        int totalNights = 0;
        double totalRevenue = 0.0;
        int maxNights = 0;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);

            // Пропуск порожніх рядків
            if (line.isBlank()) {
                errors.add(String.format("Рядок %d: порожній рядок", i + 1));
                continue;
            }

            // Розділяємо рядок, зберігаючи порожні поля в кінці
            String[] fields = line.split(";", -1);
            if (fields.length != 5) {
                errors.add(String.format("Рядок %d: очікується 5 полів, знайдено %d", i + 1, fields.length));
                continue;
            }

            if (fields[0].isBlank() || fields[4].isBlank()) {
                errors.add(String.format("Рядок %d: порожнє обов'язкове текстове поле", i + 1));
                continue;
            }

            try {
                int room = Integer.parseInt(fields[1].trim());
                int nights = Integer.parseInt(fields[2].trim());
                double nightlyRate = Double.parseDouble(fields[3].trim());

                // Перевірка на від'ємні значення, які не мають сенсу
                if (room <= 0 || nights < 0 || nightlyRate < 0) {
                    errors.add(String.format("Рядок %d: від'ємне або некоректне числове значення", i + 1));
                    continue;
                }

                // Включення в обчислення тільки валідних записів
                validCount++;
                totalNights += nights;
                totalRevenue += (nights * nightlyRate);
                maxNights = Math.max(maxNights, nights);

            } catch (NumberFormatException e) {
                errors.add(String.format("Рядок %d: числове поле має помилковий формат", i + 1));
            }
        }

        // Формування звіту
        StringBuilder reportBuilder = new StringBuilder();
        reportBuilder.append(String.format(Locale.ROOT, "--- ЗВІТ (Варіант 14: Готель) ---%n"));
        reportBuilder.append(String.format(Locale.ROOT, "%-30s %10s%n", "Показник", "Значення"));
        reportBuilder.append(String.format(Locale.ROOT, "%-30s %10d%n", "Кількість коректних записів", validCount));
        reportBuilder.append(String.format(Locale.ROOT, "%-30s %10d%n", "Сумарна кількість ночей", totalNights));
        reportBuilder.append(String.format(Locale.ROOT, "%-30s %10.2f%n", "Загальний виторг", totalRevenue));
        reportBuilder.append(String.format(Locale.ROOT, "%-30s %10d%n", "Найдовше проживання", maxNights));

        reportBuilder.append(String.format(Locale.ROOT, "%n--- ПОМИЛКИ: %d ---%n", errors.size()));
        for (String error : errors) {
            reportBuilder.append(error).append(System.lineSeparator());
        }

        String report = reportBuilder.toString();

        // Вивід у консоль
        System.out.print(report);

        // Запис у файл
        try {
            Path parent = outputPath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(outputPath, report, StandardCharsets.UTF_8);
            System.out.printf(Locale.ROOT, "%nЗвіт успішно збережено у файл: %s%n", outputPath);
        } catch (IOException e) {
            System.out.printf("Помилка запису у файл: %s%n", e.getMessage());
        }
    }}
