package application;

import domain.DateRange;
import domain.ExternalPhysician;
import domain.HealthcareProfessional;
import domain.MedicalService;
import domain.Patient;
import domain.PrivateInsurance;
import domain.RAMQ;
import domain.RecurrencePattern;
import domain.Resource;
import domain.ResourceType;
import domain.ReferralPriority;
import domain.ReferralType;
import domain.TimeSlot;
import java.io.IOException;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

// Fills an empty database with sample records, using the same operations as the menus
public class SampleData {

    public static void load(Console console) throws IOException {
        console.registerStaff(1, "admin", "Sam", "Desk");
        console.login(1, "admin");

        // Alice Tremblay (LIC1001), Marc Gagnon (LIC1002), Sofia Nguyen (LIC1003),
        // Julie Bouchard (LIC1004) and Karim Haddad (LIC1005) with their slots
        console.importAvailabilities(Path.of("data/availabilities.csv"));

        // Slots for the next four weeks, so there is always something to book
        LocalDate today = LocalDate.now();
        DateRange nextFourWeeks = new DateRange(today.plusDays(1), today.plusWeeks(4));
        RecurrencePattern weekdays = RecurrencePattern.weekly(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY);
        console.selectProfessional("LIC1001");
        console.makeAvailability(nextFourWeeks, weekdays, LocalTime.of(9, 0), LocalTime.of(12, 0), Duration.ofMinutes(30));
        console.selectProfessional("LIC1002");
        console.makeAvailability(nextFourWeeks, weekdays, LocalTime.of(13, 0), LocalTime.of(16, 0), Duration.ofMinutes(30));

        Patient ana = console.registerPatient(100, "pw", "Ana", "Lopez", LocalDate.of(1990, 5, 4),
                "514-555-0101", new RAMQ("LOPA90050412"));
        Patient ben = console.registerPatient(101, "pw", "Ben", "Roy", LocalDate.of(1985, 11, 20),
                "514-555-0102", new PrivateInsurance("SUN-4471"));
        console.registerPatient(102, "pw", "Chloe", "Martin", LocalDate.of(2001, 2, 14), "438-555-0103", null);

        Resource room1 = console.registerResource(ResourceType.ROOM, "Examination room 1");
        Resource room2 = console.registerResource(ResourceType.ROOM, "Examination room 2");
        Resource ecgMachine = console.registerResource(ResourceType.EQUIPMENT, "ECG machine");
        Resource dermatoscope = console.registerResource(ResourceType.EQUIPMENT, "Dermatoscope");
        Resource nurse = console.registerResource(ResourceType.HUMAN_ROLE, "Nurse");

        MedicalService checkup = console.registerService("General checkup", 30, false, room1);
        MedicalService cardiology = console.registerService("Cardiology consultation", 30, true, room2);
        console.registerService("Electrocardiogram (ECG)", 30, true, room2, ecgMachine, nurse);
        console.registerService("Dermatology consultation", 30, true, room1, dermatoscope);
        console.registerService("Vaccination", 15, false, nurse);

        // Ana has a confirmed referral, so she can book the cardiology consultation
        console.recordReferral(ana, console.findSpecialty("Cardiology"), new ExternalPhysician("EXT2001", "Dr. Paul Roy"),
                "Chest pain on exertion", ReferralPriority.SEMI_URGENT, ReferralType.SPECIALIST, true);

        // Two booked appointments with Alice Tremblay: one with a referral, one without
        List<TimeSlot> slots = console.searchAvailability("LIC1001", null, nextFourWeeks);
        HealthcareProfessional alice = slots.get(0).getProfessional();
        console.requestAppointment(ana, alice, slots.get(0), cardiology);
        console.requestAppointment(ben, alice, slots.get(1), checkup);

        console.logout();
    }
}
