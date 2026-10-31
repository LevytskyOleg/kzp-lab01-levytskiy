package ua.lpnu.kzp;

/** Категорія кімнати готелю. */
public enum RoomKind {
    STANDARD("Стандарт"),
    PREMIUM("Люкс");

    private final String label;

    RoomKind(String label) {
        this.label = label;
    }

    /** Повертає текстове подання категорії для звіту. */
    public String label() {
        return label;
    }
}
