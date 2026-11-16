package domain;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Schedule {
    private HealthcareProfessional professional;
    private List<TimeSlot> timeSlots = new ArrayList<>();

    public Schedule(HealthcareProfessional professional) {
        this.professional = professional;
    }

    public List<TimeSlot> findAvailable(DateRange dateRange) {
        // TODO (Search Availability)
        throw new UnsupportedOperationException("TODO: Schedule.findAvailable");
    }

    public boolean canBook(TimeSlot slot, MedicalService service) {
        // TODO (Book Appointment)
        throw new UnsupportedOperationException("TODO: Schedule.canBook");
    }

    private boolean meetsSchedulingConstraints(TimeSlot slot, MedicalService service) {
        // TODO (Book Appointment)
        throw new UnsupportedOperationException("TODO: Schedule.meetsSchedulingConstraints");
    }

    public TimeSlot addTimeSlot(LocalDate date, LocalTime startTime, LocalTime endTime) {
        // TODO (Make Availability)
        throw new UnsupportedOperationException("TODO: Schedule.addTimeSlot");
    }

    public List<TimeSlot> makeAvailability(DateRange range, RecurrencePattern pattern, LocalTime startTime, LocalTime endTime, Duration slotLength) {
        // TODO (Make Availability)
        throw new UnsupportedOperationException("TODO: Schedule.makeAvailability");
    }

    public HealthcareProfessional getProfessional() {
        return professional;
    }
}
