package ua.lpnu.kzp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Головний клас програми для обробки даних готелю (Варіант 14).
 *
 * <p>
 * Програма читає записи про проживання гостей з текстового файлу,
 * перевіряє їх на коректність, обчислює статистичні показники
 * (загальна кількість ночей, виторг, найдовше проживання)
 * та формує форматований звіт.
 *
 * @author Олег Левицький
 * @version 2.0.0
 */
public final class Main {
    /* Забороняє створення екземплярів службового класу. */
    private Main() {
    }

    /**
     * Точка входу до програми.
     * Обробляє аргументи командного рядка для підтримки режимів --help та --version. Виконує
     * читання вхідних даних, їх розбір, валідацію полів та виведення результату в консоль і файл.
     *
     * @param args аргументи командного рядка
     */
    public static void main(String[] args) {
        String inputFile = "data/input.csv";
        String outputFile = "out/report.txt";

        for (int i = 0; i < args.length; i++) {
            if ("--help".equals(args[i])) {
                System.out.printf("Використання: java -jar lab01.jar [--input <файл>] [--output <файл>]%n");
                return;
            } else if ("--version".equals(args[i])) {
                try (java.io.InputStream input = Main.class.getClassLoader().getResourceAsStream("application.properties")) {
                    java.util.Properties prop = new java.util.Properties();
                    if (input != null) {
                        prop.load(input);
                        System.out.printf("v%s%n", prop.getProperty("version", "2.0.0"));
                    } else {
                        System.out.printf("v2.0.0%n");
                    }
                } catch (java.io.IOException e) {
                    System.out.printf("v2.0.0%n");
                }
                return;
            } else if ("--input".equals(args[i]) && i + 1 < args.length) {
                inputFile = args[i + 1];
                i++;
            } else if ("--output".equals(args[i]) && i + 1 < args.length) {
                outputFile = args[i + 1];
                i++;
            }
        }

        // Шляхи за замовчуванням
        Path inputPath = Path.of(inputFile);
        Path outputPath = Path.of(outputFile);

        List<String> lines;
        try {
            // Читання файлу у фіксованому кодуванні UTF-8
            lines = Files.readAllLines(inputPath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.printf("Помилка читання файлу: %s%n", inputPath.toAbsolutePath());
            return;
        }

        // Створюємо списки для об'єктів Booking та повідомлень про помилки
        List<Booking> bookings = new ArrayList<>();
        List<String> errors = new ArrayList<>();

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

            try {
                // Викликаємо фабричний метод для створення об'єкта та його валідації
                Booking booking = Booking.fromCsv(line);
                bookings.add(booking);

                // Отримуємо значення через геттери для проміжних обчислень
                totalNights += booking.getNights();
                totalRevenue += (booking.getNights() * booking.getNightlyRate());
                maxNights = Math.max(maxNights, booking.getNights());

            } catch (IllegalArgumentException e) {
                // Перехоплюємо помилки формату чи валідації і додаємо у список
                errors.add(String.format("Рядок %d: %s", i + 1, e.getMessage()));
            }
        }

        if (bookings.isEmpty()) {
            // Спочатку виводимо всі накопичені помилки розбору рядків
            if (!errors.isEmpty()) {
                System.out.println("Помилки під час розбору даних:");
                for (String err : errors) {
                    System.out.println(err);
                }
            }

            System.out.println("Немає жодного коректного запису для обробки.");
            return;
        }

        // Запаковуємо фінальну статистику у record
        NightsRate summary = new NightsRate(bookings.size(), totalNights, totalRevenue, maxNights);

        // Формування звіту (формат залишається незмінним)
        StringBuilder reportBuilder = new StringBuilder();
        reportBuilder.append(String.format(Locale.ROOT, "--- ЗВІТ (Варіант 14: Готель) ---%n"));
        reportBuilder.append(String.format(Locale.ROOT, "%-30s %10s%n", "Показник", "Значення"));
        reportBuilder.append(String.format(Locale.ROOT, "%-30s %10d%n", "Кількість коректних записів", summary.validCount()));
        reportBuilder.append(String.format(Locale.ROOT, "%-30s %10d%n", "Сумарна кількість ночей", summary.totalNights()));
        reportBuilder.append(String.format(Locale.ROOT, "%-30s %10.2f%n", "Загальний виторг", summary.totalRevenue()));
        reportBuilder.append(String.format(Locale.ROOT, "%-30s %10d%n", "Найдовше проживання", summary.maxNights()));

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
    }
}
