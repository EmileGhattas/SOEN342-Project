package domain;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

// One bookable period in a professional's schedule
public class TimeSlot {
    private int id;
    private Schedule schedule;
    private ClinicLocation location; // may be null
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private SlotStatus status;

    // Created by Schedule
    TimeSlot(Schedule schedule, LocalDate date, LocalTime startTime, LocalTime endTime) {
        this.schedule = schedule;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        status = SlotStatus.AVAILABLE;
        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("Start time " + startTime + " must be before end time " + endTime);
        }
    }

    public boolean isAvailable() {
        return status == SlotStatus.AVAILABLE;
    }

    // Prevents double booking
    public void setStatus(SlotStatus status) {
        if (status == SlotStatus.BOOKED && this.status == SlotStatus.BOOKED) {
            throw new IllegalStateException("Time slot is already booked: " + this);
        }
        this.status = status;
    }

    // Back-to-back slots (9:00-9:30 and 9:30-10:00) do not overlap
    public boolean overlaps(TimeSlot other) {
        return date.equals(other.date) && startTime.isBefore(other.endTime) && other.startTime.isBefore(endTime);
    }

    public int getId() {
        return id;
    }

    public ClinicLocation getLocation() {
        return location;
    }

    public SlotStatus getStatus() {
        return status;
    }

    public HealthcareProfessional getProfessional() {
        return schedule.getProfessional();
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public Duration getDuration() {
        return Duration.between(startTime, endTime);
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public void setLocation(ClinicLocation location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return date + " " + startTime + "-" + endTime;
    }
}
