package ua.lpnu.kzp;

/** Преміум бронювання (Люкс). */
public final class PremiumBooking extends Booking {
    private final double nightlyRate;

    public PremiumBooking(int room, String guestName, int nights, double nightlyRate) {
        // Викликаємо конструктор базового абстрактного класу з іншою категорією
        super(room, guestName, nights, RoomKind.PREMIUM);
        if (nightlyRate < 0) {
            throw new IllegalArgumentException("Тариф не може бути від'ємним");
        }
        this.nightlyRate = nightlyRate;
    }

    /** Обчислює вартість проживання із преміум-надбавкою 20%. */
    @Override
    public double getCost() {
        return (getNights() * nightlyRate) * 1.20;
    }
}
