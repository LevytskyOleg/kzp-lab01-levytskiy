package ua.lpnu.kzp;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Клас для обробки аргументів командного рядка та зчитування налаштувань.
 */
public class ArgsParser {
    private String inputFile = "data/input.csv";
    private String outputFile = "report.txt";
    private boolean helpRequested = false;
    private boolean versionRequested = false;
    private String version = "unknown";

    public ArgsParser(String[] args) {
        loadVersion();
        parse(args);
    }

    private void loadVersion() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (input != null) {
                Properties prop = new Properties();
                prop.load(input);
                version = prop.getProperty("version", "unknown");
            }
        } catch (IOException e) {
            version = "unknown";
        }
    }

    private void parse(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if ("--help".equals(args[i])) {
                helpRequested = true;
            } else if ("--version".equals(args[i])) {
                versionRequested = true;
            } else if ("--input".equals(args[i]) && i + 1 < args.length) {
                inputFile = args[i + 1];
                i++;
            } else if ("--output".equals(args[i]) && i + 1 < args.length) {
                outputFile = args[i + 1];
                i++;
            }
        }
    }

    public String getInputFile() { return inputFile; }
    public String getOutputFile() { return outputFile; }
    public boolean isHelpRequested() { return helpRequested; }
    public boolean isVersionRequested() { return versionRequested; }
    public String getVersion() { return version; }
}
