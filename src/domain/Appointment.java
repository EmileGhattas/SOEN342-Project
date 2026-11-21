package domain;

import java.time.LocalTime;

public class Appointment {
    private Patient patient;
    private HealthcareProfessional professional;
    private TimeSlot timeSlot;
    private MedicalService service;
    private Referral referral;
    private String reason; // may be null
    private AppointmentStatus status;
    private LocalTime arrivalTime;

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

    // A cancelled or completed appointment cannot change. cancelling frees the time slot
    public void setStatus(AppointmentStatus status) {
        if (this.status == AppointmentStatus.CANCELLED || this.status == AppointmentStatus.COMPLETED) {
            throw new IllegalStateException("Appointment is already " + this.status + ".");
        }
        this.status = status;
        if (status == AppointmentStatus.CANCELLED) {
            timeSlot.setStatus(SlotStatus.AVAILABLE);
        }
    }

    // Only a confirmed referral for this patient can be used
    public void setReferral(Referral ref) {
        if (ref.getPatient() != patient) {
            throw new IllegalArgumentException("Referral is for another patient.");
        }
        if (ref.getStatus() != ReferralStatus.CONFIRMED) {
            throw new IllegalArgumentException("Referral is not confirmed (status is " + ref.getStatus() + ").");
        }
        this.referral = ref;
    }

    // Registers the patient's arrival
    public void setArrivalTime(LocalTime arrivalTime) {
        if (status != AppointmentStatus.REQUESTED && status != AppointmentStatus.CONFIRMED) {
            throw new IllegalStateException("Cannot register arrival: appointment is " + status + ".");
        }
        this.arrivalTime = arrivalTime;
        this.status = AppointmentStatus.ARRIVED;
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

    @Override
    public String toString() {
        return patient.getFullName() + " - " + service.getName() + " on " + timeSlot + " [" + status + "]";
    }
}
