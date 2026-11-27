package domain.catalog;

import domain.DateRange;
import domain.HealthcareProfessional;
import domain.MedicalSpecialty;
import domain.Patient;
import domain.TimeSlot;
import domain.WaitlistEntry;
import domain.WaitlistStatus;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class WaitlistCatalog extends Catalog<WaitlistEntry> {
    private List<WaitlistEntry> entries = new ArrayList<>();

    // Highest priority (1) first, then whoever asked first
    private static final Comparator<WaitlistEntry> BY_PRIORITY = Comparator
            .comparingInt(WaitlistEntry::getPriority)
            .thenComparing(WaitlistEntry::getRequestedAt)
            .thenComparingInt(WaitlistEntry::getId);

    @Override
    protected List<WaitlistEntry> items() {
        return entries;
    }

    // Creator: the catalog records WaitlistEntry objects, so it creates them
    public WaitlistEntry createEntry(Patient patient, HealthcareProfessional professional, MedicalSpecialty specialty,
                                     DateRange dateRange, int priority) {
        for (WaitlistEntry entry : entries) {
            if (entry.isSameRequest(patient, professional, specialty)) {
                throw new IllegalArgumentException("Patient is already waitlisted: " + entry);
            }
        }
        WaitlistEntry entry = new WaitlistEntry(patient, professional, specialty, priority, LocalDate.now(), dateRange);
        patient.addWaitlistEntry(entry);
        add(entry);
        return entry;
    }

    public WaitlistEntry findEntry(int id) {
        for (WaitlistEntry entry : entries) {
            if (entry.getId() == id) return entry;
        }
        return null;
    }

    // Identify Eligible Waitlisted Patients
    public List<WaitlistEntry> findEligible(TimeSlot slot) {
        List<WaitlistEntry> eligible = new ArrayList<>();
        for (WaitlistEntry entry : entries) {
            if (entry.isEligibleFor(slot)) {
                eligible.add(entry);
            }
        }
        eligible.sort(BY_PRIORITY);
        return eligible;
    }

    public List<WaitlistEntry> getWaiting() {
        List<WaitlistEntry> waiting = new ArrayList<>();
        for (WaitlistEntry entry : entries) {
            if (entry.getStatus() == WaitlistStatus.WAITING) {
                waiting.add(entry);
            }
        }
        waiting.sort(BY_PRIORITY);
        return waiting;
    }
}