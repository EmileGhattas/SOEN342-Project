package domain;

import java.time.LocalTime;

public class Appointment {
    private int id;
    private Patient patient;
    private HealthcareProfessional professional;
    private TimeSlot timeSlot;
    private MedicalService service;
    private Referral referral;
    private String reason; // may be null
    private AppointmentStatus status;
    private LocalTime arrivalTime;
    private String additionalNotes; // may be null

    public Appointment(Patient patient, HealthcareProfessional hcp, TimeSlot slot, MedicalService service) {
        this(patient, hcp, slot, service, null);
    }

    public Appointment(Patient patient, HealthcareProfessional hcp, TimeSlot slot, MedicalService service, String reason) {
        this.patient = patient;
        this.professional = hcp;
        this.timeSlot = slot;
        this.service = service;
        this.reason = reason;
        if (slot.getProfessional() != hcp) {
            throw new IllegalArgumentException("Time slot " + slot + " is not in " + hcp.getName() + "'s schedule.");
        }
        this.status = AppointmentStatus.REQUESTED;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    // The referral is checked when booking (AppointmentCatalog.createAppointment)
    public void setReferral(Referral ref) {
        this.referral = ref;
    }

    public void setArrivalTime(LocalTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    // Free text recorded when closing the appointment
    public void setAdditionalNotes(String additionalNotes) {
        this.additionalNotes = additionalNotes;
    }

    public void setId(int id) {
        this.id = id;
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

    public TimeSlot getTimeSlot() {
        return timeSlot;
    }

    public MedicalService getMedicalService() {
        return service;
    }

    public Referral getReferral() {
        return referral;
    }

    public String getReason() {
        return reason;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public LocalTime getArrivalTime() {
        return arrivalTime;
    }

    public String getAdditionalNotes() {
        return additionalNotes;
    }

    @Override
    public String toString() {
        return patient.getFullName() + " - " + service.getName() + " on " + timeSlot + " [" + status + "]";
    }
}
