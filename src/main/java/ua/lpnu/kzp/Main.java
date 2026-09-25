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
 * <p>
 * Програма читає записи про проживання гостей з текстового файлу,
 * перевіряє їх на коректність, обчислює статистичні показники
 * (загальна кількість ночей, виторг, найдовше проживання)
 * та формує форматований звіт.
 *
 * @author Олег Левицький
 * @version 1.0.0
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

    public static void main(String[] args) { // <-- Важлива відкриваюча дужка
        String inputFile = "data.csv";
        String outputFile = "out/report.txt";

        for (int i = 0; i < args.length; i++) {
            if ("--help".equals(args[i])) {
                System.out.printf("Використання: java -jar lab01.jar [--input <файл>] [--output <файл>]%n");
                return;
            } else if ("--version".equals(args[i])) {
                System.out.printf("v1.0.0%n");
                return;
            } else if ("--input".equals(args[i]) && i + 1 < args.length) {
                inputFile = args[i + 1];
                i++;
            } else if ("--output".equals(args[i]) && i + 1 < args.length) {
                outputFile = args[i + 1];
                i++;
            }
        } // <-- Цикл for закінчився. Тут має бути лише одна дужка }



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

            // Перевірка текстових полів (fields[1] - guest, fields[4] - category)
            if (fields[1].isBlank() || fields[4].isBlank()) {
                errors.add(String.format("Рядок %d: порожнє обов'язкове текстове поле", i + 1));
                continue;
            }

            try {
                int room = Integer.parseInt(fields[0].trim());            // room тепер на першому місці (індекс 0)
                int nights = Integer.parseInt(fields[2].trim());          // nights (індекс 2)
                double nightlyRate = Double.parseDouble(fields[3].trim());// nightlyRate (індекс 3)

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

        if (validCount == 0) {
            System.out.println("Немає жодного коректного запису для обробки.");
            return;
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
    }
}
