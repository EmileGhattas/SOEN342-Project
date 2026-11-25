import domain.*;
import domain.catalog.AppointmentCatalog;
import domain.catalog.ReferralCatalog;
import java.time.LocalDate;
import java.time.LocalTime;

/** Run with java BookingRulesTest; assertions do not require -ea or a framework. */
public class BookingRulesTest {
    private static int checks;

    public static void main(String[] args) {
        Fixture f = new Fixture();
        TimeSlot slot = f.slot(9);
        check(f.hcp.getSchedule().canBook(slot, f.service), "Available slot can be booked");
        check(!f.hcp.getSchedule().canBook(slot, new MedicalService("Long visit", 60, false)),
                "Service must fit one slot");
        check(!f.hcp.getSchedule().canBook(null, f.service), "Missing slot is rejected");
        HealthcareProfessional other = new HealthcareProfessional(3, "pw", "Other", "Doctor",
                "Dr.", "LIC2", f.specialty);
        check(!other.getSchedule().canBook(slot, f.service), "Foreign schedule is rejected");

        Appointment a = f.catalog.createAppointment(f.patient, f.hcp, slot, f.service, null);
        check(a.getStatus() == AppointmentStatus.REQUESTED, "Appointment starts Requested");
        check(a.getPatient() == f.patient && a.getProfessional() == f.hcp
                && a.getTimeSlot() == slot && a.getMedicalService() == f.service,
                "CO2 associations are retained");
        check(slot.getStatus() == SlotStatus.BOOKED && f.catalog.getAll().contains(a),
                "Slot is reserved and appointment is stored");
        rejects(() -> f.catalog.createAppointment(f.patient, f.hcp, slot, f.service, null));
        check(f.catalog.getAll().size() == 1, "Double booking creates no second appointment");

        MedicalService specialist = new MedicalService("Specialist visit", 30, true);
        TimeSlot next = f.slot(10);
        rejects(() -> f.catalog.createAppointment(f.patient, f.hcp, next, specialist, null));
        check(next.isAvailable() && f.catalog.getAll().size() == 1, "Missing referral has no state changes");
        Referral ref = new Referral("Consultation", ReferralPriority.ROUTINE, ReferralType.SPECIALIST,
                f.patient, f.specialty, null, f.hcp);
        ReferralCatalog referrals = new ReferralCatalog();
        referrals.add(ref);
        check(referrals.findRequiredReferral(f.patient, f.hcp, specialist) == null,
                "Requested referral is not accepted");
        rejects(() -> f.catalog.createAppointment(f.patient, f.hcp, next, specialist, ref));
        ref.setStatus(ReferralStatus.CONFIRMED);
        check(referrals.findRequiredReferral(f.patient, f.hcp, specialist) == ref,
                "Confirmed matching referral is found");
        check(referrals.findRequiredReferral(f.patient, f.hcp, f.service) == null,
                "Service without referral does not attach one");
        Patient stranger = new Patient(8, "pw", "Other", "Patient", LocalDate.of(2001, 1, 1), "555");
        check(referrals.findRequiredReferral(stranger, f.hcp, specialist) == null,
                "Another patient's referral is not returned");
        rejects(() -> f.catalog.createAppointment(stranger, f.hcp, next, specialist, ref));
        HealthcareProfessional dermatologist = new HealthcareProfessional(9, "pw", "Other", "Specialty",
                "Dr.", "LIC3", new MedicalSpecialty("Dermatology"));
        TimeSlot foreign = dermatologist.getSchedule().addTimeSlot(LocalDate.of(2026, 12, 1),
                LocalTime.of(11, 0), LocalTime.of(11, 30));
        check(referrals.findRequiredReferral(f.patient, dermatologist, specialist) == null,
                "Wrong specialty is not returned");
        rejects(() -> f.catalog.createAppointment(f.patient, dermatologist, foreign, specialist, ref));
        check(foreign.isAvailable(), "Wrong referral does not reserve a slot");
        Appointment referred = f.catalog.createAppointment(f.patient, f.hcp, next, specialist, ref);
        check(referred.getReferral() == ref && next.getStatus() == SlotStatus.BOOKED,
                "Valid referral is linked to a booked appointment");
        check(ref.getStatus() == ReferralStatus.CONFIRMED, "Booking does not consume a referral");
        TimeSlot shortSlot = f.slot(12);
        rejects(() -> f.catalog.createAppointment(f.patient, f.hcp, shortSlot,
                new MedicalService("Long", 31, false), null));
        check(shortSlot.isAvailable() && f.catalog.getAll().size() == 2,
                "Duration failure leaves state unchanged");
        rejects(() -> f.catalog.createAppointment(null, f.hcp, shortSlot, f.service, null));
        System.out.println("BookingRulesTest: " + checks + " checks passed");
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
        throw new AssertionError("Expected request rejection");
    }

    private static class Fixture {
        final MedicalSpecialty specialty = new MedicalSpecialty("Cardiology");
        final HealthcareProfessional hcp = new HealthcareProfessional(1, "pw", "Alice", "Doctor",
                "Dr.", "LIC1", specialty);
        final Patient patient = new Patient(2, "pw", "Test", "Patient", LocalDate.of(2000, 1, 1), "555");
        final AdministrativeStaff staff = new AdministrativeStaff(4, "pw", "Test", "Staff");
        final MedicalService service = new MedicalService("Visit", 30, false);
        final AppointmentCatalog catalog = new AppointmentCatalog();

        TimeSlot slot(int hour) {
            return hcp.getSchedule().addTimeSlot(LocalDate.of(2026, 12, 1),
                    LocalTime.of(hour, 0), LocalTime.of(hour, 30));
        }
    }
}
