import application.Console;
import domain.*;
import domain.catalog.*;
import ui.CommandLine;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Scanner;

public class BookingCliTest {
    private static int checks;

    public static void main(String[] args) throws Exception {
        fullBookingFromEmptyStartup();
        cancellationAndErrors();
        referralAndInsuranceSetup();
        Fixture passwords = new Fixture();
        String passwordOutput = passwords.run(lines("3", "1", "8", "Test", "Staff", " spaced password ", "0",
                "4", "8", " spaced password ", "0"));
        check(passwords.console.getCurrentUser() != null && passwords.console.getCurrentUser().getId() == 8,
                "CLI setup and login preserve password spaces");
        check(!passwordOutput.contains(" spaced password "), "Password input is not printed in program output");
        System.out.println("BookingCliTest: " + checks + " checks passed");
    }

    private static void fullBookingFromEmptyStartup() throws Exception {
        Fixture f = new Fixture();
        Path exported = Files.createTempFile("soen342-cli-", ".csv");
        try {
            String output = f.run(lines(
                    "3", "1", "2", "Test", "Staff", "setup-secret-123", "0",
                    "4", "2", "setup-secret-123",
                    "1", "data/availabilities.csv",
                    "3", "2", "10", "Pat", "One", "patient-secret-123", "2000-01-01", "555", "0",
                    "3", "Visit", "30", "n", "0",
                    "6", "1", "10", "1", "LIC1001", "Cardiology", "2026-12-01", "2026-12-01", "1", "y",
                    "5", "LIC1001", "Cardiology", "2026-12-01", "2026-12-01",
                    "6", "1", "10", "1", "LIC1001", "Cardiology", "2026-12-01", "2026-12-01",
                    "2", exported.toString(), "7", "6", "0"));
            check(output.contains("6 time slot(s) imported."), "CLI imports supplied availability");
            check(output.contains("Patient registered with id 10."), "CLI registers uninsured patient");
            check(output.contains("Appointment requested:") && output.contains("[REQUESTED]"),
                    "CLI confirms Requested appointment");
            check(f.appointments.getAll().size() == 1, "Second attempt does not create another booking");
            Appointment appointment = f.appointments.getAll().get(0);
            check(appointment.getPatient().getId() == 10,
                    "CLI selects the entered patient");
            check(appointment.getTimeSlot().getStatus() == SlotStatus.BOOKED, "CLI reserves selected slot");
            check(output.contains("No available time slots match this search."), "Booked slot is absent from later searches");
            check(output.contains("6 time slot(s) exported."), "CSV export remains available");
            check(Files.readAllLines(exported).size() == 7, "Export contains header and all six slots");
            long booked = Files.readAllLines(exported).stream().filter(row -> row.endsWith(",BOOKED")).count();
            check(booked == 2, "Export keeps the originally booked slot and the new booking");
            check(output.contains("Log in as clinic staff first."), "Logout protects the next booking");
            check(!output.contains("setup-secret-123") && !output.contains("patient-secret-123"),
                    "CLI output does not print password values");
            Fixture fresh = new Fixture();
            check(fresh.staff.getAll().isEmpty() && fresh.patients.getAll().isEmpty()
                    && fresh.appointments.getAll().isEmpty(), "A new application starts empty again");
        } finally {
            Files.deleteIfExists(exported);
        }
    }

    private static void cancellationAndErrors() {
        Fixture f = Fixture.ready();
        String patient = lines("6", "1", "10", "0", "0");
        check(!f.run(patient).contains("Appointment requested:"), "Cancelling service selection does not book");
        String slotCancel = lines("6", "1", "10", "1", "LIC1", "", "2026-12-01", "2026-12-01", "0", "0");
        check(!f.run(slotCancel).contains("Appointment requested:"), "Cancelling slot selection does not book");
        String no = lines("6", "1", "10", "1", "LIC1", "", "2026-12-01", "2026-12-01", "1", "n", "0");
        check(f.run(no).contains("Booking cancelled."), "Declining final confirmation cancels");
        check(f.slot.isAvailable() && f.appointments.getAll().isEmpty(), "Cancelled flows leave state unchanged");
        check(f.run(lines("6", "1", "999", "0")).contains("Patient not found."), "Missing patient has clear feedback");
        check(f.run(lines("6", "1", "10", "99", "0")).contains("Choose a listed number."),
                "Invalid service index is handled");
        check(f.run(lines("5", "", "Unknown specialty", "0")).contains("Specialty not found:"),
                "Unknown specialty does not silently search all specialties");
        check(f.run(lines("5", "", "", "2026-12-02", "2026-12-01", "0")).contains("is before start date"),
                "Reversed date range is handled");
        check(f.run(lines("5", "", "", "bad-date", "0")).contains("Enter dates as YYYY-MM-DD"),
                "Malformed date is handled");
        check(f.run(lines("6", "1", "not-a-number", "0")).contains("Enter a whole number."),
                "Malformed number is handled");
        check(f.run(lines("1", "missing-soen342-input.csv", "0")).contains("File not found:"),
                "Missing CSV does not crash the CLI");
        f.console.registerService("Long visit", 60, false);
        String longVisit = lines("6", "1", "10", "2", "LIC1", "", "2026-12-01", "2026-12-01", "1", "y", "0");
        check(f.run(longVisit).contains("cannot fit the service"), "Too-short slot is rejected through CLI");
        check(f.slot.isAvailable() && f.appointments.getAll().isEmpty(), "Rejected input does not reserve a slot");
        check(f.run("").contains("Input closed."), "EOF at menu exits cleanly");
        check(new Fixture().run(lines("3", "1", "9", "Partial")).contains("Input closed."),
                "EOF during setup exits cleanly");
        check(new Fixture().run(lines("5", "0")).contains("Log in as clinic staff first."),
                "Search requires staff login");
    }

