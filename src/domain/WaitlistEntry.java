package domain;

import java.time.LocalDate;

// A patient waiting for an opening with a professional OR in a specialty
public class WaitlistEntry {
    private int id;
    private Patient patient;
    private HealthcareProfessional professional; // may be null
    private MedicalSpecialty specialty; // may be null
    private int priority;
    private LocalDate requestedAt;
    private DateRange dateRange;
    private WaitlistStatus status;

    public WaitlistEntry(Patient patient, HealthcareProfessional professional, MedicalSpecialty specialty,
                         int priority, LocalDate requestedAt, DateRange dateRange) {
        this.patient = patient;
        this.professional = professional;
        this.specialty = specialty;
        this.priority = priority;
        this.requestedAt = requestedAt;
        this.dateRange = dateRange;
        this.status = WaitlistStatus.WAITING;
    }

    public int getId() {
        return id;
    }

    public Patient getPatient() {
        return patient;
    }

    public HealthcareProfessional getProfessional() {
        return professional;
    }

    public MedicalSpecialty getSpecialty() {
        return specialty;
    }

    public int getPriority() {
        return priority;
    }

    public LocalDate getRequestedAt() {
        return requestedAt;
    }

    public DateRange getDateRange() {
        return dateRange;
    }

    public WaitlistStatus getStatus() {
        return status;
    }
    
    public void setId(int id) {
        this.id = id;
    }

    public void setStatus(WaitlistStatus status) {
        this.status = status;
    }
}
