package ua.lpnu.kzp;

/**
 * Зберігає незмінні підсумкові показники проживання в готелі.
 *
 * @param validCount   кількість коректних записів
 * @param totalNights  сумарна кількість ночей
 * @param totalRevenue загальний виторг
 * @param maxNights    найдовше проживання
 */
public record NightsRate(int validCount, int totalNights, double totalRevenue, int maxNights) {

    /**
     * Компактний конструктор для перевірки допустимість підсумкових показників.
     */
    public NightsRate {
        if (validCount < 0 || totalNights < 0 || totalRevenue < 0 || maxNights < 0) {
            throw new IllegalArgumentException("Підсумкові показники не можуть бути від'ємними");
        }
        if (!Double.isFinite(totalRevenue)) {
            throw new IllegalArgumentException("Загальний виторг має бути скінченним числом");
        }
    }
}
