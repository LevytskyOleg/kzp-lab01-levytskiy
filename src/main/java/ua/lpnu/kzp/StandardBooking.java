package ua.lpnu.kzp;

/** Стандартне бронювання. */
public final class StandardBooking extends Booking {
    private final double nightlyRate;

    public StandardBooking(int room, String guestName, int nights, double nightlyRate) {
        // Викликаємо конструктор базового абстрактного класу
        super(room, guestName, nights, RoomKind.STANDARD);
        if (nightlyRate < 0) {
            throw new IllegalArgumentException("Тариф не може бути від'ємним");
        }
        this.nightlyRate = nightlyRate;
    }

    /** Обчислює вартість проживання за стандартним тарифом. */
    @Override
    public double getCost() {
        return getNights() * nightlyRate;
    }
}
