package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * Модульні тести для перевірки потокових запитів {@link BookingQueries}.
 */
class BookingQueriesTest {

    // Контрольний набір із 6 записів (для перевірки top-5, межі порогу, дубліката та tie-breaker)
    private final List<Booking> sampleBookings = List.of(
        new StandardBooking(105, "Олег", 3, 1000.0),     // на межі порогу minNights = 3
        new PremiumBooking(202, "Марія", 7, 2000.0),     // найдовше (7 ночей)
        new StandardBooking(101, "Іван", 5, 1100.0),     // 5 ночей, кімната 101 (має бути перед 304)
        new PremiumBooking(304, "Анна", 5, 2200.0),      // 5 ночей, кімната 304 (tie-breaker за кімнатою)
        new StandardBooking(102, "Петро", 1, 900.0),     // 1 ніч — відсікається у top-5
        new StandardBooking(105, "Дублікат", 4, 1000.0)  // дубльований номер 105 (4 ночі)
    );

    @Test
    void filtersByMinNightsIncludingBoundary() {
        List<Booking> filtered = BookingQueries.filterByMinNights(sampleBookings, 3);
        // Відсіюється лише запис на 1 ніч (кімната 102), запис на 3 ночі (105) входить
        assertEquals(5, filtered.size());
        assertEquals(List.of(105, 202, 101, 304, 105), BookingQueries.extractRoomNumbers(filtered));
    }

    @Test
    void groupsAndCountsByKind() {
        Map<RoomKind, Long> counts = BookingQueries.countByKind(sampleBookings);
        assertEquals(4L, counts.get(RoomKind.STANDARD));
        assertEquals(2L, counts.get(RoomKind.PREMIUM));
    }

    @Test
    void computesNightsStatistics() {
        IntSummaryStatistics stats = BookingQueries.nightsStatistics(sampleBookings);
        assertEquals(6, stats.getCount());
        assertEquals(25, stats.getSum()); // 3 + 7 + 5 + 5 + 1 + 4 = 25
        assertEquals(1, stats.getMin());
        assertEquals(7, stats.getMax());
        assertEquals(25.0 / 6.0, stats.getAverage(), 0.0001);
    }

    @Test
    void ordersTopBookingsAndBreaksTiesByRoomAscending() {
        // Очікуваний порядок: 202 (7 ночей), 101 (5 ночей), 304 (5 ночей), 105 (4 ночі), 105 (3 ночі).
        // Кімната 102 (1 ніч) усікається.
        List<Booking> top5 = BookingQueries.topLongestBookings(sampleBookings, 5);
        assertEquals(List.of(202, 101, 304, 105, 105), BookingQueries.extractRoomNumbers(top5));

        // Крайові значення limit
        assertEquals(List.of(), BookingQueries.topLongestBookings(sampleBookings, 0));
        assertEquals(6, BookingQueries.topLongestBookings(sampleBookings, 20).size());
    }

    @Test
    void findsFirstBookingByRoomOrReturnsEmpty() {
        Optional<Booking> found = BookingQueries.findByRoom(sampleBookings, 105);
        assertTrue(found.isPresent());
        // Перевіряємо, що findFirst повертає саме перший запис у початковому порядку ("Олег")
        assertEquals("Олег", found.get().getGuestName());

        Optional<Booking> missing = BookingQueries.findByRoom(sampleBookings, 999);
        assertTrue(missing.isEmpty());
    }

    @Test
    void streamMatchesLegacyLoopCalculations() {
        long legacyTotalNights = 0;
        int legacyMaxNights = 0;
        double legacyRevenue = 0.0;

        for (Booking b : sampleBookings) {
            legacyTotalNights += b.getNights();
            if (b.getNights() > legacyMaxNights) {
                legacyMaxNights = b.getNights();
            }
            legacyRevenue += b.getCost();
        }

        IntSummaryStatistics streamStats = BookingQueries.nightsStatistics(sampleBookings);
        double streamRevenue = sampleBookings.stream().mapToDouble(Booking::getCost).sum();

        assertEquals(legacyTotalNights, streamStats.getSum());
        assertEquals(legacyMaxNights, streamStats.getMax());
        assertEquals(legacyRevenue, streamRevenue, 0.0001);
    }

    @Test
    void handlesEmptyInputSafely() {
        List<Booking> empty = List.of();
        assertTrue(BookingQueries.filterByMinNights(empty, 3).isEmpty());
        assertTrue(BookingQueries.extractRoomNumbers(empty).isEmpty());
        assertTrue(BookingQueries.countByKind(empty).isEmpty());
        assertTrue(BookingQueries.topLongestBookings(empty, 5).isEmpty());
        assertTrue(BookingQueries.findByRoom(empty, 101).isEmpty());

        IntSummaryStatistics emptyStats = BookingQueries.nightsStatistics(empty);
        assertEquals(0, emptyStats.getCount());
        assertEquals(0, emptyStats.getSum());
    }

    @Test
    void rejectsNegativeArguments() {
        assertThrows(IllegalArgumentException.class, () -> BookingQueries.filterByMinNights(sampleBookings, -1));
        assertThrows(IllegalArgumentException.class, () -> BookingQueries.topLongestBookings(sampleBookings, -1));
    }

    @Test
    void queriesDoNotMutateInputCollection() {
        List<Booking> before = List.copyOf(sampleBookings);

        BookingQueries.filterByMinNights(sampleBookings, 3);
        BookingQueries.extractRoomNumbers(sampleBookings);
        BookingQueries.countByKind(sampleBookings);
        BookingQueries.nightsStatistics(sampleBookings);
        BookingQueries.topLongestBookings(sampleBookings, 5);
        BookingQueries.findByRoom(sampleBookings, 105);

        assertEquals(before, sampleBookings);
    }
}
