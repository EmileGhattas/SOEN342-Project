package application;

import domain.AvailabilityCsv;
import domain.Appointment;
import domain.AuthorizedClinicStaff;
import domain.DateRange;
import domain.HealthcareProfessional;
import domain.MedicalService;
import domain.MedicalSpecialty;
import domain.Patient;
import domain.Referral;
import domain.TimeSlot;
import domain.User;
import domain.catalog.AppointmentCatalog;
import domain.catalog.PatientCatalog;
import domain.catalog.ReferralCatalog;
import domain.catalog.ServiceCatalog;
import domain.catalog.SpecialtyCatalog;
import domain.catalog.StaffCatalog;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

// Controller: Receives the system operations from the UI
public class Console {
    private PatientCatalog patientCatalog;
    private StaffCatalog staffCatalog;
    private SpecialtyCatalog specialtyCatalog;
    private ServiceCatalog serviceCatalog;
    private ReferralCatalog referralCatalog;
    private AppointmentCatalog appointmentCatalog;
    private AvailabilityCsv availabilityCsv;
    private User currentUser;

    public Console(PatientCatalog patientCatalog, StaffCatalog staffCatalog,
                   SpecialtyCatalog specialtyCatalog, ServiceCatalog serviceCatalog,
                   ReferralCatalog referralCatalog, AppointmentCatalog appointmentCatalog,
                   AvailabilityCsv availabilityCsv) {
        this.patientCatalog = Objects.requireNonNull(patientCatalog);
        this.staffCatalog = Objects.requireNonNull(staffCatalog);
        this.specialtyCatalog = Objects.requireNonNull(specialtyCatalog);
        this.serviceCatalog = Objects.requireNonNull(serviceCatalog);
        this.referralCatalog = Objects.requireNonNull(referralCatalog);
        this.appointmentCatalog = Objects.requireNonNull(appointmentCatalog);
        this.availabilityCsv = Objects.requireNonNull(availabilityCsv);
    }

    public void login(int id, String password) {
        currentUser = null;
        // This PoC implements the staff-assisted booking path.
        User user = staffCatalog.findStaff(id);
        if (user == null) throw new IllegalArgumentException("Invalid id or password.");
        user.login(id, password);
        currentUser = user;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public Patient searchPatient(String insuranceNumber) {
        requireStaff();
        return patientCatalog.findPatient(insuranceNumber);
    }

    public Patient searchPatient(int id) {
        requireStaff();
        return patientCatalog.findPatient(id);
    }

    public List<TimeSlot> searchAvailability(String professional, MedicalSpecialty specialty, DateRange range) {
        requireStaff();
        if (range == null) throw new IllegalArgumentException("A date range is required.");
        List<TimeSlot> available = new ArrayList<>();
        for (HealthcareProfessional hcp : staffCatalog.findProfessionals(professional, specialty)) {
            available.addAll(hcp.getSchedule().findAvailable(range));
        }
        available.sort(Comparator.comparing(TimeSlot::getDate)
                .thenComparing(TimeSlot::getStartTime)
                .thenComparing(slot -> slot.getProfessional().getFullName()));
        return available;
    }

    public Appointment requestAppointment(Patient patient, HealthcareProfessional professional,
                                          TimeSlot slot, MedicalService service) {
        AuthorizedClinicStaff staff = requireStaff();
        if (patient == null || !patientCatalog.getAll().contains(patient)) {
            throw new IllegalArgumentException("Patient not found. Register the patient before booking.");
        }
        if (professional == null || !staffCatalog.getAll().contains(professional)) {
            throw new IllegalArgumentException("Healthcare professional not found.");
        }
        if (service == null || !serviceCatalog.getAll().contains(service)) {
            throw new IllegalArgumentException("Medical service not found.");
        }
        if (!professional.getSchedule().canBook(slot, service)) {
            throw new IllegalArgumentException("The selected time slot is unavailable or cannot fit the service.");
        }
        Referral referral = referralCatalog.findRequiredReferral(patient, professional, service);
        if (service.requiresReferral() && referral == null) {
            throw new IllegalArgumentException("No confirmed referral matches this patient and specialty.");
        }
        return appointmentCatalog.createAppointment(patient, professional, slot, service, staff, referral);
    }

    private AuthorizedClinicStaff requireStaff() {
        if (!(currentUser instanceof AuthorizedClinicStaff)) {
            throw new IllegalStateException("Log in as clinic staff first.");
        }
        return (AuthorizedClinicStaff) currentUser;
    }

    public int importAvailabilities(Path source) throws IOException {
        return availabilityCsv.importAvailabilities(source);
    }

    public int exportAvailabilities(Path target) throws IOException {
        return availabilityCsv.exportAvailabilities(target);
    }
}
