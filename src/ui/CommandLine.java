package ui;

import application.Console;
import domain.Appointment;
import domain.AdministrativeStaff;
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
import domain.HealthcareProfessional;
import domain.User;
import domain.RecurrencePattern;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
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
                boolean keepGoing;
                User user = console.getCurrentUser();
                if (user == null) keepGoing = loginMenu();
                else if (user instanceof Patient) keepGoing = patientMenu();
                else if (user instanceof AdministrativeStaff) keepGoing = adminMenu();
                else keepGoing = professionalMenu();
                if (!keepGoing) return;
            } catch (NoSuchElementException e) {
                out.println("Input closed.");
                return;
            } catch (NumberFormatException e) {
                out.println("Enter a whole number.");
            } catch (DateTimeParseException e) {
                out.println("Enter dates as YYYY-MM-DD and times as HH:MM, using valid calendar dates and times.");
            } catch (NoSuchFileException e) {
                out.println("File not found: " + Path.of(e.getFile()).toAbsolutePath());
            } catch (IOException e) {
                out.println("Could not access file: " + e.getMessage());
            } catch (RuntimeException e) {
                out.println("Error: " + e.getMessage());
            }
        }
    }

    // Each menu returns false when the user quits
    private boolean loginMenu() {
        out.println("Not logged in");
        out.println("1. Log in");
        out.println("0. Quit");
        String choice = read("Choice: ");
        if (choice.equals("0")) return false;
        if (choice.equals("1")) {
            int id = readInt("User id: ");
            console.login(id, readPassword("Password: "));
            out.println("Logged in as " + console.getCurrentUser() + ".");
        } else {
            out.println("Unknown choice.");
        }
        return true;
    }

    private boolean patientMenu() {
        out.println("Patient: " + console.getCurrentUser());
        out.println("1. Search availability");
        out.println("2. Request appointment");
        out.println("3. Confirm appointment (todo)");
        out.println("4. Reschedule appointment (todo)");
        out.println("5. Cancel appointment (todo)");
        out.println("6. Request placement on waitlist (todo)");
        out.println("L. Log out");
        out.println("0. Quit");
        String choice = read("Choice: ");
        if (choice.equals("0")) return false;
        if (choice.equals("1")) searchSlots();
        else if (choice.equals("2")) bookAppointment();
        else if (choice.equals("3")) out.println("Not implemented yet.");
        else if (choice.equals("4")) out.println("Not implemented yet.");
        else if (choice.equals("5")) out.println("Not implemented yet.");
        else if (choice.equals("6")) out.println("Not implemented yet.");
        else if (choice.equalsIgnoreCase("L")) logout();
        else out.println("Unknown choice.");
        return true;
    }

    private boolean professionalMenu() {
        out.println("Healthcare professional: " + console.getCurrentUser());
        out.println("1. Search availability");
        out.println("2. Request appointment");
        out.println("3. Confirm appointment (todo)");
        out.println("4. Reschedule appointment (todo)");
        out.println("5. Cancel appointment (todo)");
        out.println("6. Search patient (todo)");
        out.println("7. Issue referral (todo)");
        out.println("8. Record clinical information (todo)");
        out.println("L. Log out");
        out.println("0. Quit");
        String choice = read("Choice: ");
        if (choice.equals("0")) return false;
        if (choice.equals("1")) searchSlots();
        else if (choice.equals("2")) bookAppointment();
        else if (choice.equals("3")) out.println("Not implemented yet.");
        else if (choice.equals("4")) out.println("Not implemented yet.");
        else if (choice.equals("5")) out.println("Not implemented yet.");
        else if (choice.equals("6")) out.println("Not implemented yet.");
        else if (choice.equals("7")) out.println("Not implemented yet.");
        else if (choice.equals("8")) out.println("Not implemented yet.");
        else if (choice.equalsIgnoreCase("L")) logout();
        else out.println("Unknown choice.");
        return true;
    }

    private boolean adminMenu() throws IOException {
        out.println("Administrative staff: " + console.getCurrentUser());
        out.println("1. Search availability");
        out.println("2. Request appointment");
        out.println("3. Make availability");
        out.println("4. Import/export CSV");
        out.println("5. Register patient");
        out.println("6. Record external referral");
        out.println("7. Confirm appointment (todo)");
        out.println("8. Reschedule appointment (todo)");
        out.println("9. Cancel appointment (todo)");
        out.println("10. Search patient (todo)");
        out.println("11. Register patient arrival (todo)");
        out.println("12. Offer appointment (todo)");
        out.println("13. Remove patient from waitlist (todo)");
        out.println("14. Change waitlist patient priority (todo)");
        out.println("L. Log out");
        out.println("0. Quit");
        String choice = read("Choice: ");
        if (choice.equals("0")) return false;
        if (choice.equals("1")) searchSlots();
        else if (choice.equals("2")) bookAppointment();
        else if (choice.equals("3")) makeAvailability();
        else if (choice.equals("4")) importExportCsv();
        else if (choice.equals("5")) registerPatient();
        else if (choice.equals("6")) recordReferral();
        else if (choice.equals("7")) out.println("Not implemented yet.");
        else if (choice.equals("8")) out.println("Not implemented yet.");
        else if (choice.equals("9")) out.println("Not implemented yet.");
        else if (choice.equals("10")) out.println("Not implemented yet.");
        else if (choice.equals("11")) out.println("Not implemented yet.");
        else if (choice.equals("12")) out.println("Not implemented yet.");
        else if (choice.equals("13")) out.println("Not implemented yet.");
        else if (choice.equals("14")) out.println("Not implemented yet.");
        else if (choice.equalsIgnoreCase("L")) logout();
        else out.println("Unknown choice.");
        return true;
    }

    private void importExportCsv() throws IOException {
        out.println("1. Import availabilities, 2. Export availabilities, 0. Cancel");
        int choice = readChoice("CSV choice: ", 2);
        if (choice == 1) {
            Path source = Path.of(read("CSV file to import: "));
            if (!Files.exists(source) && Files.exists(Path.of("data").resolve(source))) {
                source = Path.of("data").resolve(source);
            }
            out.println(console.importAvailabilities(source) + " time slot(s) imported.");
        } else if (choice == 2) {
            Path target = Path.of(read("CSV file to write: "));
            out.println(console.exportAvailabilities(target) + " time slot(s) exported.");
        }
    }

    private void logout() {
        console.logout();
        out.println("Logged out.");
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
        if (patient == null) out.println("Patient not found. Register the patient first.");
        else out.println("Patient: " + patient);
        return patient;
    }

    private List<TimeSlot> searchSlots() {
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
        // A patient books for themselves, staff choose the patient
        Patient patient;
        if (console.getCurrentUser() instanceof Patient) patient = (Patient) console.getCurrentUser();
        else patient = readPatient();
        if (patient == null) return;
        List<MedicalService> services = console.getServices();
        if (services.isEmpty()) {
            out.println("No medical services available.");
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

    private void makeAvailability(){
        HealthcareProfessional hcp = console.selectProfessional(read("Professional's license number: "));
        out.println("Selected: " + hcp.getTitle() + " " + hcp.getFullName() + " (" + hcp.getSpecialty() + ")");
        LocalDate start = LocalDate.parse(read("Start date (YYYY-MM-DD): "));
        LocalDate end = LocalDate.parse(read("End date (YYYY-MM-DD): "));
        DateRange range = new DateRange(start, end);
        out.println("Repeat: 1. Weekly, 2. Monthly");
        int repeat = readChoice("Repeat (0 to cancel): ", 2);
        if (repeat == 0) return;
        RecurrencePattern pattern = repeat == 1
                ? RecurrencePattern.weekly(readDays("Day(s) of week, e.g. MON,WED: "))
                : RecurrencePattern.monthly(readInt("Day of month (1-31): "));
        LocalTime startTime = LocalTime.parse(read("Start time (HH:MM): "));
        LocalTime endTime = LocalTime.parse(read("End time (HH:MM): "));
        Duration slotLength = Duration.ofMinutes(readInt("Slot length in minutes: "));
        List<TimeSlot> created = console.makeAvailability(range, pattern, startTime, endTime, slotLength);
        out.println(created.size() + " time slot(s) created for " + hcp.getFullName() + ", from "
                + created.get(0) + " to " + created.get(created.size() - 1) + ".");
    }

    // "MON, wednesday" -> MONDAY, WEDNESDAY
    private DayOfWeek[] readDays(String prompt) {
        String[] names = read(prompt).split(",");
        DayOfWeek[] days = new DayOfWeek[names.length];
        for (int i = 0; i < names.length; i++) {
            String name = names[i].trim().toUpperCase();
            for (DayOfWeek day : DayOfWeek.values()) {
                if (name.length() >= 3 && day.name().startsWith(name)) {
                    days[i] = day;
                }
            }
            if (days[i] == null) {
                throw new IllegalArgumentException("'" + names[i].trim() + "' is not a day of the week.");
            }
        }
        return days;
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
