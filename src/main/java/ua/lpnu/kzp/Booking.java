package ua.lpnu.kzp;

import java.util.Objects;

/** Спільний базовий тип для бронювань. */
public abstract class Booking {
    private final int room;
    private final String guestName;
    private final int nights;
    private final RoomKind kind;

    /** Створює бронювання із перевіреними полями. */
    protected Booking(int room, String guestName, int nights, RoomKind kind) {
        this(validateRoom(room), validateName(guestName), validateNights(nights), validateKind(kind), true);
    }

    /* Присвоює вже перевірений стан. */
    private Booking(int room, String guestName, int nights, RoomKind kind, boolean validated) {
        this.room = room;
        this.guestName = guestName;
        this.nights = nights;
        this.kind = kind;
    }

    private static int validateRoom(int room) {
        if (room <= 0) {
            throw new IllegalArgumentException("Номер кімнати має бути додатним");
        }
        return room;
    }

    private static String validateName(String guestName) {
        if (guestName == null || guestName.isBlank()) {
            throw new IllegalArgumentException("Ім'я гостя не може бути порожнім");
        }
        return guestName;
    }

    private static int validateNights(int nights) {
        if (nights < 0) {
            throw new IllegalArgumentException("Кількість ночей не може бути від'ємною");
        }
        return nights;
    }

    private static RoomKind validateKind(RoomKind kind) {
        return Objects.requireNonNull(kind, "Категорія номеру не може бути null");
    }

    public final int getRoom() { return room; }
    public final String getGuestName() { return guestName; }
    public final int getNights() { return nights; }
    public final RoomKind getKind() { return kind; }

    /** Поліморфна операція: обчислює підсумкову вартість проживання. */
    public abstract double getCost();

    @Override
    public final boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        Booking booking = (Booking) other;
        return room == booking.room;
    }

    @Override
    public final int hashCode() {
        return Objects.hash(getClass(), room);
    }
}