    private static void referralAndInsuranceSetup() {
        Fixture f = Fixture.ready();
        String registrations = lines("3",
                "2", "11", "Ramq", "Patient", "pw", "2000-01-01", "555", "1", "RAMQ123",
                "2", "12", "Private", "Patient", "pw", "2000-01-01", "555", "2", "PRIVATE123", "0", "0");
        f.run(registrations);
        check(f.console.searchPatient("RAMQ123").getId() == 11, "RAMQ setup lookup works");
        check(f.console.searchPatient("PRIVATE123").getInsurances().get(0) instanceof PrivateInsurance,
                "Private insurance setup keeps coverage type");
        f.console.registerService("Specialist visit", 30, true);
        String pending = lines("3", "4", "1", "10", "Cardiology", "External Doctor", "EXT1", "Consultation",
                "1", "1", "n", "0",
                "6", "1", "10", "2", "LIC1", "", "2026-12-01", "2026-12-01", "1", "y", "0");
        String output = f.run(pending);
        check(output.contains("Referral recorded:") && output.contains("[REQUESTED]"), "Unconfirmed referral is recorded");
        check(output.contains("No confirmed referral matches"), "Unconfirmed referral cannot authorize a booking");
        check(f.slot.isAvailable() && f.appointments.getAll().isEmpty(), "Referral rejection preserves state");
        String confirmed = lines("3", "4", "2", "RAMQ123", "Cardiology", "External Doctor", "EXT1", "Consultation",
                "3", "1", "y", "0",
                "6", "2", "RAMQ123", "2", "LIC1", "", "2026-12-01", "2026-12-01", "1", "y", "0");
        check(f.run(confirmed).contains("Appointment requested:"), "Insurance lookup and confirmed-referral booking work");
        Appointment a = f.appointments.getAll().get(0);
        check(a.getPatient().getId() == 11 && a.getReferral().getPriority() == ReferralPriority.URGENT,
                "Booking uses the selected patient's recorded referral");
        check(a.getReferral().getRequestedByPhysician().getLicenseNumber().equals("EXT1"),
                "Recorded referral retains the external physician");
    }

    private static String lines(String... lines) {
        return String.join("\n", lines) + "\n";
    }

    private static void check(boolean value, String message) {
        checks++;
        if (!value) throw new AssertionError(message);
    }

    private static class Fixture {
        final PatientCatalog patients = new PatientCatalog();
        final StaffCatalog staff = new StaffCatalog();
        final SpecialtyCatalog specialties = new SpecialtyCatalog();
        final ServiceCatalog services = new ServiceCatalog();
        final ReferralCatalog referrals = new ReferralCatalog();
        final AppointmentCatalog appointments = new AppointmentCatalog();
        final Console console = new Console(patients, staff, specialties, services, referrals, appointments,
                new AvailabilityCsv(staff, specialties, "pw"));
        TimeSlot slot;

        static Fixture ready() {
            Fixture f = new Fixture();
            f.console.registerStaff(2, "pw", "Test", "Staff");
            f.console.login(2, "pw");
            f.console.registerPatient(10, "pw", "Test", "Patient", LocalDate.of(2000, 1, 1), "555", null);
            f.console.registerService("Visit", 30, false);
            MedicalSpecialty specialty = new MedicalSpecialty("Cardiology");
            f.specialties.add(specialty);
            HealthcareProfessional hcp = new HealthcareProfessional(3, "pw", "Alice", "Doctor", "Dr.", "LIC1", specialty);
            f.staff.add(hcp);
            f.slot = hcp.getSchedule().addTimeSlot(LocalDate.of(2026, 12, 1), LocalTime.of(9, 0), LocalTime.of(9, 30));
            return f;
        }

        String run(String input) {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            PrintStream output = new PrintStream(bytes, true, StandardCharsets.UTF_8);
            new CommandLine(console, new Scanner(input), output).run();
            return bytes.toString(StandardCharsets.UTF_8);
        }
    }
}
