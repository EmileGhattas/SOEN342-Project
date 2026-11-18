package ui;

import application.Console;
import domain.Appointment;
import domain.AuthorizedClinicStaff;
import domain.DateRange;
import domain.ExternalPhysician;
import domain.Insurance;
import domain.MedicalService;
import domain.MedicalSpecialty;
import domain.Patient;
import domain.PrivateInsurance;
import domain.RAMQ;
import domain.Referral;
import domain.ReferralPriority;
import domain.ReferralType;
import domain.TimeSlot;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class CommandLine {
    private Console console;
    private Scanner in;
    private PrintStream out;

    public CommandLine(Console console) {
        this(console, new Scanner(System.in), System.out);
    }

    public CommandLine(Console console, Scanner in, PrintStream out) {
        this.console = console;
        this.in = in;
        this.out = out;
    }

    public void run() {
        while (true) {
            try {
                out.println();
                out.println("Staff: " + (console.getCurrentUser() == null ? "not logged in" : console.getCurrentUser()));
                out.println("1. Import availabilities from CSV");
                out.println("2. Export availabilities to CSV");
                out.println("3. Setup booking records");
                out.println("4. Staff login");
                out.println("5. Search availability");
                out.println("6. Book appointment");
                out.println("7. Log out");
                out.println("0. Quit");
                String choice = read("Choice: ");
                if (choice.equals("0")) return;
                if (choice.equals("1")) {
                    Path source = Path.of(read("CSV file to import: "));
                    if (!Files.exists(source) && Files.exists(Path.of("data").resolve(source))) {
                        source = Path.of("data").resolve(source);
                    }
                    out.println(console.importAvailabilities(source) + " time slot(s) imported.");
                } else if (choice.equals("2")) {
                    Path target = Path.of(read("CSV file to write: "));
                    out.println(console.exportAvailabilities(target) + " time slot(s) exported.");
                } else if (choice.equals("3")) {
                    setup();
                } else if (choice.equals("4")) {
                    int id = readInt("Staff id: ");
                    console.login(id, readPassword("Password: "));
                    out.println("Logged in as " + console.getCurrentUser() + ".");
                } else if (choice.equals("5")) {
                    searchSlots();
                } else if (choice.equals("6")) {
                    bookAppointment();
                } else if (choice.equals("7")) {
                    console.logout();
                    out.println("Logged out.");
                } else {
                    out.println("Unknown choice, enter a number from 0 to 7.");
                }
            } catch (NoSuchElementException e) {
                out.println("Input closed.");
                return;
            } catch (NumberFormatException e) {
                out.println("Enter a whole number.");
            } catch (DateTimeParseException e) {
                out.println("Enter dates as YYYY-MM-DD, using a valid calendar date.");
            } catch (NoSuchFileException e) {
                out.println("File not found: " + Path.of(e.getFile()).toAbsolutePath());
            } catch (IOException e) {
                out.println("Could not access file: " + e.getMessage());
            } catch (RuntimeException e) {
                out.println("Error: " + e.getMessage());
            }
        }
    }

    private void setup() {
        while (true) {
            out.println();
            out.println("1. Add administrative staff (create the first account here)");
            out.println("2. Register patient");
            out.println("3. Add medical service");
            out.println("4. Record an external referral");
            out.println("0. Back");
            String choice = read("Setup choice: ");
            if (choice.equals("0")) return;
            if (choice.equals("1")) {
                int id = readInt("Staff id: ");
                String first = read("First name: ");
                String last = read("Last name: ");
                console.registerStaff(id, readPassword("Password: "), first, last);
                out.println("Staff account created. Use Staff login with id " + id + ".");
            } else if (choice.equals("2")) {
                requireStaff();
                registerPatient();
            } else if (choice.equals("3")) {
                requireStaff();
                String name = read("Service name: ");
                int duration = readInt("Duration in minutes: ");
                console.registerService(name, duration, yes("Referral required? (y/n): "));
                out.println("Medical service added.");
            } else if (choice.equals("4")) {
                requireStaff();
                recordReferral();
            } else {
                out.println("Unknown setup choice.");
            }
        }
    }

    private void registerPatient() {
        int id = readInt("Patient id: ");
        String first = read("First name: ");
        String last = read("Last name: ");
        String password = readPassword("Patient password: ");
        LocalDate birth = LocalDate.parse(read("Date of birth (YYYY-MM-DD): "));
        String phone = read("Phone number: ");
        out.println("Coverage: 0. None, 1. RAMQ, 2. Private insurance");
        int coverage = readChoice("Coverage: ", 2);
        Insurance insurance = null;
        if (coverage == 1) insurance = new RAMQ(read("Insurance number: "));
        if (coverage == 2) insurance = new PrivateInsurance(read("Insurance number: "));
        console.registerPatient(id, password, first, last, birth, phone, insurance);
        out.println("Patient registered with id " + id + ".");
    }

    private void recordReferral() {
        Patient patient = readPatient();
        if (patient == null) return;
        MedicalSpecialty specialty = console.findSpecialty(read("Requested specialty: "));
        String name = read("Referring physician name: ");
        String licence = read("Referring physician licence: ");
        String reason = read("Clinical reason: ");
        out.println("Priority: 1. Routine, 2. Semi-urgent, 3. Urgent");
        int priority = readChoice("Priority (0 to cancel): ", 3);
        if (priority == 0) return;
        out.println("Type: 1. Specialist, 2. Follow up");
        int type = readChoice("Type (0 to cancel): ", 2);
        if (type == 0) return;
        Referral referral = console.recordReferral(patient, specialty, new ExternalPhysician(licence, name),
                reason, ReferralPriority.values()[priority - 1], ReferralType.values()[type - 1],
                yes("Is the received referral confirmed? (y/n): "));
        out.println("Referral recorded: " + referral);
    }

    private Patient readPatient() {
        out.println("Find patient: 1. Patient id, 2. Insurance number, 0. Cancel");
        int choice = readChoice("Find by: ", 2);
        if (choice == 0) return null;
        Patient patient;
        if (choice == 1) patient = console.searchPatient(readInt("Patient id: "));
        else patient = console.searchPatient(read("Insurance number: "));
        if (patient == null) out.println("Patient not found. Register the patient in Setup first.");
        else out.println("Patient: " + patient);
        return patient;
    }

    private List<TimeSlot> searchSlots() {
        requireStaff();
        String professional = read("Professional name or licence (blank for any): ");
        String specialtyName = read("Specialty (blank for any): ");
        MedicalSpecialty specialty = specialtyName.isEmpty() ? null : console.findSpecialty(specialtyName);
        LocalDate start = LocalDate.parse(read("Start date (YYYY-MM-DD): "));
        LocalDate end = LocalDate.parse(read("End date (YYYY-MM-DD): "));
        List<TimeSlot> slots = console.searchAvailability(professional, specialty, new DateRange(start, end));
        if (slots.isEmpty()) out.println("No available time slots match this search.");
        for (int i = 0; i < slots.size(); i++) {
            TimeSlot slot = slots.get(i);
            out.println((i + 1) + ". " + slot.getProfessional().getFullName() + " ("
                    + slot.getProfessional().getLicenseNumber() + ", "
                    + slot.getProfessional().getSpecialty() + ") - " + slot);
        }
        return slots;
    }

    private void bookAppointment() {
        requireStaff();
        Patient patient = readPatient();
        if (patient == null) return;
        List<MedicalService> services = console.getServices();
        if (services.isEmpty()) {
            out.println("No medical services registered. Add one in Setup first.");
            return;
        }
        for (int i = 0; i < services.size(); i++) {
            MedicalService service = services.get(i);
            out.println((i + 1) + ". " + service + (service.requiresReferral() ? " - referral required" : ""));
        }
        int serviceChoice = readChoice("Service (0 to cancel): ", services.size());
        if (serviceChoice == 0) return;
        List<TimeSlot> slots = searchSlots();
        if (slots.isEmpty()) return;
        int slotChoice = readChoice("Time slot (0 to cancel): ", slots.size());
        if (slotChoice == 0) return;
        TimeSlot slot = slots.get(slotChoice - 1);
        MedicalService service = services.get(serviceChoice - 1);
        if (!yes("Request " + service.getName() + " on " + slot + "? (y/n): ")) {
            out.println("Booking cancelled. The slot remains available.");
            return;
        }
        Appointment appointment = console.requestAppointment(patient, slot.getProfessional(), slot, service);
        out.println("Appointment requested: " + appointment);
    }

    private void requireStaff() {
        if (!(console.getCurrentUser() instanceof AuthorizedClinicStaff)) {
            throw new IllegalStateException("Log in as clinic staff first.");
        }
    }

    private String read(String prompt) {
        out.print(prompt);
        return in.nextLine().trim();
    }

    private int readInt(String prompt) {
        return Integer.parseInt(read(prompt));
    }

    private String readPassword(String prompt) {
        out.print(prompt);
        return in.nextLine();
    }

    private int readChoice(String prompt, int maximum) {
        int choice = readInt(prompt);
        if (choice < 0 || choice > maximum) throw new IllegalArgumentException("Choose a listed number.");
        return choice;
    }

    private boolean yes(String prompt) {
        String answer = read(prompt);
        if (answer.equalsIgnoreCase("y")) return true;
        if (answer.equalsIgnoreCase("n")) return false;
        throw new IllegalArgumentException("Enter y or n.");
    }
}
