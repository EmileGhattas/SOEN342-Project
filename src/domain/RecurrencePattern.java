package domain;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

// How availability repeats
public class RecurrencePattern {
    private Set<DayOfWeek> daysOfWeek;
    private int dayOfMonth;

    private RecurrencePattern(Set<DayOfWeek> daysOfWeek, int dayOfMonth) {
        this.daysOfWeek = daysOfWeek;
        this.dayOfMonth = dayOfMonth;
    }

    public static RecurrencePattern weekly(DayOfWeek... days) {
        if (days.length == 0) {
            throw new IllegalArgumentException("Choose at least one day of the week.");
        }
        return new RecurrencePattern(Set.copyOf(Arrays.asList(days)), 0);
    }

    public static RecurrencePattern monthly(int dayOfMonth) {
        if (dayOfMonth < 1 || dayOfMonth > 31) {
            throw new IllegalArgumentException("Day of month must be between 1 and 31.");
        }
        return new RecurrencePattern(Set.of(), dayOfMonth);
    }

    // Every date in the range that matches the pattern, in order
    public List<LocalDate> datesIn(DateRange range) {
        List<LocalDate> dates = new ArrayList<>();
        for (LocalDate date = range.getStart(); range.contains(date); date=date.plusDays(1)) {
            if (matches(date)) {
                dates.add(date);
            }
        }
        return dates;
    }

    // A month without that day has not matching dates, e.g. February 30th.
    private boolean matches(LocalDate date) {
        if (dayOfMonth == 0) {
            return daysOfWeek.contains(date.getDayOfWeek());
        }
        return date.getDayOfMonth() == dayOfMonth;
    }

    @Override
    public String toString() {
        return dayOfMonth == 0 ? "every " + daysOfWeek : "monthly on day " + dayOfMonth;
    }
}
