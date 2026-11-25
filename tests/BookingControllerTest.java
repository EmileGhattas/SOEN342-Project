import application.Console;
import domain.*;
import domain.catalog.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class BookingControllerTest {
    private static int checks;

    public static void main(String[] args) {
        PatientCatalog patients = new PatientCatalog();
        StaffCatalog staff = new StaffCatalog();
        SpecialtyCatalog specialties = new SpecialtyCatalog();
        ServiceCatalog services = new ServiceCatalog();
        ReferralCatalog referrals = new ReferralCatalog();
        AppointmentCatalog appointments = new AppointmentCatalog();
        Console console = new Console(patients, staff, specialties, services, referrals, appointments,
                new AvailabilityCsv(staff, specialties, "pw"));
        MedicalSpecialty cardio = new MedicalSpecialty("Cardiology");
        MedicalSpecialty derm = new MedicalSpecialty("Dermatology");
        HealthcareProfessional alice = new HealthcareProfessional(1, "pw", "Alice", "Doctor", "Dr.", "LIC1", cardio);
        HealthcareProfessional bob = new HealthcareProfessional(2, "pw", "Bob", "Doctor", "Dr.", "LIC2", derm);
        AdministrativeStaff clerk = new AdministrativeStaff(3, "pw", "Desk", "Staff");
        Patient patient = new Patient(4, "pw", "Test", "Patient", LocalDate.of(2000, 1, 1), "555");
        Patient insured = new Patient(5, "pw", "Insured", "Patient", LocalDate.of(2001, 1, 1), "556");
        insured.addInsurance(new RAMQ("TEST123"));
        patients.add(patient);
        patients.add(insured);
        staff.add(alice);
        staff.add(bob);
        staff.add(clerk);
        MedicalService visit = new MedicalService("Visit", 30, false);
        MedicalService specialist = new MedicalService("Specialist", 30, true);
        services.add(visit);
        services.add(specialist);
        LocalDate date = LocalDate.of(2026, 12, 1);
        TimeSlot first = alice.getSchedule().addTimeSlot(date, LocalTime.of(9, 0), LocalTime.of(9, 30));
        TimeSlot last = alice.getSchedule().addTimeSlot(date.plusDays(1), LocalTime.of(10, 0), LocalTime.of(10, 30));
        TimeSlot booked = alice.getSchedule().addTimeSlot(date, LocalTime.of(11, 0), LocalTime.of(11, 30));
        booked.setStatus(SlotStatus.BOOKED);
        TimeSlot other = bob.getSchedule().addTimeSlot(date, LocalTime.of(8, 0), LocalTime.of(8, 30));
        DateRange range = new DateRange(date, date.plusDays(1));
        rejects(() -> console.searchPatient(4));
        rejects(() -> console.requestAppointment(patient, alice, first, visit));
        rejects(() -> console.login(4, "pw"));
        rejects(() -> console.requestAppointment(patient, alice, first, visit));
        console.login(3, "pw");
        check(console.searchPatient(4) == patient, "Uninsured patient is found by id");
        check(console.searchPatient("test123") == insured, "Insurance lookup is case-insensitive");
        check(console.searchPatient(999) == null, "Missing patient is reported");
        check(console.searchAvailability("ALICE", null, range).equals(List.of(first, last)),
                "Partial name and inclusive range exclude booked slots");
        check(console.searchAvailability("lic1", null, range).equals(List.of(first, last)), "License lookup");
        check(console.searchAvailability(null, new MedicalSpecialty("cardiology"), range).equals(List.of(first, last)),
                "Specialty query matches by name");
        check(console.searchAvailability("Alice", derm, range).isEmpty(), "Combined filters narrow results");
        check(console.searchAvailability("", null, range).equals(List.of(other, first, last)),
                "All-professional results are chronological");
        check(console.searchAvailability("missing", null, range).isEmpty(), "No professional yields no slots");
        List<TimeSlot> copy = alice.getSchedule().findAvailable(range);
        copy.clear();
        check(alice.getSchedule().getTimeSlots().size() == 3, "Query results do not expose schedule container");
        rejects(() -> console.searchAvailability("", null, null));
        Patient unknown = new Patient(20, "pw", "Missing", "Patient", date, "555");
        rejects(() -> console.requestAppointment(unknown, alice, first, visit));
        rejects(() -> console.requestAppointment(patient, alice, first, new MedicalService("Unknown", 30, false)));
        rejects(() -> console.requestAppointment(patient, bob, first, visit));
        rejects(() -> console.requestAppointment(patient, alice, first, specialist));
        check(first.isAvailable() && appointments.getAll().isEmpty(), "Failed controller requests do not mutate state");
        Appointment a = console.requestAppointment(patient, alice, first, visit);
        check(a.getStatus() == AppointmentStatus.REQUESTED,
                "Requested status matches CO2");
        check(!console.searchAvailability("Alice", null, range).contains(first), "Booked slot disappears from search");
        rejects(() -> console.requestAppointment(insured, alice, first, visit));
        Referral ref = new Referral("Consultation", ReferralPriority.ROUTINE, ReferralType.SPECIALIST,
                patient, cardio, null, alice);
        ref.setStatus(ReferralStatus.CONFIRMED);
        referrals.add(ref);
        check(console.requestAppointment(patient, alice, last, specialist).getReferral() == ref,
                "Controller finds and links matching referral");
        rejects(() -> console.login(3, "wrong"));
        check(console.getCurrentUser() == null, "Failed login clears previous session");
        rejects(() -> console.searchPatient(4));
        check(appointments.getAll().size() == 2, "Only successful requests are stored");
        System.out.println("BookingControllerTest: " + checks + " checks passed");
    }

    private static void check(boolean value, String message) {
        checks++;
        if (!value) throw new AssertionError(message);
    }

    private static void rejects(Runnable action) {
        checks++;
        try {
            action.run();
        } catch (IllegalArgumentException | IllegalStateException expected) {
            return;
        }
        throw new AssertionError("Expected rejection");
    }
}
