package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Модульні тести для параметризованого сховища {@link Repository},
 * рефлексивного експортера {@link CsvExporter}, парсера {@link CsvParser}
 * та моделі бронювання {@link BookingRecord} (Варіант 14).
 */
class StorageAndCsvTest {

    @TempDir
    Path tempDir;

    /* Допоміжний плоский клас іншого домену для доведення універсальності CsvExporter. */
    private static final class SimpleNote {
        @CsvColumn("Примітка")
        private final String note;

        @CsvColumn("Код")
        private final int code;

        private SimpleNote(int code, String note) {
            this.code = code;
            this.note = note;
        }
    }

    /* Клас без анотованих полів для перевірки реакції експортера. */
    private static final class WithoutAnnotations {
        @SuppressWarnings("unused")
        private final String data = "test";
    }

    @Test
    void repositoryStoresFindsAndReturnsUnmodifiableSnapshot() {
        Repository<BookingRecord> repo = new Repository<>();
        BookingRecord r1 = new BookingRecord(101, "Олег Левицький", LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 4), 1200.0);
        BookingRecord r2 = new BookingRecord(205, "Марія Коваль", LocalDate.of(2026, 5, 10), LocalDate.of(2026, 5, 15), 2500.0);

        repo.add(r1);
        repo.add(r2);

        assertEquals(2, repo.all().size());
        assertEquals(1, repo.find(b -> b.getRoom() == 205).size());
        assertEquals(r2, repo.find(b -> b.getRate() > 2000.0).get(0));

        // Перевірка, що all() повертає незмінний знімок
        List<BookingRecord> snapshot = repo.all();
        assertThrows(UnsupportedOperationException.class, () -> snapshot.add(r1));
        assertThrows(NullPointerException.class, () -> repo.add(null));
    }

    @Test
    void roundTripPreservesFiveRecordsWithCommasQuotesAndMultiline() throws Exception {
        // 5 записів Варіанту 14 (включно з комами, подвійними лапками та перенесенням рядка в імені гостя)
        List<BookingRecord> original = List.of(
            new BookingRecord(101, "Іван Петренко", LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 3), 1100.0),
            new BookingRecord(102, "Коваленко, Олена", LocalDate.of(2026, 6, 2), LocalDate.of(2026, 6, 5), 1350.5),
            new BookingRecord(201, "ТОВ \"Карпати-Тур\"", LocalDate.of(2026, 6, 4), LocalDate.of(2026, 6, 10), 2400.0),
            new BookingRecord(202, "Перший рядок\nДругий рядок", LocalDate.of(2026, 6, 5), LocalDate.of(2026, 6, 7), 1900.0),
            new BookingRecord(305, "Анна Сидоренко", LocalDate.of(2026, 6, 8), LocalDate.of(2026, 6, 12), 3100.0)
        );

        Path csvPath = tempDir.resolve("hotel_bookings.csv");
        CsvExporter.write(csvPath, BookingRecord.class, original);

        String csvContent = Files.readString(csvPath, StandardCharsets.UTF_8);
        List<List<String>> rows = CsvParser.parseDocument(csvContent);

        // 1 рядок заголовка + 5 записів = 6 логічних записів CSV
        assertEquals(6, rows.size());
        // Перевірка детермінованого порядку заголовків (за алфавітом полів: checkIn, checkOut, guest, rate, room)
        assertEquals("заїзд,виїзд,гість,тариф,номер", String.join(",", rows.get(0)));
        // Перевірка екранування коми та лапок у тексті файлу
        assertTrue(csvContent.contains("\"Коваленко, Олена\""));
        assertTrue(csvContent.contains("\"ТОВ \"\"Карпати-Тур\"\"\""));

        List<BookingRecord> restored = rows.stream()
            .skip(1)
            .map(fields -> {
                try {
                    return BookingRecord.fromFields(fields);
                } catch (DataStorageException e) {
                    throw new IllegalStateException(e);
                }
            })
            .toList();

        assertEquals(original, restored);
    }

    @Test
    void exporterWorksWithAnyAnnotatedClass() throws Exception {
        Path path = tempDir.resolve("notes.csv");
        List<SimpleNote> notes = List.of(new SimpleNote(7, "Тест, з комою"));

        CsvExporter.write(path, SimpleNote.class, notes);
        String csv = Files.readString(path, StandardCharsets.UTF_8);
        List<List<String>> rows = CsvParser.parseDocument(csv);

        assertEquals(2, rows.size());
        // Сортування за іменами полів: code ("Код"), note ("Примітка")
        assertEquals(List.of("Код", "Примітка"), rows.get(0));
        assertEquals(List.of("7", "Тест, з комою"), rows.get(1));
    }

    @Test
    void handlesEmptyListAndEmptyCsvDocument() throws Exception {
        Path emptyRecordsPath = tempDir.resolve("empty_records.csv");
        CsvExporter.write(emptyRecordsPath, BookingRecord.class, List.of());

        String csv = Files.readString(emptyRecordsPath, StandardCharsets.UTF_8);
        List<List<String>> rows = CsvParser.parseDocument(csv);

        // Заголовок присутній, даних немає (0 відновлених записів)
        assertEquals(1, rows.size());
        assertEquals(List.of("заїзд", "виїзд", "гість", "тариф", "номер"), rows.get(0));
        assertTrue(CsvParser.parseDocument("").isEmpty());
    }

    @Test
    void rejectsInvalidCsvAndPreservesCause() {
        // Неправильна кількість полів
        assertThrows(DataStorageException.class,
            () -> BookingRecord.fromCsv("2026-06-01,2026-06-03,Олег"));

        // Хибне число (номер кімнати) -> перевіряємо наявність NumberFormatException у cause
        DataStorageException badNumber = assertThrows(DataStorageException.class,
            () -> BookingRecord.fromCsv("2026-06-01,2026-06-03,Олег,1200.0,не_число"));
        assertNotNull(badNumber.getCause());
        assertTrue(badNumber.getCause() instanceof NumberFormatException);

        // Хибна дата -> перевіряємо наявність DateTimeParseException у cause
        DataStorageException badDate = assertThrows(DataStorageException.class,
            () -> BookingRecord.fromCsv("не-дата,2026-06-03,Олег,1200.0,101"));
        assertNotNull(badDate.getCause());
        assertTrue(badDate.getCause() instanceof java.time.format.DateTimeParseException);

        // Незакриті лапки у парсері
        assertThrows(DataStorageException.class,
            () -> CsvParser.parseDocument("заїзд,гість\n2026-06-01,\"Незакриті лапки"));

        // Клас без @CsvColumn
        Path path = tempDir.resolve("no_annotations.csv");
        assertThrows(DataStorageException.class,
            () -> CsvExporter.write(path, WithoutAnnotations.class, List.of(new WithoutAnnotations())));
    }

    @Test
    void wrapsFileIoExceptionWithCause() throws Exception {
        Path directoryInsteadOfFile = tempDir.resolve("folder_target");
        Files.createDirectory(directoryInsteadOfFile);

        DataStorageException exception = assertThrows(DataStorageException.class,
            () -> CsvExporter.write(directoryInsteadOfFile, BookingRecord.class, List.of()));

        assertNotNull(exception.getCause());
        assertTrue(exception.getCause() instanceof IOException);
    }
}
