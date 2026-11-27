package ua.lpnu.kzp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Контролер, що керує процесом зчитування, валідації, потокової обробки та CSV-експорту бронювань.
 */
public class BookingController {

    private static final LocalDate DEFAULT_BASE_DATE = LocalDate.of(2026, 6, 1);

    /** Створює контролер обробки бронювань. */
    public BookingController() {
    }

    /**
     * Зчитує вхідний файл, валідує записи, формує текстовий звіт та експортує записи у CSV.
     *
     * @param inputFile  шлях до вхідного файлу
     * @param outputFile шлях до вихідного файлу звіту
     */
    public void process(String inputFile, String outputFile) {
        process(inputFile, outputFile, null);
    }

    /**
     * Зчитує вхідний файл, валідує записи, формує текстовий звіт та експортує записи у вказаний CSV-файл.
     *
     * @param inputFile  шлях до вхідного файлу
     * @param outputFile шлях до вихідного файлу звіту
     * @param exportFile шлях до вихідного CSV-файлу (якщо {@code null}, формується автоматично поруч зі звітом)
     */
    public void process(String inputFile, String outputFile, String exportFile) {
        List<String> lines;
        try {
            lines = Files.readAllLines(Paths.get(inputFile), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println("Помилка читання файлу: " + e.getMessage());
            return;
        }

        Repository<Booking> bookingRepository = new Repository<>();
        Repository<BookingRecord> recordRepository = new Repository<>();
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            try {
                Booking booking = parseLine(line);
                bookingRepository.add(booking);
                recordRepository.add(toRecord(booking, i));
            } catch (Exception e) {
                errors.add(String.format("Рядок %d: %s - %s", i + 1, line, e.getMessage()));
            }
        }

        List<Booking> validBookings = bookingRepository.all();
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

        Path csvOutputPath = resolveExportPath(outputFile, exportFile);
        generateReport(validBookings, recordRepository, outputFile, csvOutputPath);
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

    private BookingRecord toRecord(Booking booking, int offsetDays) {
        LocalDate checkIn = DEFAULT_BASE_DATE.plusDays(offsetDays);
        LocalDate checkOut = checkIn.plusDays(booking.getNights());
        double dailyRate = booking.getNights() > 0
            ? booking.getCost() / booking.getNights()
            : booking.getCost();
        return new BookingRecord(booking.getRoom(), booking.getGuestName(), checkIn, checkOut, dailyRate);
    }

    private Path resolveExportPath(String outputFile, String exportFile) {
        if (exportFile != null && !exportFile.isBlank()) {
            return Path.of(exportFile);
        }
        Path reportPath = Path.of(outputFile);
        Path parent = reportPath.getParent();
        Path fileNamePath = reportPath.getFileName();
        String baseName = fileNamePath != null ? fileNamePath.toString() : "report.txt";
        String fileName = baseName + ".records.csv";
        return parent != null ? parent.resolve(fileName) : Path.of("target", fileName);
    }

    private void generateReport(
        List<Booking> bookings,
        Repository<BookingRecord> recordRepository,
        String outputFile,
        Path csvOutputPath) {
        Set<Booking> uniqueRooms = new HashSet<>(bookings);

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

        String exportStatus;
        try {
            List<BookingRecord> records = recordRepository.all();
            CsvExporter.write(csvOutputPath, BookingRecord.class, records);

            String csvContent = Files.readString(csvOutputPath, StandardCharsets.UTF_8);
            List<List<String>> parsedRows = CsvParser.parseDocument(csvContent);
            List<BookingRecord> restored = new ArrayList<>();
            for (int i = 1; i < parsedRows.size(); i++) {
                restored.add(BookingRecord.fromFields(parsedRows.get(i)));
            }

            boolean roundTripOk = records.equals(restored);
            exportStatus = String.format(Locale.ROOT,
                "CSV-експорт: %s (записів: %d, round-trip: %s)",
                csvOutputPath, restored.size(), roundTripOk);
        } catch (DataStorageException | IOException e) {
            exportStatus = "Помилка CSV-експорту: " + e.getMessage();
        }

        String summary = String.format(Locale.ROOT,
            "Коректних записів: %d%n"
                + "Загальна кількість ночей: %d%n"
                + "Максимальна тривалість (ночей): %d%n"
                + "Загальний виторг: %.2f%n"
                + "Унікальних кімнат: %d%n"
                + "Розподіл за категоріями: %s%n"
                + "Top-5 найдовших бронювань (кімнати): %s%n"
                + "%s",
            validCount, totalNights, maxNights, totalRevenue, uniqueRooms.size(), kindSummary, top5Rooms, exportStatus);

        System.out.println(summary);
        report.append(summary).append(System.lineSeparator());

        try {
            Files.writeString(Paths.get(outputFile), report.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println("Помилка запису у файл: " + e.getMessage());
        }
    }
}
