package domain.catalog;

import domain.Appointment;
import domain.AppointmentStatus;
import domain.AuthorizedClinicStaff;
import domain.HealthcareProfessional;
import domain.MedicalService;
import domain.Patient;
import domain.Referral;
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
        // TODO (Book Appointment)
        throw new UnsupportedOperationException("TODO: AppointmentCatalog.createAppointment");
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
