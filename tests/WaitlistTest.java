import application.Console;
import domain.*;
import domain.catalog.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

// Request Placement, Identify Eligible, Offer, Accept, Remove, Change Priority
public class WaitlistTest {
    private static int checks;

    public static void main(String[] args) {
        PatientCatalog patients = new PatientCatalog();
        StaffCatalog staff = new StaffCatalog();
        SpecialtyCatalog specialties = new SpecialtyCatalog();
        ServiceCatalog services = new ServiceCatalog();
        WaitlistCatalog waitlist = new WaitlistCatalog();
        Console console = new Console(patients, staff, specialties, services, new ReferralCatalog(),
                new AppointmentCatalog(), new AvailabilityCsv(staff, specialties, "pw"), waitlist, null);

        MedicalSpecialty cardio = new MedicalSpecialty("Cardiology");
        specialties.add(cardio);
        HealthcareProfessional alice = new HealthcareProfessional(1, "pw", "Alice", "Doc", "Dr.", "LIC1", cardio);
        HealthcareProfessional bob = new HealthcareProfessional(2, "pw", "Bob", "Doc", "Dr.", "LIC2", cardio);
        staff.add(alice);
        staff.add(bob);
        staff.add(new AdministrativeStaff(3, "pw", "Desk", "Staff"));
        Patient ana = new Patient(10, "pw", "Ana", "A", LocalDate.of(1990, 1, 1), "111");
        Patient ben = new Patient(11, "pw", "Ben", "B", LocalDate.of(1990, 1, 1), "222");
        Patient cleo = new Patient(12, "pw", "Cleo", "C", LocalDate.of(1990, 1, 1), "333");
        patients.add(ana);
        patients.add(ben);
        patients.add(cleo);

        LocalDate day = LocalDate.of(2026, 12, 7);
        TimeSlot a9 = alice.getSchedule().addTimeSlot(day, LocalTime.of(9, 0), LocalTime.of(9, 30));
        TimeSlot aLater = alice.getSchedule().addTimeSlot(day.plusDays(3), LocalTime.of(9, 0), LocalTime.of(9, 30));

        // Must be logged in
        rejects(() -> console.requestWaitlistPlacement(ana, alice, null, null, 1));

        // A healthcare professional may place patients but not offer, accept, remove or change priority
        console.login(1, "pw");
        WaitlistEntry low = console.requestWaitlistPlacement(cleo, null, cardio, null, 2);
        rejects(() -> console.offerAppointment(low, a9));
        rejects(() -> console.changeWaitlistPriority(low, 1));
        console.logout();
        console.login(3, "pw");

        // Request Placement on Waitlist
        rejects(() -> console.requestWaitlistPlacement(ana, null, null, null, 1));      // no criteria
        rejects(() -> console.requestWaitlistPlacement(ana, alice, null, null, 0));     // priority < 1
        rejects(() -> console.requestWaitlistPlacement(cleo, null, new MedicalSpecialty("cardiology"), null, 3)); // duplicate
        WaitlistEntry high = console.requestWaitlistPlacement(ben, alice, null, new DateRange(day, day), 1);
        WaitlistEntry otherDoc = console.requestWaitlistPlacement(ana, bob, null, null, 1);
        check(low.getStatus() == WaitlistStatus.WAITING && low.getRequestedAt().equals(LocalDate.now()),
                "New entry is Waiting and requested today");
        check(cleo.getWaitlistEntries().contains(low), "Entry is linked to its patient");
        check(waitlist.getAll().size() == 3, "Entries are added to the WaitlistCatalog");

        // Identify Eligible Waitlisted Patients
        check(console.identifyEligibleWaitlistedPatients(a9).equals(List.of(high, low)),
                "Eligible patients are sorted by priority; Bob's patient is excluded");
        check(console.identifyEligibleWaitlistedPatients(aLater).equals(List.of(low)),
                "An entry's date range is respected");

        // Change Waitlist Priority: equal priority, then earliest request first
        console.changeWaitlistPriority(low, 1);
        check(console.identifyEligibleWaitlistedPatients(a9).get(0) == low, "Equal priority: earlier request first");
        rejects(() -> console.changeWaitlistPriority(low, 0));

        // Offer Appointment, declined, then Remove Patient from Waitlist
        check(console.offerAppointment(low, a9) == low, "Offer returns the entry to phone");
        rejects(() -> console.offerAppointment(otherDoc, a9));
        console.removeFromWaitlist(low);
        check(low.getStatus() == WaitlistStatus.REMOVED, "Declined patient can be removed");
        rejects(() -> console.removeFromWaitlist(low));
        rejects(() -> console.offerAppointment(low, a9));

        // Offer Appointment, accepted
        MedicalService visit = console.registerService("Visit", 30, false);
        Appointment booked = console.acceptOffer(high, a9, visit);
        check(booked.getStatus() == AppointmentStatus.CONFIRMED && booked.getPatient() == ben,
                "Accepted offer books a confirmed appointment for the waitlisted patient");
        check(high.getStatus() == WaitlistStatus.FULFILLED && !a9.isAvailable(), "Entry fulfilled and slot booked");
        check(console.getWaitlist().equals(List.of(otherDoc)), "Only waiting entries are listed");
        rejects(() -> console.acceptOffer(otherDoc, aLater, visit)); // aLater is Alice's, entry wants Bob

        System.out.println("WaitlistTest: " + checks + " checks passed");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
        checks++;
    }

    private static void rejects(Runnable action) {
        try {
            action.run();
        } catch (IllegalArgumentException | IllegalStateException e) {
            checks++;
            return;
        }
        throw new AssertionError("Expected the operation to be rejected");
    }
}