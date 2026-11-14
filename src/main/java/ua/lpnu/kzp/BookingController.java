package ua.lpnu.kzp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Контролер, що керує процесом зчитування, валідації та потокової обробки бронювань.
 */
public class BookingController {

    /** Створює контролер обробки бронювань. */
    public BookingController() {
    }

    /**
     * Зчитує вхідний CSV-файл, валідує записи та формує звіт у вихідний файл.
     *
     * @param inputFile  шлях до вхідного файлу
     * @param outputFile шлях до вихідного файлу звіту
     */
    public void process(String inputFile, String outputFile) {
        List<String> lines;
        try {
            lines = Files.readAllLines(Paths.get(inputFile));
        } catch (IOException e) {
            System.out.println("Помилка читання файлу: " + e.getMessage());
            return;
        }

        List<Booking> validBookings = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            try {
                validBookings.add(parseLine(line));
            } catch (Exception e) {
                errors.add(String.format("Рядок %d: %s - %s", i + 1, line, e.getMessage()));
            }
        }

        if (validBookings.isEmpty()) {
            if (!errors.isEmpty()) {
                System.out.println("Помилки під час розбору даних:");
                for (String err : errors) {
                    System.out.println(err);
                }
            }
            System.out.println("Немає жодного коректного запису для обробки.");
            return;
        }

        generateReport(validBookings, outputFile);
    }

    private Booking parseLine(String line) {
        String[] parts = line.split(";");
        if (parts.length != 5) {
            throw new IllegalArgumentException("Неправильна кількість полів");
        }

        int room = Integer.parseInt(parts[0].trim());
        String name = parts[1].trim();
        int nights = Integer.parseInt(parts[2].trim());
        double rate = Double.parseDouble(parts[3].trim());
        String kindStr = parts[4].trim();

        if ("Стандарт".equalsIgnoreCase(kindStr)) {
            return new StandardBooking(room, name, nights, rate);
        } else if ("Люкс".equalsIgnoreCase(kindStr) || "Апартаменти".equalsIgnoreCase(kindStr)) {
            return new PremiumBooking(room, name, nights, rate);
        } else {
            throw new IllegalArgumentException("Невідома категорія: " + kindStr);
        }
    }

    private void generateReport(List<Booking> bookings, String outputFile) {
        Set<Booking> uniqueRooms = new HashSet<>(bookings);

        // Потокові обчислення замість ручного накопичення в циклі
        double totalRevenue = bookings.stream()
            .mapToDouble(Booking::getCost)
            .sum();

        IntSummaryStatistics nightsStats = BookingQueries.nightsStatistics(bookings);
        long validCount = nightsStats.getCount();
        long totalNights = nightsStats.getSum();
        int maxNights = validCount > 0 ? nightsStats.getMax() : 0;

        Map<RoomKind, Long> byKind = BookingQueries.countByKind(bookings);
        List<Integer> top5Rooms = BookingQueries.extractRoomNumbers(
            BookingQueries.topLongestBookings(bookings, 5)
        );

        StringBuilder report = new StringBuilder();
        for (Booking b : bookings) {
            double cost = b.getCost();
            String outLine = String.format(Locale.ROOT, "Кімната %d (%s): %s, %d ночей, Вартість: %.2f",
                b.getRoom(), b.getKind().label(), b.getGuestName(), b.getNights(), cost);
            System.out.println(outLine);
            report.append(outLine).append(System.lineSeparator());
        }

        String kindSummary = byKind.entrySet().stream()
            .map(entry -> entry.getKey().label() + "=" + entry.getValue())
            .collect(Collectors.joining(", ", "{", "}"));

        String summary = String.format(Locale.ROOT,
            "Коректних записів: %d%n"
                + "Загальна кількість ночей: %d%n"
                + "Максимальна тривалість (ночей): %d%n"
                + "Загальний виторг: %.2f%n"
                + "Унікальних кімнат: %d%n"
                + "Розподіл за категоріями: %s%n"
                + "Top-5 найдовших бронювань (кімнати): %s",
            validCount, totalNights, maxNights, totalRevenue, uniqueRooms.size(), kindSummary, top5Rooms);

        System.out.println(summary);
        report.append(summary).append(System.lineSeparator());

        try {
            Files.writeString(Paths.get(outputFile), report.toString());
        } catch (IOException e) {
            System.out.println("Помилка запису у файл: " + e.getMessage());
        }
    }
}
