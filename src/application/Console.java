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
import java.sql.SQLException;
import domain.ActivityLog;
import java.time.LocalDateTime;
import persistence.ActivityLogMapper;
import persistence.AppointmentMapper;
import persistence.Database;
import persistence.ExternalPhysicianMapper;
import persistence.MedicalServiceMapper;
import persistence.PatientMapper;
import persistence.ReferralMapper;
import persistence.SpecialtyMapper;
import persistence.StaffMapper;
import persistence.TimeSlotMapper;

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
    private Database database;

    public Console(PatientCatalog patientCatalog, StaffCatalog staffCatalog,
                   SpecialtyCatalog specialtyCatalog, ServiceCatalog serviceCatalog,
                   ReferralCatalog referralCatalog, AppointmentCatalog appointmentCatalog,
                   AvailabilityCsv availabilityCsv) {
        this(patientCatalog, staffCatalog, specialtyCatalog, serviceCatalog, referralCatalog, appointmentCatalog,
                availabilityCsv, null);
    }

    // Saves every change to the database
    public Console(PatientCatalog patientCatalog, StaffCatalog staffCatalog,
                   SpecialtyCatalog specialtyCatalog, ServiceCatalog serviceCatalog,
                   ReferralCatalog referralCatalog, AppointmentCatalog appointmentCatalog,
                   AvailabilityCsv availabilityCsv, Database database) {
        this.database = database;
        this.patientCatalog = Objects.requireNonNull(patientCatalog);
        this.staffCatalog = Objects.requireNonNull(staffCatalog);
        this.specialtyCatalog = Objects.requireNonNull(specialtyCatalog);
        this.serviceCatalog = Objects.requireNonNull(serviceCatalog);
        this.referralCatalog = Objects.requireNonNull(referralCatalog);
        this.appointmentCatalog = Objects.requireNonNull(appointmentCatalog);
        this.availabilityCsv = Objects.requireNonNull(availabilityCsv);
    }

    // Ids are unique across staff and patients, so one login works for every role
    public void login(int id, String password) {
        currentUser = null;
        User user = findUser(id);
        if (user == null) throw new IllegalArgumentException("Invalid id or password.");
        user.login(id, password);
        currentUser = user;
        log(currentUser, "Logged in");
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void logout() {
        if (currentUser != null) {
            log(currentUser, "Logged out");
        }
        currentUser = null;
        currentProfessional = null;
    }

    // Anyone can create the first administrator; after that only administrative staff can add staff
    public AdministrativeStaff registerStaff(int id, String password, String firstName, String lastName) {
        if (hasAdministrator()) {
            requireAdmin();
        }
        requireId(id);
        if (findUser(id) != null) {
            throw new IllegalArgumentException("A user already has that id.");
        }
        requiredText(password, "Password");
        AdministrativeStaff staff = new AdministrativeStaff(id, password,
                requiredText(firstName, "First name"), requiredText(lastName, "Last name"));
        staffCatalog.add(staff);
        if (database != null) {
            try {
                database.begin();
                new StaffMapper(database).insert(staff);
                log(currentUser == null ? staff : currentUser, "Added staff " + staff.getId() + " (" + staff.getFullName() + ")");
                database.commit();
            } catch (SQLException e) {
                throw saveFailed(e);
            }
        }
        return staff;
    }

    public Patient registerPatient(int id, String password, String firstName, String lastName,
                                   LocalDate birthDate, String phone, Insurance insurance) {
        requireAdmin();
        requireId(id);
        if (findUser(id) != null) {
            throw new IllegalArgumentException("A user already has that id.");
        }
        if (birthDate == null) throw new IllegalArgumentException("Date of birth is required.");
        if (insurance != null) requiredText(insurance.getHealthInsuranceNum(), "Insurance number");
        requiredText(password, "Password");
        Patient patient = new Patient(id, password,
                requiredText(firstName, "First name"), requiredText(lastName, "Last name"),
                birthDate, requiredText(phone, "Phone number"));
        if (insurance != null) patient.addInsurance(insurance);
        patientCatalog.add(patient);
        if (database != null) {
            try {
                database.begin();
                new PatientMapper(database).insert(patient);
                log(currentUser, "Registered patient " + patient.getId() + " (" + patient.getFullName() + ")");
                database.commit();
            } catch (SQLException e) {
                throw saveFailed(e);
            }
        }
        return patient;
    }

    public MedicalService registerService(String name, int duration, boolean requiresReferral) {
        requireAdmin();
        name = requiredText(name, "Service name");
        if (serviceCatalog.findService(name) != null) {
            throw new IllegalArgumentException("A service already has that name.");
        }
        MedicalService service = new MedicalService(name, duration, requiresReferral);
        serviceCatalog.add(service);
        if (database != null) {
            try {
                database.begin();
                new MedicalServiceMapper(database).insert(service);
                log(currentUser, "Added service " + service.getName());
                database.commit();
            } catch (SQLException e) {
                throw saveFailed(e);
            }
        }
        return service;
    }

    // Record an existing external referral; this does not send or issue a referral.
    public Referral recordReferral(Patient patient, MedicalSpecialty specialty, ExternalPhysician physician,
                                   String reason, ReferralPriority priority, ReferralType type, boolean confirmed) {
        requireAdmin();
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
                patient, specialty, null, physician);
        if (confirmed) referral.setStatus(ReferralStatus.CONFIRMED);
        referralCatalog.add(referral);
        if (database != null) {
            try {
                database.begin();
                new ExternalPhysicianMapper(database).insert(physician);
                new ReferralMapper(database).insert(referral);
                log(currentUser, "Recorded referral " + referral.getId() + " for patient " + patient.getId()+ " to " + specialty.getName());
                database.commit();
            } catch (SQLException e) {
                throw saveFailed(e);
            }
        }
        return referral;
    }

    public List<MedicalService> getServices() {
        requireLogin();
        return serviceCatalog.getAll();
    }

    public MedicalSpecialty findSpecialty(String name) {
        requireLogin();
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

    // Viewing a patient's record is logged to protect confidentiality
    public Patient searchPatient(String insuranceNumber) {
        requireStaff();
        Patient patient = patientCatalog.findPatient(insuranceNumber);
        if (patient != null) {
            log(currentUser, "Looked up patient " + patient.getId());
        }
        return patient;
    }

    public Patient searchPatient(int id) {
        requireStaff();
        Patient patient = patientCatalog.findPatient(id);
        if (patient != null) {
            log(currentUser, "Looked up patient " + patient.getId());
        }
        return patient;
    }

    public List<TimeSlot> searchAvailability(String professional, MedicalSpecialty specialty, DateRange range) {
        requireLogin();
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
        requireLogin();
        // A patient can only request an appointment for themselves
        if (currentUser instanceof Patient && patient != currentUser) {
            throw new IllegalStateException("Patients can only request appointments for themselves.");
        }
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
        Appointment appointment = appointmentCatalog.createAppointment(patient, professional, slot, service, referral);
        if (database != null) {
            try {
                database.begin();
                new TimeSlotMapper(database).update(slot);
                new AppointmentMapper(database).insert(appointment);
                log(currentUser, "Booked appointment " + appointment.getId() + " for patient " + patient.getId()
                        + " with " + professional.getFullName() + " on " + slot);
                database.commit();
            } catch (SQLException e) {
                throw saveFailed(e);
            }
        }
        return appointment;
    }

    // Make availability step 1: Choose who's schedule to add to
    public HealthcareProfessional selectProfessional(String licenseNumber){
        requireAdmin();
        HealthcareProfessional hcp = staffCatalog.findProfessionalByLicense(requiredText(licenseNumber, "License number"));
        if (hcp == null) {
            throw new IllegalArgumentException("No healthcare professional with license number " + licenseNumber + ".");
        }
        currentProfessional = hcp;
        return hcp;
    }

    // Make availability step 2: return time slots created
    public List<TimeSlot> makeAvailability(DateRange dateRange, RecurrencePattern pattern, LocalTime startTime, LocalTime endTime, Duration slotLength) {
        requireAdmin();
        if (currentProfessional == null) {
            throw new IllegalStateException("Select a healthcare professional first");
        }
        List<TimeSlot> created = currentProfessional.getSchedule().makeAvailability(dateRange, pattern, startTime, endTime, slotLength);
        if (database != null) {
            try {
                database.begin();
                TimeSlotMapper slotMapper = new TimeSlotMapper(database);
                for (TimeSlot slot : created) {
                    slotMapper.insert(slot);
                }
                log(currentUser, "Made " + created.size() + " time slot(s) available for "+ currentProfessional.getFullName());
                database.commit();
            } catch (SQLException e) {
                throw saveFailed(e);
            }
        }
        return created;
    }

    // Use cases open to every role
    private void requireLogin() {
        if (currentUser == null) {
            throw new IllegalStateException("Log in first.");
        }
    }

    // Use cases of the Authorized Clinic Staff actor
    private void requireStaff() {
        if (!(currentUser instanceof AuthorizedClinicStaff)) {
            throw new IllegalStateException("Log in as clinic staff first.");
        }
    }

    // Use cases of the Administrative Staff actor
    private void requireAdmin() {
        if (!(currentUser instanceof AdministrativeStaff)) {
            throw new IllegalStateException("Log in as administrative staff first.");
        }
    }

    // The staff member or patient with that id, or null
    private User findUser(int id) {
        User user = staffCatalog.findStaff(id);
        if (user == null) {
            user = patientCatalog.findPatient(id);
        }
        return user;
    }

    public boolean hasAdministrator() {
        for (AuthorizedClinicStaff member : staffCatalog.getAll()) {
            if (member instanceof AdministrativeStaff) {
                return true;
            }
        }
        return false;
    }

    public int importAvailabilities(Path source) throws IOException {
        requireAdmin();
        try {
            return availabilityCsv.importAvailabilities(source);
        } finally {
            saveImported(source);
        }
    }

    // The import creates specialties, professionals and slots. The ones not saved yet have no id
    private void saveImported(Path source) {
        if (database == null) {
            return;
        }
        try {
            database.begin();
            SpecialtyMapper specialtyMapper = new SpecialtyMapper(database);
            for (MedicalSpecialty specialty : specialtyCatalog.getAll()) {
                if (specialty.getId() == 0) {
                    specialtyMapper.insert(specialty);
                }
            }
            StaffMapper staffMapper = new StaffMapper(database);
            TimeSlotMapper slotMapper = new TimeSlotMapper(database);
            int saved = 0;
            for (HealthcareProfessional hcp : staffCatalog.getHealthcareProfessionals()) {
                // A schedule id is 0 until it is saved
                if (hcp.getSchedule().getId() == 0) {
                    staffMapper.insert(hcp);
                }
                for (TimeSlot slot : hcp.getSchedule().getTimeSlots()) {
                    if (slot.getId() == 0) {
                        slotMapper.insert(slot);
                        saved++;
                    }
                }
            }
            log(currentUser, "Imported " + saved + " time slot(s) from " + source.getFileName());
            database.commit();
        } catch (SQLException e) {
            throw saveFailed(e);
        }
    }

    // Records who did what for accountability
    private void log(User user, String action) {
        if (database == null) {
            return;
        }
        try {
            new ActivityLogMapper(database).insert(new ActivityLog(user, action, LocalDateTime.now()));
        } catch (SQLException e) {
            throw saveFailed(e);
        }
    }

    // Cancels the unfinished transaction and reports the problem to the user
    private IllegalStateException saveFailed(SQLException e) {
        try {
            database.rollback();
        } catch (SQLException ignored) {
            // Nothing to do
        }
        return new IllegalStateException("Could not save to the database: " + e.getMessage());
    }

    public int exportAvailabilities(Path target) throws IOException {
        requireAdmin();
        return availabilityCsv.exportAvailabilities(target);
    }
}
