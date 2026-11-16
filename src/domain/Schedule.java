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
        return slot != null && service != null && timeSlots.contains(slot)
                && slot.isAvailable() && meetsSchedulingConstraints(slot, service);
    }

    private boolean meetsSchedulingConstraints(TimeSlot slot, MedicalService service) {
        // This PoC books one whole slot; the service must fit inside it.
        return slot.getDuration().compareTo(Duration.ofMinutes(service.getDuration())) >= 0;
    }

    // Rejects a slot that overlaps one already in the schedule
    public TimeSlot addTimeSlot(LocalDate date, LocalTime startTime, LocalTime endTime) {
        TimeSlot slot = new TimeSlot(this, date, startTime, endTime);
        for (TimeSlot existing : timeSlots) {
            if (existing.overlaps(slot)) {
                throw new IllegalArgumentException("Time slot " + slot + " overlaps " + existing);
            }
        }
        timeSlots.add(slot);
        return slot;
    }

    public List<TimeSlot> makeAvailability(DateRange range, RecurrencePattern pattern, LocalTime startTime, LocalTime endTime, Duration slotLength) {
        // TODO (Make Availability)
        throw new UnsupportedOperationException("TODO: Schedule.makeAvailability");
    }

    public List<TimeSlot> getTimeSlots() {
        return new ArrayList<>(timeSlots);
    }

    public HealthcareProfessional getProfessional() {
        return professional;
    }
}
