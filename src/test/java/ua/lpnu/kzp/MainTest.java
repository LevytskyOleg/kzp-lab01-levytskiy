package ua.lpnu.kzp;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

class MainTest {

    @Test
    void testNegativeDataValidation() throws Exception {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        // Створюємо тимчасовий файл із гарантовано неправильними даними
        Path tempFile = Files.createTempFile("invalid_data", ".csv");
        Files.writeString(tempFile,
            "bad_room;Іван;bad_nights;bad_rate;Люкс\n" +
                "-5;Олег;-3;-100.0;НеіснуючаКатегорія"
        );

        // Запускаємо програму
        Main.main(new String[]{"--input", tempFile.toString()});

        String output = outContent.toString();
        System.setOut(System.out);
        Files.delete(tempFile);

        // Перевіряємо вивід контролера
        assertTrue(output.contains("Помилки під час розбору даних:"));
        assertTrue(output.contains("Рядок 1"));
        assertTrue(output.contains("Рядок 2"));
        assertTrue(output.contains("Немає жодного коректного запису для обробки."));
    }
}
