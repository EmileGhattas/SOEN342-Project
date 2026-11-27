package domain;

import java.time.LocalDate;

// A patient waiting for an opening with a professional, in a specialty, and/or within a date range
public class WaitlistEntry {
    private int id;
    private Patient patient;
    private HealthcareProfessional professional; // may be null
    private MedicalSpecialty specialty; // may be null
    private int priority; // 1 is the highest priority
    private LocalDate requestedAt;
    private DateRange dateRange; // may be null
    private WaitlistStatus status;

    public WaitlistEntry(Patient patient, HealthcareProfessional professional, MedicalSpecialty specialty,
                         int priority, LocalDate requestedAt, DateRange dateRange) {
        if (patient == null) {
            throw new IllegalArgumentException("A patient is required.");
        }
        if (professional == null && specialty == null && dateRange == null) {
            throw new IllegalArgumentException("Choose a professional, a specialty or a date range.");
        }
        requireValidPriority(priority);
        this.patient = patient;
        this.professional = professional;
        this.specialty = specialty;
        this.priority = priority;
        this.requestedAt = requestedAt;
        this.dateRange = dateRange;
        this.status = WaitlistStatus.WAITING;
    }

    // An entry can be offered a slot if it is still waiting and the slot matches every criterion it has
    public boolean isEligibleFor(TimeSlot slot) {
        if (status != WaitlistStatus.WAITING || slot == null || !slot.isAvailable()) {
            return false;
        }
        HealthcareProfessional hcp = slot.getProfessional();
        boolean professionalMatches = professional == null || professional == hcp;
        boolean specialtyMatches = specialty == null || (hcp.getSpecialty() != null
                && hcp.getSpecialty().getName().equalsIgnoreCase(specialty.getName()));
        boolean dateMatches = dateRange == null || dateRange.contains(slot.getDate());
        return professionalMatches && specialtyMatches && dateMatches;
    }

    // Same patient already waiting for the same professional and specialty
    public boolean isSameRequest(Patient patient, HealthcareProfessional professional, MedicalSpecialty specialty) {
        return status == WaitlistStatus.WAITING && this.patient == patient
                && this.professional == professional && sameSpecialty(specialty);
    }

    public void changePriority(int priority) {
        requireWaiting();
        requireValidPriority(priority);
        this.priority = priority;
    }

    public void remove() {
        requireWaiting();
        status = WaitlistStatus.REMOVED;
    }

    public void fulfil() {
        requireWaiting();
        status = WaitlistStatus.FULFILLED;
    }

    private boolean sameSpecialty(MedicalSpecialty other) {
        if (specialty == null || other == null) {
            return specialty == other;
        }
        return specialty.getName().equalsIgnoreCase(other.getName());
    }

    private void requireWaiting() {
        if (status != WaitlistStatus.WAITING) {
            throw new IllegalStateException("Waitlist entry " + id + " is " + status + ", not WAITING.");
        }
    }

    private static void requireValidPriority(int priority) {
        if (priority < 1) {
            throw new IllegalArgumentException("Priority must be 1 or more (1 is the highest).");
        }
    }

    public int getId() { return id; }
    public Patient getPatient() { return patient; }
    public HealthcareProfessional getProfessional() { return professional; }
    public MedicalSpecialty getSpecialty() { return specialty; }
    public int getPriority() { return priority; }
    public LocalDate getRequestedAt() { return requestedAt; }
    public DateRange getDateRange() { return dateRange; }
    public WaitlistStatus getStatus() { return status; }

    public void setId(int id) { this.id = id; }

    // Used by the mapper when loading
    public void setStatus(WaitlistStatus status) { this.status = status; }

    @Override
    public String toString() {
        String wants = (professional != null ? professional.getFullName() : "any professional")
                + (specialty != null ? ", " + specialty.getName() : "")
                + (dateRange != null ? ", " + dateRange : "");
        return "#" + id + " " + patient.getFullName() + " (" + patient.getPhoneNum() + ") - priority " + priority
                + " - " + wants + " [" + status + "]";
    }
}