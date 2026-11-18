import application.Console;
import domain.*;
import domain.catalog.*;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class MakeAvailabilityTest {
    private static int checks;
 
    private static final LocalDate NOV_1 = LocalDate.of(2026, 11, 1);
    private static final LocalDate NOV_30 = LocalDate.of(2026, 11, 30);
    private static final DateRange NOVEMBER = new DateRange(NOV_1, NOV_30);
    private static final Duration HALF_HOUR = Duration.ofMinutes(30);
 
    public static void main(String[] args) {
        recurrencePattern();
        schedule();
        controller();
        System.out.println("MakeAvailabilityTest: " + checks + " checks passed");
    }
 
    private static void recurrencePattern() {
        List<LocalDate> mondays = RecurrencePattern.weekly(DayOfWeek.MONDAY).datesIn(NOVEMBER);
        check(mondays.size() == 5, "November 2026 has five Mondays");
        check(mondays.get(0).equals(LocalDate.of(2026, 11, 2))
                && mondays.get(4).equals(LocalDate.of(2026, 11, 30)), "First and last Mondays, range end included");
        check(RecurrencePattern.weekly(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY).datesIn(NOVEMBER).size() == 9,
                "Several days of the week are combined");
        List<LocalDate> fifteenths = RecurrencePattern.monthly(15)
                .datesIn(new DateRange(NOV_1, LocalDate.of(2027, 1, 31)));
        check(fifteenths.size() == 3 && fifteenths.get(2).equals(LocalDate.of(2027, 1, 15)),
                "Monthly pattern repeats across months and years");
        check(RecurrencePattern.monthly(31).datesIn(NOVEMBER).isEmpty(), "Month without that day has no date");
        rejects(() -> RecurrencePattern.weekly());
        rejects(() -> RecurrencePattern.monthly(32));
    }
 
    private static void schedule() {
        Schedule schedule = newProfessional().getSchedule();
        RecurrencePattern mondays = RecurrencePattern.weekly(DayOfWeek.MONDAY);
 
        // Handout example: during November, available every Monday from 9am to 12pm
        List<TimeSlot> created = schedule.makeAvailability(NOVEMBER, mondays,
                LocalTime.of(9, 0), LocalTime.of(12, 0), HALF_HOUR);
        check(created.size() == 30, "Five Mondays x six half-hour slots");
        check(schedule.getTimeSlots().size() == 30, "Created slots are added to the schedule");
        TimeSlot first = created.get(0);
        check(first.getDate().equals(LocalDate.of(2026, 11, 2)) && first.getStartTime().equals(LocalTime.of(9, 0))
                && first.getEndTime().equals(LocalTime.of(9, 30)), "First slot is Monday 09:00-09:30");
        check(created.get(5).getEndTime().equals(LocalTime.of(12, 0)), "Last slot of a day ends at the end time");
        check(first.isAvailable(), "New slots are available");
 
        // All or nothing: one overlapping day blocks the whole request
        rejects(() -> schedule.makeAvailability(new DateRange(LocalDate.of(2026, 11, 29), LocalDate.of(2026, 12, 1)),
                RecurrencePattern.weekly(DayOfWeek.MONDAY, DayOfWeek.TUESDAY),
                LocalTime.of(11, 0), LocalTime.of(13, 0), HALF_HOUR));
        check(schedule.getTimeSlots().size() == 30, "Conflict adds no slot, not even on the free day");
 
        // Back-to-back with existing slots is not an overlap
        List<TimeSlot> afternoon = schedule.makeAvailability(NOVEMBER, mondays,
                LocalTime.of(12, 0), LocalTime.of(13, 0), HALF_HOUR);
        check(afternoon.size() == 10 && schedule.getTimeSlots().size() == 40, "Adjacent period is accepted");
 
        // Time left over after the last full slot is not used
        List<TimeSlot> uneven = newProfessional().getSchedule().makeAvailability(NOVEMBER,
                RecurrencePattern.monthly(2), LocalTime.of(9, 0), LocalTime.of(10, 0), Duration.ofMinutes(25));
        check(uneven.size() == 2 && uneven.get(1).getEndTime().equals(LocalTime.of(9, 50)),
                "Leftover minutes are dropped");
 
        Schedule empty = newProfessional().getSchedule();
        rejects(() -> empty.makeAvailability(NOVEMBER, mondays, LocalTime.of(12, 0), LocalTime.of(9, 0), HALF_HOUR));
        rejects(() -> empty.makeAvailability(NOVEMBER, mondays, LocalTime.of(9, 0), LocalTime.of(9, 0), HALF_HOUR));
        rejects(() -> empty.makeAvailability(NOVEMBER, mondays, LocalTime.of(9, 0), LocalTime.of(10, 0), Duration.ZERO));
        rejects(() -> empty.makeAvailability(NOVEMBER, mondays, LocalTime.of(9, 0), LocalTime.of(9, 20), HALF_HOUR));
        rejects(() -> empty.makeAvailability(NOVEMBER, RecurrencePattern.monthly(31),
                LocalTime.of(9, 0), LocalTime.of(10, 0), HALF_HOUR));
        rejects(() -> empty.makeAvailability(null, mondays, LocalTime.of(9, 0), LocalTime.of(10, 0), HALF_HOUR));
        check(empty.getTimeSlots().isEmpty(), "Rejected requests leave the schedule empty");
    }
 
    private static void controller() {
        StaffCatalog staff = new StaffCatalog();
        SpecialtyCatalog specialties = new SpecialtyCatalog();
        Console console = new Console(new PatientCatalog(), staff, specialties, new ServiceCatalog(),
                new ReferralCatalog(), new AppointmentCatalog(), new AvailabilityCsv(staff, specialties, "pw"));
        HealthcareProfessional alice = new HealthcareProfessional(1, "pw", "Alice", "Doctor", "Dr.", "LIC1",
                new MedicalSpecialty("Cardiology"));
        staff.add(alice);
        staff.add(new AdministrativeStaff(10, "pw", "Desk", "Staff"));
        RecurrencePattern mondays = RecurrencePattern.weekly(DayOfWeek.MONDAY);
 
        rejects(() -> console.selectProfessional("LIC1"));
        console.login(10, "pw");
        rejects(() -> console.makeAvailability(NOVEMBER, mondays, LocalTime.of(9, 0), LocalTime.of(12, 0), HALF_HOUR));
        rejects(() -> console.selectProfessional("NOPE"));
        rejects(() -> console.selectProfessional(" "));
        check(console.selectProfessional("lic1") == alice, "Licence lookup ignores case");
 
        List<TimeSlot> created = console.makeAvailability(NOVEMBER, mondays,
                LocalTime.of(9, 0), LocalTime.of(12, 0), HALF_HOUR);
        check(created.size() == 30 && alice.getSchedule().getTimeSlots().size() == 30,
                "Controller adds slots to the selected professional");
        check(console.searchAvailability("LIC1", null,
                new DateRange(LocalDate.of(2026, 11, 2), LocalDate.of(2026, 11, 2))).size() == 6,
                "New slots appear in Search Availability");
 
        console.logout();
        console.login(10, "pw");
        rejects(() -> console.makeAvailability(NOVEMBER, mondays, LocalTime.of(13, 0), LocalTime.of(14, 0), HALF_HOUR));
        check(alice.getSchedule().getTimeSlots().size() == 30, "Logout clears the selected professional");
    }
 
    // A professional with an empty schedule
    private static HealthcareProfessional newProfessional() {
        return new HealthcareProfessional(1, "pw", "Alice", "Doctor", "Dr.", "LIC1",
                new MedicalSpecialty("Cardiology"));
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
}
