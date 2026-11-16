package ui;

import application.Console;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Scanner;

// Assuming user is administrative staff
public class CommandLine {
    private Console console;
    private Scanner in = new Scanner(System.in);

    public CommandLine(Console console) {
        this.console = console;
    }

    public void run() {
        String choice = "";
        while (!choice.equals("0")) {
            System.out.println();
            System.out.println("1. Import availabilities from CSV");
            System.out.println("2. Export availabilities to CSV");
            System.out.println("0. Quit");
            System.out.print("Choice: ");
            choice = in.nextLine().trim();
            try {
                if (choice.equals("1")) {
                    System.out.print("CSV file to import: ");
                    Path source = Path.of(in.nextLine().trim());
                    // look in data/ if not found in current directory
                    if (!Files.exists(source) && Files.exists(Path.of("data").resolve(source))) {
                        source = Path.of("data").resolve(source);
                    }
                    int count = console.importAvailabilities(source);
                    System.out.println(count + " time slot(s) imported.");
                } else if (choice.equals("2")) {
                    System.out.print("CSV file to write: ");
                    int count = console.exportAvailabilities(Path.of(in.nextLine().trim()));
                    System.out.println(count + " time slot(s) exported.");
                } else if (!choice.equals("0")) {
                    System.out.println("Unknown choice, enter 1, 2 or 0.");
                }
            } catch (NoSuchFileException e) {
                System.out.println("File not found: " + Path.of(e.getFile()).toAbsolutePath());
            } catch (IOException e) {
                System.out.println("Could not access file: " + e.getMessage());
            } catch (RuntimeException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }
}
