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
import domain.AppointmentStatus;
import domain.WaitlistEntry;
import domain.catalog.WaitlistCatalog;
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
import persistence.WaitlistEntryMapper;

// Controller: Receives the system operations from the UI
public class Console {
    private PatientCatalog patientCatalog;
    private StaffCatalog staffCatalog;
    private SpecialtyCatalog specialtyCatalog;
    private ServiceCatalog serviceCatalog;
    private ReferralCatalog referralCatalog;
    private AppointmentCatalog appointmentCatalog;
    private AvailabilityCsv availabilityCsv;
    private WaitlistCatalog waitlistCatalog;
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

    public Console(PatientCatalog patientCatalog, StaffCatalog staffCatalog,
                   SpecialtyCatalog specialtyCatalog, ServiceCatalog serviceCatalog,
                   ReferralCatalog referralCatalog, AppointmentCatalog appointmentCatalog,
                   AvailabilityCsv availabilityCsv, Database database) {
        this(patientCatalog, staffCatalog, specialtyCatalog, serviceCatalog, referralCatalog, appointmentCatalog,
                availabilityCsv, new WaitlistCatalog(), database);
    }

    // Saves every change to the database
    public Console(PatientCatalog patientCatalog, StaffCatalog staffCatalog,
                   SpecialtyCatalog specialtyCatalog, ServiceCatalog serviceCatalog,
                   ReferralCatalog referralCatalog, AppointmentCatalog appointmentCatalog,
                   AvailabilityCsv availabilityCsv, WaitlistCatalog waitlistCatalog, Database database) {
        this.database = database;
        this.waitlistCatalog = Objects.requireNonNull(waitlistCatalog);
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
        requireStaff();
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
        requireStaff();
        Appointment appointment = book(patient, professional, slot, service);
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

    // Shared by Request Appointment and Accept Offer: checks every booking rule, then creates the appointment
    private Appointment book(Patient patient, HealthcareProfessional professional, TimeSlot slot, MedicalService service) {
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
        return appointmentCatalog.createAppointment(patient, professional, slot, service, referral);
    }

    // Request Placement on Waitlist
    public WaitlistEntry requestWaitlistPlacement(Patient patient, HealthcareProfessional professional,
                                                  MedicalSpecialty specialty, DateRange dateRange, int priority) {
        requireStaff();
        if (patient == null || !patientCatalog.getAll().contains(patient)) {
            throw new IllegalArgumentException("Patient not found.");
        }
        WaitlistEntry entry = waitlistCatalog.createEntry(patient, professional, specialty, dateRange, priority);
        if (database != null) {
            try {
                database.begin();
                new WaitlistEntryMapper(database).insert(entry);
                log(currentUser, "Waitlisted patient " + patient.getId() + " (entry " + entry.getId() + ")");
                database.commit();
            } catch (SQLException e) {
                throw saveFailed(e);
            }
        }
        return entry;
    }

    // Plain lookup; selectProfessional() would change Make Availability's state
    public HealthcareProfessional findProfessional(String licenseNumber) {
        requireStaff();
        HealthcareProfessional hcp = staffCatalog.findProfessionalByLicense(requiredText(licenseNumber, "License number"));
        if (hcp == null) throw new IllegalArgumentException("No healthcare professional with license number " + licenseNumber + ".");
        return hcp;
    }

    public List<WaitlistEntry> getWaitlist() {
        requireStaff();
        return waitlistCatalog.getWaiting();
    }

    public WaitlistEntry findWaitlistEntry(int id) {
        requireStaff();
        WaitlistEntry entry = waitlistCatalog.findEntry(id);
        if (entry == null) throw new IllegalArgumentException("No waitlist entry with id " + id + ".");
        return entry;
    }

    // Identify Eligible Waitlisted Patients, highest priority first
    public List<WaitlistEntry> identifyEligibleWaitlistedPatients(TimeSlot slot) {
        requireStaff();
        if (slot == null) throw new IllegalArgumentException("A time slot is required.");
        return waitlistCatalog.findEligible(slot);
    }

    // Offer Appointment: returns the entry so staff can phone the patient. Nothing changes yet
    public WaitlistEntry offerAppointment(WaitlistEntry entry, TimeSlot slot) {
        requireAdministrativeStaff();
        requireEligible(entry, slot);
        log(currentUser, "Offered " + slot + " to waitlist entry " + entry.getId());
        return entry;
    }

    // The patient accepted the offer on the phone: book and confirm the appointment
    public Appointment acceptOffer(WaitlistEntry entry, TimeSlot slot, MedicalService service) {
        requireAdministrativeStaff();
        requireEligible(entry, slot);
        Appointment appointment = book(entry.getPatient(), slot.getProfessional(), slot, service);
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        entry.fulfil();
        if (database != null) {
            try {
                database.begin();
                new TimeSlotMapper(database).update(slot);
                new AppointmentMapper(database).insert(appointment);
                new WaitlistEntryMapper(database).update(entry);
                log(currentUser, "Waitlist entry " + entry.getId() + " accepted appointment " + appointment.getId());
                database.commit();
            } catch (SQLException e) {
                throw saveFailed(e);
            }
        }
        return appointment;
    }

    public void removeFromWaitlist(WaitlistEntry entry) {
        requireAdministrativeStaff();
        requireEntry(entry);
        entry.remove();
        saveEntry(entry, "Removed waitlist entry " + entry.getId());
    }

    public WaitlistEntry changeWaitlistPriority(WaitlistEntry entry, int newPriority) {
        requireAdministrativeStaff();
        requireEntry(entry);
        entry.changePriority(newPriority);
        saveEntry(entry, "Changed priority of waitlist entry " + entry.getId() + " to " + newPriority);
        return entry;
    }

    private void requireEntry(WaitlistEntry entry) {
        if (entry == null || !waitlistCatalog.getAll().contains(entry)) {
            throw new IllegalArgumentException("Waitlist entry not found.");
        }
    }

    private void requireEligible(WaitlistEntry entry, TimeSlot slot) {
        requireEntry(entry);
        if (!entry.isEligibleFor(slot)) {
            throw new IllegalArgumentException("This time slot does not match the waitlist entry or is not available.");
        }
    }

    private void saveEntry(WaitlistEntry entry, String action) {
        if (database == null) return;
        try {
            database.begin();
            new WaitlistEntryMapper(database).update(entry);
            log(currentUser, action);
            database.commit();
        } catch (SQLException e) {
            throw saveFailed(e);
        }
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

    private AdministrativeStaff requireAdministrativeStaff() {
        if (!(currentUser instanceof AdministrativeStaff)) {
            throw new IllegalStateException("Log in as administrative staff first.");
        }
        return (AdministrativeStaff) currentUser;
    }

    private AuthorizedClinicStaff requireStaff() {
        if (!(currentUser instanceof AuthorizedClinicStaff)) {
            throw new IllegalStateException("Log in as clinic staff first.");
        }
        return (AuthorizedClinicStaff) currentUser;
    }

    public int importAvailabilities(Path source) throws IOException {
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
        return availabilityCsv.exportAvailabilities(target);
    }
}
