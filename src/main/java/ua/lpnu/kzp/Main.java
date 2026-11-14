package ua.lpnu.kzp;

/**
 * Головний клас запуску консольного застосунку обліку бронювань готелю.
 */
public class Main {

    /** Приватний конструктор, що запобігає створенню екземплярів утилітного класу. */
    private Main() {
    }

    /**
     * Точка входу в програму.
     *
     * @param args аргументи командного рядка ({@code --input}, {@code --output}, {@code --help}, {@code --version})
     */
    public static void main(String[] args) {
        ArgsParser parser = new ArgsParser(args);

        if (parser.isHelpRequested()) {
            System.out.printf("Використання: java -jar lab04.jar [--input <файл>] [--output <файл>]%n");
            return;
        }

        if (parser.isVersionRequested()) {
            System.out.printf("v%s%n", parser.getVersion());
            return;
        }

        String inputFile = parser.getInputFile();
        String outputFile = parser.getOutputFile();

        BookingController controller = new BookingController();
        controller.process(inputFile, outputFile);
    }
}
