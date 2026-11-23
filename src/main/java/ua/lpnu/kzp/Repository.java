package ua.lpnu.kzp;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Типобезпечне параметризоване сховище об'єктів у пам'яті.
 *
 * @param <T> тип елементів, що зберігаються
 */
public final class Repository<T> {
    private final List<T> items = new ArrayList<>();

    /** Створює порожнє сховище. */
    public Repository() {
    }

    /**
     * Додає непорожній запис до сховища.
     *
     * @param item запис для додавання
     */
    public void add(T item) {
        items.add(Objects.requireNonNull(item, "Запис не може бути null"));
    }

    /**
     * Повертає незмінний знімок усіх записів у сховищі.
     *
     * @return незмінний список записів
     */
    public List<T> all() {
        return List.copyOf(items);
    }

    /**
     * Повертає список записів, що задовольняють переданий предикат.
     *
     * @param condition умова відбору
     * @return незмінний список знайдених записів
     */
    public List<T> find(Predicate<? super T> condition) {
        Objects.requireNonNull(condition, "Умова пошуку не може бути null");
        return items.stream()
            .filter(condition)
            .toList();
    }
}
