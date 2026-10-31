package ua.lpnu.kzp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Контролер, що керує процесом обробки бронювань. */
public class BookingController {

    public void process(String inputFile, String outputFile) {
        List<String> lines;
        try {
            lines = Files.readAllLines(Paths.get(inputFile));
        } catch (IOException e) {
            System.out.println("Помилка читання файлу: " + e.getMessage());
            System.exit(1);
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
            System.exit(1); // Код завершення 1 вказує на помилку
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

        // Створення різних підтипів залежно від категорії
        if ("Стандарт".equalsIgnoreCase(kindStr)) {
            return new StandardBooking(room, name, nights, rate);
        } else if ("Люкс".equalsIgnoreCase(kindStr) || "Апартаменти".equalsIgnoreCase(kindStr)) {
            return new PremiumBooking(room, name, nights, rate);
        } else {
            throw new IllegalArgumentException("Невідома категорія: " + kindStr);
        }
    }

    private void generateReport(List<Booking> bookings, String outputFile) {
        double totalRevenue = 0.0;
        Set<Booking> uniqueRooms = new HashSet<>(bookings); // Працюють наші equals/hashCode!

        StringBuilder report = new StringBuilder();
        for (Booking b : bookings) {
            double cost = b.getCost(); // Поліморфний виклик!
            totalRevenue += cost;
            String outLine = String.format(Locale.ROOT, "Кімната %d (%s): %s, %d ночей, Вартість: %.2f",
                b.getRoom(), b.getKind().label(), b.getGuestName(), b.getNights(), cost);
            System.out.println(outLine);
            report.append(outLine).append(System.lineSeparator());
        }

        String summary = String.format(Locale.ROOT, "Загальний виторг: %.2f%nУнікальних кімнат: %d", totalRevenue, uniqueRooms.size());
        System.out.println(summary);
        report.append(summary).append(System.lineSeparator());

        try {
            Files.writeString(Paths.get(outputFile), report.toString());
        } catch (IOException e) {
            System.out.println("Помилка запису у файл: " + e.getMessage());
        }
    }
}
