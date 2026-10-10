package ua.lpnu.kzp;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BookingTest {

    // ==========================================
    // ТЕСТИ ФАБРИЧНОГО МЕТОДУ fromCsv
    // ==========================================

    @Test
    void fromCsvCreatesValidEntity() {
        // Перевіряємо, чи правильно розбирається ідеальний рядок
        Booking booking = Booking.fromCsv("101;Іванов;5;1200.50;Стандарт");

        assertEquals(101, booking.getRoom());
        assertEquals("Іванов", booking.getGuest());
        assertEquals(5, booking.getNights());
        assertEquals(1200.50, booking.getNightlyRate(), 0.0001);
        assertEquals("Стандарт", booking.getCategory());
    }

    @Test
    void fromCsvRejectsInvalidFieldCount() {
        // Передаємо 4 поля замість 5 і очікуємо помилку
        assertThrows(IllegalArgumentException.class,
            () -> Booking.fromCsv("101;Іванов;5;1200.50"));
    }

    @Test
    void fromCsvRejectsInvalidNumber() {
        // Передаємо текст "сто" замість номера кімнати
        assertThrows(IllegalArgumentException.class,
            () -> Booking.fromCsv("сто;Іванов;5;1200.50;Стандарт"));
    }

    // ==========================================
    // ТЕСТИ КОНСТРУКТОРА (ВАЛІДАЦІЯ)
    // ==========================================

    @Test
    void constructorRejectsNullOrEmptyStrings() {
        // Перевіряємо реакцію на null та порожні рядки (пробіли)
        assertThrows(NullPointerException.class,
            () -> new Booking(101, null, 5, 1200.50, "Стандарт"));

        assertThrows(IllegalArgumentException.class,
            () -> new Booking(101, "   ", 5, 1200.50, "Стандарт"));
    }

    @Test
    void constructorRejectsNegativeValues() {
        // Перевіряємо захист від від'ємних чисел (кімната та ночі)
        assertThrows(IllegalArgumentException.class,
            () -> new Booking(-101, "Іванов", 5, 1200.50, "Стандарт"));

        assertThrows(IllegalArgumentException.class,
            () -> new Booking(101, "Іванов", -3, 1200.50, "Стандарт"));
    }

    // ==========================================
    // ТЕСТ ДЛЯ RECORD NightsRate
    // ==========================================

    @Test
    void recordUsesValueEquality() {
        // Перевіряємо, що два record з однаковими даними вважаються рівними
        NightsRate first = new NightsRate(3, 10, 5000.0, 5);
        NightsRate second = new NightsRate(3, 10, 5000.0, 5);

        assertEquals(first, second, "Два records з однаковими даними мають бути рівними");
    }
}
