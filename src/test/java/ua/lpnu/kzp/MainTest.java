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
    void testNegativeDataValidation(@TempDir Path tempDir) throws IOException {
        // НЕГАТИВНИЙ ТЕСТ 1: некоректний формат числа та від'ємні значення
        Path inputFile = tempDir.resolve("invalid_data.csv");
        Path outputFile = tempDir.resolve("out.txt");
        // Передаємо від'ємну кількість ночей (-3) та текст замість числа ("п'ять")
        Files.writeString(inputFile, "103;Сидоров;-3;1500.0;Люкс\n104;Коваленко;п'ять;1000.0;Економ");

        String[] args = {"--input", inputFile.toString(), "--output", outputFile.toString()};

        assertDoesNotThrow(() -> Main.main(args), "Програма не повинна падати при некоректних даних, вона має їх просто пропустити");
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
}
