package domain.catalog;

import domain.Appointment;
import domain.AppointmentStatus;
import domain.AuthorizedClinicStaff;
import domain.HealthcareProfessional;
import domain.MedicalService;
import domain.Patient;
import domain.Referral;
import domain.ReferralStatus;
import domain.SlotStatus;
import domain.TimeSlot;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AppointmentCatalog extends Catalog<Appointment> {
    private List<Appointment> appointments = new ArrayList<>();

    @Override
    protected List<Appointment> items() {
        return appointments;
    }

    public Appointment createAppointment(Patient patient, HealthcareProfessional hcp, TimeSlot slot, MedicalService service, AuthorizedClinicStaff staff, Referral ref) {
        if (patient == null || hcp == null || service == null || staff == null) {
            throw new IllegalArgumentException("Patient, professional, service and requesting staff are required.");
        }
        if (!hcp.getSchedule().canBook(slot, service)) {
            throw new IllegalArgumentException("The selected time slot is unavailable or cannot fit the service.");
        }
        if (service.requiresReferral() && ref == null) {
            throw new IllegalArgumentException("A confirmed referral is required for this service.");
        }
        if (ref != null && (ref.getPatient() != patient
                || ref.getStatus() != ReferralStatus.CONFIRMED
                || ref.getSpecialty() == null || hcp.getSpecialty() == null
                || !ref.getSpecialty().getName().equalsIgnoreCase(hcp.getSpecialty().getName()))) {
            throw new IllegalArgumentException("The referral must be confirmed and match the patient and specialty.");
        }

        Appointment appointment = new Appointment(patient, hcp, slot, service, staff);
        if (ref != null) {
            appointment.setReferral(ref);
        }
        // Complete validation before changing the slot or the catalog.
        slot.setStatus(SlotStatus.BOOKED);
        add(appointment);
        return appointment;
    }

    // The patient's earliest non-cancelled appointment on that date (or null)
    public Appointment findAppointment(Patient patient, LocalDate date) {
        Appointment earliest = null;
        for (Appointment appointment : appointments) {
            TimeSlot slot = appointment.getTimeSlot();
            if (appointment.getPatient() == patient 
                    && slot.getDate().equals(date)
                    && appointment.getStatus() != AppointmentStatus.CANCELLED
                    && (earliest == null || slot.getStartTime().isBefore(earliest.getTimeSlot().getStartTime()))) {
                earliest = appointment;
            }
        }
        return earliest;
    }
}
