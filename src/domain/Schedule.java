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
        if (dateRange == null) {
            throw new IllegalArgumentException("A date range is required.");
        }
        List<TimeSlot> available = new ArrayList<>();
        for (TimeSlot slot : timeSlots) {
            if (slot.isAvailable() && dateRange.contains(slot.getDate())) {
                available.add(slot);
            }
        }
        return available;
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

    // Creates back-to-back slots of the given length for each date in the range that matches the pattern.
    public List<TimeSlot> makeAvailability(DateRange range, RecurrencePattern pattern, LocalTime startTime, LocalTime endTime, Duration slotLength) {
        // Exceptions
        if (range == null || pattern == null || startTime == null || endTime == null || slotLength == null) {
            throw new IllegalArgumentException("Date range, pattern, start time, end time, and slot length are required.");
        }
        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("Start time" + startTime + "must be before end time " + endTime);
        }
        long slotMinutes = slotLength.toMinutes();
        if (slotMinutes <= 0) {
            throw new IllegalArgumentException("Slot length must be atleast one minute.");
        }
        // time left over at the end of the period (shorter than one slot) is not used
        long slotsPerDay = Duration.between(startTime, endTime).toMinutes() / slotMinutes;
        if (slotsPerDay == 0){
            throw new IllegalArgumentException("Slot length is longer than the period" + startTime + "-" + endTime + ".");
        }

        List<TimeSlot> created = new ArrayList<>();
        List<TimeSlot> conflicts = new ArrayList<>();
        for (LocalDate date : pattern.datesIn(range)) {
            for (long i = 0; i < slotsPerDay; i++) {
                LocalTime slotStart = startTime.plusMinutes(i * slotMinutes);
                TimeSlot slot = new TimeSlot(this, date, slotStart, slotStart.plusMinutes(slotMinutes));
                created.add(slot);
                for (TimeSlot existing : timeSlots) {
                    if (existing.overlaps(slot) && !conflicts.contains(existing)) { 
                        // If any conflicts exist
                        conflicts.add(existing);
                    }
                }
            }
        }
        // No matching dates in the given range were found
        if(created.isEmpty()) {
            throw new IllegalArgumentException("No date in " + range + " matches" + pattern + ".");
        }
        // Availability has a conflict with existing time slots
        if(!conflicts.isEmpty()) {
            throw new IllegalArgumentException("Availability conflicts with existing time slot(s): " + conflicts);
        }
        timeSlots.addAll(created);
        return created;
    }

    public List<TimeSlot> getTimeSlots() {
        return new ArrayList<>(timeSlots);
    }

    public HealthcareProfessional getProfessional() {
        return professional;
    }
}
