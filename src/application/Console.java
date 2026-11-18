package application;

import domain.RecurrencePattern;
import domain.AvailabilityCsv;
import domain.Appointment;
import domain.AdministrativeStaff;
import domain.AuthorizedClinicStaff;
import domain.DateRange;
import domain.ExternalPhysician;
import domain.HealthcareProfessional;
import domain.Insurance;
import domain.MedicalService;
import domain.MedicalSpecialty;
import domain.Patient;
import domain.Referral;
import domain.ReferralPriority;
import domain.ReferralStatus;
import domain.ReferralType;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.time.Duration;
import java.time.LocalTime;

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
    private HealthcareProfessional currentProfessional; 

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

    public void logout() {
        currentUser = null;
        currentProfessional = null;
    }

    // Bootstrap the first administrator; later setup requires a staff session.
    public AdministrativeStaff registerStaff(int id, String password, String firstName, String lastName) {
        for (AuthorizedClinicStaff member : staffCatalog.getAll()) {
            if (member instanceof AdministrativeStaff) {
                requireStaff();
                break;
            }
        }
        requireId(id);
        if (staffCatalog.findStaff(id) != null) {
            throw new IllegalArgumentException("A staff member already has that id.");
        }
        requiredText(password, "Password");
        AdministrativeStaff staff = new AdministrativeStaff(id, password,
                requiredText(firstName, "First name"), requiredText(lastName, "Last name"));
        staffCatalog.add(staff);
        return staff;
    }

    public Patient registerPatient(int id, String password, String firstName, String lastName,
                                   LocalDate birthDate, String phone, Insurance insurance) {
        requireStaff();
        requireId(id);
        if (patientCatalog.findPatient(id) != null) {
            throw new IllegalArgumentException("A patient already has that id.");
        }
        if (birthDate == null) throw new IllegalArgumentException("Date of birth is required.");
        if (insurance != null) requiredText(insurance.getHealthInsuranceNum(), "Insurance number");
        requiredText(password, "Password");
        Patient patient = new Patient(id, password,
                requiredText(firstName, "First name"), requiredText(lastName, "Last name"),
                birthDate, requiredText(phone, "Phone number"));
        if (insurance != null) patient.addInsurance(insurance);
        patientCatalog.add(patient);
        return patient;
    }

    public MedicalService registerService(String name, int duration, boolean requiresReferral) {
        requireStaff();
        name = requiredText(name, "Service name");
        if (serviceCatalog.findService(name) != null) {
            throw new IllegalArgumentException("A service already has that name.");
        }
        MedicalService service = new MedicalService(name, duration, requiresReferral);
        serviceCatalog.add(service);
        return service;
    }

    // Record an existing external referral; this does not send or issue a referral.
    public Referral recordReferral(Patient patient, MedicalSpecialty specialty, ExternalPhysician physician,
                                   String reason, ReferralPriority priority, ReferralType type, boolean confirmed) {
        requireStaff();
        if (patient == null || !patientCatalog.getAll().contains(patient)) {
            throw new IllegalArgumentException("Patient not found.");
        }
        if (specialty == null || !specialtyCatalog.getAll().contains(specialty)) {
            throw new IllegalArgumentException("Specialty not found. Import professional availability first.");
        }
        if (physician == null || priority == null || type == null) {
            throw new IllegalArgumentException("Referring physician, priority and type are required.");
        }
        requiredText(physician.getLicenseNumber(), "Referring physician licence");
        requiredText(physician.getName(), "Referring physician name");
        Referral referral = new Referral(requiredText(reason, "Clinical reason"), priority, type,
                patient, specialty, physician);
        if (confirmed) referral.setStatus(ReferralStatus.CONFIRMED);
        referralCatalog.add(referral);
        return referral;
    }

    public List<MedicalService> getServices() {
        requireStaff();
        return serviceCatalog.getAll();
    }

    public MedicalSpecialty findSpecialty(String name) {
        requireStaff();
        MedicalSpecialty specialty = specialtyCatalog.findSpecialty(requiredText(name, "Specialty"));
        if (specialty == null) throw new IllegalArgumentException("Specialty not found: " + name);
        return specialty;
    }

    private static void requireId(int id) {
        if (id <= 0) throw new IllegalArgumentException("Id must be a positive integer.");
    }

    private static String requiredText(String text, String field) {
        if (text == null || text.isBlank()) throw new IllegalArgumentException(field + " is required.");
        return text.trim();
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

    // Make availability step 1: Choose who's schedule to add to
    public HealthcareProfessional selectProfessional(String licenseNumber){
        requireStaff();
        HealthcareProfessional hcp = staffCatalog.findProfessionalByLicense(requiredText(licenseNumber, "License number"));
        if (hcp == null) {
            throw new IllegalArgumentException("No healthcare professional with license number " + licenseNumber + ".");
        }
        currentProfessional = hcp;
        return hcp;
    }

    // Make availability step 2: return time slots created
    public List<TimeSlot> makeAvailability(DateRange dateRange, RecurrencePattern pattern, LocalTime startTime, LocalTime endTime, Duration slotLength) {
        requireStaff();
        if (currentProfessional == null) {
            throw new IllegalStateException("Select a healthcare professional first");
        }
        return currentProfessional.getSchedule().makeAvailability(dateRange, pattern, startTime, endTime, slotLength);
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
