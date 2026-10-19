package ua.lpnu.kzp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    @Test
    void testPositiveDataValidation(@TempDir Path tempDir) throws IOException {
        // ПОЗИТИВНИЙ ТЕСТ: перевірка з правильними даними
        Path inputFile = tempDir.resolve("valid_data.csv");
        Path outputFile = tempDir.resolve("out.txt");
        Files.writeString(inputFile, "101;Іванов;5;1200.50;Стандарт\n102;Петров;2;1000.0;Економ");

        String[] args = {"--input", inputFile.toString(), "--output", outputFile.toString()};

        assertDoesNotThrow(() -> Main.main(args), "Програма має успішно обробити коректні дані без винятків");
        assertTrue(Files.exists(outputFile), "Файл звіту має бути успішно створений");
    }

    @Test
    void testNegativeDataValidation() throws Exception {
        // 1. Перехоплюємо вивід програми в консоль
        java.io.ByteArrayOutputStream outContent = new java.io.ByteArrayOutputStream();
        System.setOut(new java.io.PrintStream(outContent));

        // 2. Створюємо тимчасовий файл із гарантовано неправильними даними
        java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("invalid_data", ".csv");
        java.nio.file.Files.writeString(tempFile,
            "bad_room;Іван;bad_nights;bad_rate;Люкс\n" +
                "-5;Олег;-3;-100.0;Стандарт"
        );

        // 3. Запускаємо програму з нашим тимчасовим файлом
        Main.main(new String[]{"--input", tempFile.toString()});

        // 4. Зберігаємо те, що програма вивела, і повертаємо консоль до норми
        String output = outContent.toString();
        System.setOut(System.out);

        // 5. Видаляємо тимчасовий файл (прибираємо за собою)
        java.nio.file.Files.delete(tempFile);

        // 6. Перевіряємо, чи вивела програма правильні повідомлення
        org.junit.jupiter.api.Assertions.assertTrue(output.contains("Помилки під час розбору даних:"));
        org.junit.jupiter.api.Assertions.assertTrue(output.contains("Рядок")); // перевіряє, що вказано номер рядка з помилкою
        org.junit.jupiter.api.Assertions.assertTrue(output.contains("Немає жодного коректного запису для обробки."));
    }

    @Test
    void testNegativeMissingFile() {
        // НЕГАТИВНИЙ ТЕСТ 2: файлу не існує
        String[] args = {"--input", "non_existent_folder/fake_file.csv", "--output", "out.txt"};

        assertDoesNotThrow(() -> Main.main(args), "Програма має коректно обробити відсутність файлу через try-catch і не впасти");
    }

    @Test
    void testPositiveHelpArgument() {
        // ПОЗИТИВНИЙ ТЕСТ: виклик меню довідки
        String[] args = {"--help"};

        assertDoesNotThrow(() -> Main.main(args), "Команда --help має відпрацювати штатно");
    }
    @Test
    void testBoundaryValidValues() {
        // Крайові допустимі значення: мінімальний номер кімнати (1), мінімальні ночі (0 або 1 залежно від твоєї валідації), нульовий тариф
        Booking booking = new Booking(1, "Тест", 0, 0.0, "Стандарт");
        org.junit.jupiter.api.Assertions.assertEquals(1, booking.getRoom());
        org.junit.jupiter.api.Assertions.assertEquals(0, booking.getNights());
        org.junit.jupiter.api.Assertions.assertEquals(0.0, booking.getNightlyRate());
    }

    @Test
    void testBoundaryInvalidValues() {
        // Крайові недопустимі: нульова кімната (якщо мінімум 1), від'ємні ночі (-1), від'ємний тариф (-0.1)
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () ->
            new Booking(0, "Тест", 5, 100.0, "Стандарт"), "Кімната 0 має викликати виняток");

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () ->
            new Booking(101, "Тест", -1, 100.0, "Стандарт"), "-1 ніч має викликати виняток");

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () ->
            new Booking(101, "Тест", 5, -0.1, "Стандарт"), "Від'ємний тариф має викликати виняток");
    }
}
