import application.Console;
import domain.*;
import domain.catalog.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public class BookingSetupTest {
    private static int checks;

    public static void main(String[] args) throws Exception {
        PatientCatalog patients = new PatientCatalog();
        StaffCatalog staff = new StaffCatalog();
        SpecialtyCatalog specialties = new SpecialtyCatalog();
        ServiceCatalog services = new ServiceCatalog();
        ReferralCatalog referrals = new ReferralCatalog();
        AppointmentCatalog appointments = new AppointmentCatalog();
        Console console = new Console(patients, staff, specialties, services, referrals, appointments,
                new AvailabilityCsv(staff, specialties, "pw"));
        check(staff.getAll().isEmpty() && patients.getAll().isEmpty() && services.getAll().isEmpty(),
                "Setup starts empty");
        rejects(() -> console.registerStaff(0, "pw", "Test", "Staff"));
        rejects(() -> console.registerStaff(5, " ", "Test", "Staff"));
        check(staff.getAll().isEmpty(), "Invalid setup creates no account");
        console.registerStaff(2, "pw", "Test", "Staff");
        rejects(() -> console.registerStaff(3, "pw", "Other", "Staff"));
        rejects(() -> console.registerPatient(1, "pw", "Test", "Patient", LocalDate.of(2000, 1, 1), "555", null));
        console.login(2, "pw");
        console.registerStaff(20, " spaced password ", "Other", "Staff");
        console.login(20, " spaced password ");
        check(console.getCurrentUser().getId() == 20, "Account setup preserves the password exactly");
        console.login(2, "pw");
        rejects(() -> console.registerStaff(2, "pw", "Duplicate", "Staff"));
        rejects(() -> console.registerStaff(-1, "pw", "Negative", "Staff"));
        Patient uninsured = console.registerPatient(1, "pw", "Test", "Patient", LocalDate.of(2000, 1, 1), "555", null);
        check(uninsured.getInsurances().isEmpty() && console.searchPatient(1) == uninsured,
                "Uninsured patient is supported");
        rejects(() -> console.registerPatient(1, "pw", "Duplicate", "Patient", LocalDate.of(2000, 1, 1), "555", null));
        rejects(() -> console.registerPatient(3, "pw", "Test", "Patient", null, "555", null));
        rejects(() -> console.registerPatient(3, "pw", "Test", "Patient", LocalDate.of(2000, 1, 1), "", null));
        check(patients.getAll().size() == 1, "Invalid patients are not inserted");
        Patient insured = console.registerPatient(3, "pw", "Insured", "Patient", LocalDate.of(2000, 1, 1), "555", new RAMQ("INS123"));
        check(console.searchPatient("ins123") == insured, "Setup insurance participates in lookup");
        MedicalService visit = console.registerService("Visit", 30, false);
        MedicalService specialist = console.registerService("Specialist", 30, true);
        rejects(() -> console.registerService("visit", 30, false));
        rejects(() -> console.registerService("", 30, false));
        rejects(() -> console.registerService("Invalid duration", 0, false));
        check(console.getServices().size() == 2, "Only valid services are registered");
        console.getServices().clear();
        check(services.getAll().size() == 2, "Service listing returns a copy");
        Path csv = Files.createTempFile("soen342-setup-", ".csv");
        Path exported = Files.createTempFile("soen342-export-", ".csv");
        try {
            Files.writeString(csv, AvailabilityCsv.HEADER + "\n"
                    + "Alice,Doctor,Dr.,LIC1,Cardiology,2026-12-01,09:00,09:30,AVAILABLE\n"
                    + "Bob,Doctor,Dr.,LIC2,Dermatology,2026-12-01,10:00,10:30,AVAILABLE\n");
            check(console.importAvailabilities(csv) == 2, "Import still works after manual staff setup");
            Set<Integer> ids = new HashSet<>();
            for (AuthorizedClinicStaff member : staff.getAll()) ids.add(member.getId());
            check(ids.size() == staff.getAll().size(), "CSV does not reuse a manually assigned staff id");
            console.logout();
            rejects(() -> console.getServices());
            // Import did not replace or shadow the administrator's credentials.
            console.login(2, "pw");
            MedicalSpecialty specialty = console.findSpecialty("cardiology");
            rejects(() -> console.findSpecialty("unknown"));
            ExternalPhysician external = new ExternalPhysician("EXT1", "External Doctor");
            Referral pending = console.recordReferral(uninsured, specialty, external, "Consultation",
                    ReferralPriority.ROUTINE, ReferralType.SPECIALIST, false);
            check(pending.getStatus() == ReferralStatus.REQUESTED, "Unconfirmed referral stays Requested");
            HealthcareProfessional hcp = staff.findProfessionalByLicense("LIC1");
            TimeSlot slot = hcp.getSchedule().getTimeSlots().get(0);
            rejects(() -> console.requestAppointment(uninsured, hcp, slot, specialist));
            Referral confirmed = console.recordReferral(uninsured, specialty, external, "Consultation",
                    ReferralPriority.URGENT, ReferralType.SPECIALIST, true);
            check(confirmed.getRequestedByPhysician() == external, "External physician is retained");
            rejects(() -> console.recordReferral(uninsured, specialty, external, " ",
                    ReferralPriority.ROUTINE, ReferralType.SPECIALIST, true));
            rejects(() -> console.recordReferral(uninsured, specialty, null, "Consultation",
                    ReferralPriority.ROUTINE, ReferralType.SPECIALIST, true));
            rejects(() -> console.recordReferral(uninsured, new MedicalSpecialty("Unknown"), external, "Consultation",
                    ReferralPriority.ROUTINE, ReferralType.SPECIALIST, true));
            check(referrals.getAll().size() == 2, "Invalid referrals do not enter the catalog");
            Appointment a = console.requestAppointment(uninsured, hcp, slot, specialist);
            check(a.getReferral() == confirmed, "Setup enables the confirmed-referral booking path");
            check(console.exportAvailabilities(exported) == 2, "Export still includes all slots");
            String output = Files.readString(exported);
            check(output.contains("09:00,09:30,BOOKED") && output.contains("10:00,10:30,AVAILABLE"),
                    "Export records booking without hiding unbooked slots");
            check(visit.getDuration() == 30 && appointments.getAll().size() == 1, "Setup is usable by booking");
        } finally {
            Files.deleteIfExists(csv);
            Files.deleteIfExists(exported);
        }
        System.out.println("BookingSetupTest: " + checks + " checks passed");
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
