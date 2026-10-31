package ua.lpnu.kzp;

public class Main {
    public static void main(String[] args) {
        ArgsParser parser = new ArgsParser(args);

        if (parser.isHelpRequested()) {
            System.out.printf("Використання: java -jar lab03.jar [--input <файл>] [--output <файл>]%n");
            return;
        }

        if (parser.isVersionRequested()) {
            System.out.printf("v%s%n", parser.getVersion());
            return;
        }

        String inputFile = parser.getInputFile();
        String outputFile = parser.getOutputFile();

        // Запуск обработки через контроллер
        BookingController controller = new BookingController();
        controller.process(inputFile, outputFile);
    }
}
