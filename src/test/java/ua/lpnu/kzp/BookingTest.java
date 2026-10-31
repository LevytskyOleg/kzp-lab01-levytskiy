package ua.lpnu.kzp;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;

class BookingTest {

    @Test
    void testStandardBookingBehavior() {
        Booking booking = new StandardBooking(101, "Олег", 5, 1000.0);

        assertEquals(101, booking.getRoom());
        assertEquals("Олег", booking.getGuestName());
        assertEquals(5, booking.getNights());
        assertEquals(RoomKind.STANDARD, booking.getKind());

        // 5 ночей * 1000.0 = 5000.0
        assertEquals(5000.0, booking.getCost(), 0.001);
    }

    @Test
    void testPremiumBookingBehavior() {
        Booking booking = new PremiumBooking(102, "Іван", 3, 2000.0);

        assertEquals(RoomKind.PREMIUM, booking.getKind());

        // 3 ночі * 2000.0 + 20% преміум-надбавки = 6000.0 * 1.20 = 7200.0
        assertEquals(7200.0, booking.getCost(), 0.001);
    }

    @Test
    void testPolymorphismCostDifference() {
        // Однаковий базовий тариф і ночі, але різні підтипи
        Booking standard = new StandardBooking(1, "Гість", 5, 1000.0);
        Booking premium = new PremiumBooking(2, "Гість", 5, 1000.0);

        // Поліморфний виклик дає різні результати
        assertNotEquals(standard.getCost(), premium.getCost());
    }


    @Test
    void testEqualityAndHashSet() {
        // Два бронювання ОДНАКОВОГО типу на одну й ту саму кімнату (205)
        Booking b1 = new StandardBooking(205, "Олег", 2, 1000.0);
        Booking b2 = new StandardBooking(205, "Марія", 5, 3000.0);

        // Бронювання ІНШОГО типу на ту ж кімнату (205)
        Booking b3 = new PremiumBooking(205, "Іван", 3, 2000.0);

        // За нашою логікою (getClass() та номер кімнати):
        assertEquals(b1, b2, "Об'єкти одного підтипу з однаковою кімнатою мають бути рівні");
        assertNotEquals(b1, b3, "Об'єкти різних підтипів НЕ рівні, навіть якщо кімната однакова");

        // Перевіряємо, чи HashSet не дублює рівні об'єкти
        Set<Booking> uniqueBookings = new HashSet<>();
        uniqueBookings.add(b1);
        uniqueBookings.add(b2); // Не додасться, бо вже є b1
        uniqueBookings.add(b3); // Додасться, бо це інший клас (PremiumBooking)

        assertEquals(2, uniqueBookings.size(), "У множині має бути лише два записи: один Standard і один Premium");
    }


    @Test
    void testBoundaryInvalidValues() {
        // Перевіряємо валідацію полів базового та похідних класів
        assertThrows(IllegalArgumentException.class, () ->
            new StandardBooking(0, "Олег", 5, 1000.0), "Кімната 0 має викликати виняток");

        assertThrows(IllegalArgumentException.class, () ->
            new PremiumBooking(101, "", 5, 1000.0), "Порожнє ім'я має викликати виняток");

        assertThrows(IllegalArgumentException.class, () ->
            new StandardBooking(101, "Олег", -1, 1000.0), "Від'ємні ночі мають викликати виняток");

        assertThrows(IllegalArgumentException.class, () ->
            new PremiumBooking(101, "Олег", 2, -50.0), "Від'ємний тариф має викликати виняток");
    }
}
