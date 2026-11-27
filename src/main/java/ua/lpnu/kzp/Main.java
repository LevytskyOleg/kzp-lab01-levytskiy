package ua.lpnu.kzp;

/**
 * Головний клас запуску консольного застосунку обліку бронювань готелю.
 */
public class Main {

    /* Забороняє створення екземплярів утилітного класу. */
    private Main() {
    }

    /**
     * Точка входу в програму.
     *
     * @param args аргументи командного рядка ({@code --input}, {@code --output}, {@code --export}, {@code --help}, {@code --version})
     */
    public static void main(String[] args) {
        ArgsParser parser = new ArgsParser(args);

        if (parser.isHelpRequested()) {
            System.out.printf("Використання: java -jar lab05.jar [--input <файл>] [--output <файл>] [--export <csv-файл>]%n");
            return;
        }

        if (parser.isVersionRequested()) {
            System.out.printf("v%s%n", parser.getVersion());
            return;
        }

        BookingController controller = new BookingController();
        controller.process(parser.getInputFile(), parser.getOutputFile(), parser.getExportFile());
    }
}
