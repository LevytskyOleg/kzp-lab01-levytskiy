package ua.lpnu.kzp;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;

/**
 * Плоский запис бронювання готелю для збереження у сховищі та CSV-експорту/імпорту (Варіант 14).
 */
public final class BookingRecord {

    @CsvColumn("номер")
    private final int room;

    @CsvColumn("гість")
    private final String guest;

    @CsvColumn("заїзд")
    private final LocalDate checkIn;

    @CsvColumn("виїзд")
    private final LocalDate checkOut;

    @CsvColumn("тариф")
    private final double rate;

    /**
     * Створює перевірений запис бронювання готелю.
     *
     * @param room     номер кімнати (додатне число)
     * @param guest    ім'я гостя (не порожнє)
     * @param checkIn  дата заїзду
     * @param checkOut дата виїзду (не раніше дати заїзду)
     * @param rate     добовий тариф (невід'ємне скінченне число)
     */
    public BookingRecord(int room, String guest, LocalDate checkIn, LocalDate checkOut, double rate) {
        if (room <= 0) {
            throw new IllegalArgumentException("Номер кімнати має бути додатним");
        }
        if (guest == null || guest.isBlank()) {
            throw new IllegalArgumentException("Ім'я гостя не може бути порожнім");
        }
        Objects.requireNonNull(checkIn, "Дата заїзду не може бути null");
        Objects.requireNonNull(checkOut, "Дата виїзду не може бути null");
        if (checkOut.isBefore(checkIn)) {
            throw new IllegalArgumentException("Дата виїзду не може бути раніше дати заїзду");
        }
        if (rate < 0 || !Double.isFinite(rate)) {
            throw new IllegalArgumentException("Тариф має бути невід'ємним скінченним числом");
        }

        this.room = room;
        this.guest = guest;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.rate = rate;
    }

    /**
     * Відновлює об'єкт {@link BookingRecord} із одного CSV-рядка.
     *
     * @param line рядок CSV
     * @return відновлений запис бронювання
     * @throws DataStorageException якщо рядок пошкоджений або має некоректні значення
     */
    public static BookingRecord fromCsv(String line) throws DataStorageException {
        List<String> fields = CsvParser.parse(line);
        return fromFields(fields);
    }

    /**
     * Відновлює об'єкт {@link BookingRecord} зі списку полів у детермінованому порядку експорту:
     * {@code checkIn, checkOut, guest, rate, room}.
     *
     * @param fields список із 5 текстових значень полів
     * @return відновлений запис бронювання
     * @throws DataStorageException якщо кількість полів не дорівнює 5 або значення неможливо перетворити
     */
    public static BookingRecord fromFields(List<String> fields) throws DataStorageException {
        if (fields == null || fields.size() != 5) {
            throw new DataStorageException(
                "Очікується 5 полів CSV для BookingRecord, отримано: " + (fields == null ? 0 : fields.size()));
        }
        try {
            LocalDate checkIn = LocalDate.parse(fields.get(0).trim());
            LocalDate checkOut = LocalDate.parse(fields.get(1).trim());
            String guest = fields.get(2);
            double rate = Double.parseDouble(fields.get(3).trim());
            int room = Integer.parseInt(fields.get(4).trim());
            return new BookingRecord(room, guest, checkIn, checkOut, rate);
        } catch (DateTimeParseException | IllegalArgumentException exception) {
            throw new DataStorageException("Некоректні дані в полях CSV бронювання: " + fields, exception);
        }
    }

    /** @return номер кімнати */
    public int getRoom() { return room; }

    /** @return ім'я гостя */
    public String getGuest() { return guest; }

    /** @return дата заїзду */
    public LocalDate getCheckIn() { return checkIn; }

    /** @return дата виїзду */
    public LocalDate getCheckOut() { return checkOut; }

    /** @return добовий тариф */
    public double getRate() { return rate; }

    /**
     * Порівнює записи бронювань за всіма полями (включно з ключем round-trip {@code room + checkIn}).
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof BookingRecord that)) {
            return false;
        }
        return room == that.room
            && Double.compare(that.rate, rate) == 0
            && guest.equals(that.guest)
            && checkIn.equals(that.checkIn)
            && checkOut.equals(that.checkOut);
    }

    @Override
    public int hashCode() {
        return Objects.hash(room, guest, checkIn, checkOut, rate);
    }

    @Override
    public String toString() {
        return String.format("BookingRecord{room=%d, guest='%s', checkIn=%s, checkOut=%s, rate=%.2f}",
            room, guest, checkIn, checkOut, rate);
    }
}
