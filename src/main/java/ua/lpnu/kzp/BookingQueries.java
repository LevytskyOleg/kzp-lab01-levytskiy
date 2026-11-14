package ua.lpnu.kzp;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Набір чистих потокових запитів (Stream API) для аналізу бронювань готелю (Варіант 14).
 */
public final class BookingQueries {

    /* Забороняє створення екземплярів утилітного класу. */
    private BookingQueries() {
    }

    /**
     * Відбирає бронювання, тривалість яких не менша за вказаний поріг ночей.
     *
     * @param bookings  список бронювань
     * @param minNights мінімальна кількість ночей (включно)
     * @return незмінний список відфільтрованих бронювань
     */
    public static List<Booking> filterByMinNights(List<Booking> bookings, int minNights) {
        if (minNights < 0) {
            throw new IllegalArgumentException("Поріг ночей не може бути від'ємним");
        }
        return streamOf(bookings)
            .filter(b -> b.getNights() >= minNights)
            .toList();
    }

    /**
     * Перетворює список бронювань у список номерів кімнат.
     *
     * @param bookings список бронювань
     * @return незмінний список номерів кімнат
     */
    public static List<Integer> extractRoomNumbers(List<Booking> bookings) {
        return streamOf(bookings)
            .map(Booking::getRoom)
            .toList();
    }

    /**
     * Групує бронювання за категорією номера та підраховує їх кількість у кожній групі.
     *
     * @param bookings список бронювань
     * @return карта, де ключ — категорія номера, а значення — кількість бронювань
     */
    public static Map<RoomKind, Long> countByKind(List<Booking> bookings) {
        return streamOf(bookings)
            .collect(Collectors.groupingBy(
                Booking::getKind,
                () -> new EnumMap<>(RoomKind.class),
                Collectors.counting()
            ));
    }

    /**
     * Обчислює зведену статистику тривалості проживання (кількість, сума, мінімум, максимум, середнє).
     *
     * @param bookings список бронювань
     * @return об'єкт {@link IntSummaryStatistics} за кількістю ночей
     */
    public static IntSummaryStatistics nightsStatistics(List<Booking> bookings) {
        return streamOf(bookings)
            .collect(Collectors.summarizingInt(Booking::getNights));
    }

    /**
     * Повертає перші {@code limit} найдовших бронювань.
     * Основний критерій — кількість ночей за спаданням, додатковий — номер кімнати за зростанням.
     *
     * @param bookings список бронювань
     * @param limit    максимальна кількість елементів у результаті
     * @return незмінний відсортований список найдовших бронювань
     */
    public static List<Booking> topLongestBookings(List<Booking> bookings, int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("Ліміт не може бути від'ємним");
        }
        Comparator<Booking> byNightsDescThenRoomAsc = Comparator
            .comparingInt(Booking::getNights)
            .reversed()
            .thenComparingInt(Booking::getRoom);

        return streamOf(bookings)
            .sorted(byNightsDescThenRoomAsc)
            .limit(limit)
            .toList();
    }

    /**
     * Виконує безпечний пошук першого бронювання за номером кімнати.
     *
     * @param bookings список бронювань
     * @param room     номер кімнати для пошуку
     * @return {@link Optional} із знайденим бронюванням або порожній {@link Optional}
     */
    public static Optional<Booking> findByRoom(List<Booking> bookings, int room) {
        return streamOf(bookings)
            .filter(b -> b.getRoom() == room)
            .findFirst();
    }

    /* Перевіряє список на null і відкриває послідовний потік. */
    private static Stream<Booking> streamOf(List<Booking> bookings) {
        return Objects.requireNonNull(bookings, "Список бронювань не може бути null").stream();
    }
}
