package ua.lpnu.kzp;

import java.util.Locale;
import java.util.Objects;

/**
 * Описує коректний запис про бронювання в готелі (Варіант 14).
 * Клас є незмінним (immutable).
 */
public final class Booking {

    // Всі поля є private (закриті від зовнішнього втручання) та final (незмінні після створення)
    private final int room;
    private final String guest;
    private final int nights;
    private final double nightlyRate;
    private final String category;

    /**
     * Створює коректний запис про бронювання після перевірки інваріантів.
     */
    public Booking(int room, String guest, int nights, double nightlyRate, String category) {

        // 1. Перевірка на null для текстових полів (щоб програма не впала з NullPointerException)
        this.guest = Objects.requireNonNull(guest, "Ім'я гостя не може бути null");
        this.category = Objects.requireNonNull(category, "Категорія не може бути null");

        // 2. Перевірка на порожні рядки (як вимагає методичка для гостя і категорії)
        if (this.guest.isBlank() || this.category.isBlank()) {
            throw new IllegalArgumentException("Ім'я гостя та категорія не можуть бути порожніми");
        }

        // 3. Перевірка числових полів на від'ємні значення та коректність формату
        if (room <= 0 || nights < 0 || nightlyRate < 0 || !Double.isFinite(nightlyRate)) {
            throw new IllegalArgumentException("Номер кімнати має бути > 0, ночі та тариф >= 0, а тариф - скінченним числом");
        }

        // Якщо всі перевірки пройдені, присвоюємо значення полям
        this.room = room;
        this.nights = nights;
        this.nightlyRate = nightlyRate;
    }

    // --- Далі йдуть тільки ГЕТТЕРИ (методи для читання). СЕТТЕРІВ немає, щоб зберегти незмінність об'єкта ---

    public int getRoom() {
        return room;
    }

    public String getGuest() {
        return guest;
    }

    public int getNights() {
        return nights;
    }

    public double getNightlyRate() {
        return nightlyRate;
    }

    public String getCategory() {
        return category;
    }
    /**
     * Створює запис із CSV-рядка.
     *
     * @param line рядок у форматі: room;guest;nights;nightlyRate;category
     * @return створений запис (об'єкт Booking)
     * @throws IllegalArgumentException якщо структура або числа некоректні
     */
    public static Booking fromCsv(String line) {
        // Фабрика відповідає лише за структуру й типи полів, конструктор — за інваріанти
        Objects.requireNonNull(line, "Рядок не може бути null");
        String[] fields = line.split(";", -1); // Розділяємо рядок, зберігаючи порожні поля

        // Викладач вимагає перевірку на точну кількість полів
        if (fields.length != 5) {
            throw new IllegalArgumentException("Очікується 5 полів");
        }

        try {
            int room = Integer.parseInt(fields[0].trim());
            String guest = fields[1].trim();
            int nights = Integer.parseInt(fields[2].trim());
            double nightlyRate = Double.parseDouble(fields[3].trim());
            String category = fields[4].trim();

            return new Booking(room, guest, nights, nightlyRate, category);
        } catch (NumberFormatException exception) {
            // Перехоплюємо помилку перетворення тексту в число і кидаємо свою, зрозумілу
            throw new IllegalArgumentException("Числове поле має помилковий формат", exception);
        }
    }

    /**
     * Повертає текстове подання запису.
     */
    @Override
    public String toString() {
        return String.format(Locale.ROOT, "Кімната %d: %s, %d ночей, %.2f грн (Категорія: %s)",
            room, guest, nights, nightlyRate, category);
    }
}
