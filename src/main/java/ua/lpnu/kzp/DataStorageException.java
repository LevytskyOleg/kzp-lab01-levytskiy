package ua.lpnu.kzp;

/**
 * Повідомляє про помилку зберігання, розбору CSV або рефлексивного доступу до полів.
 */
public class DataStorageException extends Exception {

    /**
     * Створює виняток із текстовим описом помилки.
     *
     * @param message повідомлення про помилку
     */
    public DataStorageException(String message) {
        super(message);
    }

    /**
     * Створює виняток із текстовим описом помилки та першопричиною.
     *
     * @param message повідомлення про помилку
     * @param cause   першопричина винятку
     */
    public DataStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
