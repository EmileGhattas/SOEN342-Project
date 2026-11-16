package domain;

import java.time.LocalDate;

// An inclusive range of dates, e.g. 2026-11-01 to 2026-11-30
public class DateRange {
    private LocalDate start;
    private LocalDate end;

    public DateRange(LocalDate start, LocalDate end) {
        this.start = start;
        this.end = end;
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("End date " + end + " is before start date " + start);
        }
    }

    public boolean contains(LocalDate date) {
        return !date.isBefore(start) && !date.isAfter(end);
    }

    public LocalDate getStart() {
        return start;
    }

    public LocalDate getEnd() {
        return end;
    }

    @Override
    public String toString() {
        return start + " to " + end;
    }
}
