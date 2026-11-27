import application.Console;
import application.SampleData;
import domain.AuthorizedClinicStaff;
import domain.AvailabilityCsv;
import domain.Clinic;
import domain.MedicalService;
import domain.MedicalSpecialty;
import domain.Patient;
import domain.Referral;
import domain.TimeSlot;
import domain.Appointment;
import domain.Consultation;
import domain.catalog.SpecialtyCatalog;
import domain.catalog.StaffCatalog;
import domain.catalog.PatientCatalog;
import domain.catalog.ServiceCatalog;
import domain.catalog.ReferralCatalog;
import domain.catalog.AppointmentCatalog;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import persistence.AppointmentMapper;
import persistence.ClinicMapper;
import persistence.ConsultationMapper;
import persistence.Database;
import persistence.ExternalPhysicianMapper;
import persistence.MedicalServiceMapper;
import persistence.PatientMapper;
import persistence.ReferralMapper;
import persistence.ResourceMapper;
import persistence.SpecialtyMapper;
import persistence.StaffMapper;
import persistence.TimeSlotMapper;
import persistence.WaitlistEntryMapper;
import ui.CommandLine;

public class App {
    public static void main(String[] args) throws SQLException, IOException {
        Database database = new Database();
        StaffCatalog staffCatalog = new StaffCatalog();
        SpecialtyCatalog specialtyCatalog = new SpecialtyCatalog();
        PatientCatalog patientCatalog = new PatientCatalog();
        ServiceCatalog serviceCatalog = new ServiceCatalog();
        ReferralCatalog referralCatalog = new ReferralCatalog();
        AppointmentCatalog appointmentCatalog = new AppointmentCatalog();

        // Everything saved before is loaded back into the catalogs
        load(database, staffCatalog, specialtyCatalog, patientCatalog, serviceCatalog, referralCatalog, appointmentCatalog);

        AvailabilityCsv availabilityCsv = new AvailabilityCsv(staffCatalog, patientCatalog, specialtyCatalog, "changeit");
        Console console = new Console(patientCatalog, staffCatalog, specialtyCatalog,
                serviceCatalog, referralCatalog, appointmentCatalog, availabilityCsv, database);

        // An empty database gets sample records
        if (staffCatalog.getAll().isEmpty()) {
            SampleData.load(console);
            System.out.println("Sample data loaded. Log in as admin 1/admin, professional 2/changeit or patient 100/pw.");
        }
        new CommandLine(console).run();
        database.close();
    }

    // Each table is loaded after the ones it points to, so every link finds its object
    private static void load(Database database, StaffCatalog staffCatalog, SpecialtyCatalog specialtyCatalog,
                             PatientCatalog patientCatalog, ServiceCatalog serviceCatalog,
                             ReferralCatalog referralCatalog, AppointmentCatalog appointmentCatalog) throws SQLException {
        List<MedicalSpecialty> specialties = new SpecialtyMapper(database).findAll();
        List<MedicalService> services = new MedicalServiceMapper(database).findAll(new ResourceMapper(database).findAll());
        List<Clinic> clinics = new ClinicMapper(database).findAll();
        List<Patient> patients = new PatientMapper(database).findAll();
        List<AuthorizedClinicStaff> staff = new StaffMapper(database).findAll(specialties, clinics);
        List<TimeSlot> slots = new TimeSlotMapper(database).findAll(staff, clinics);
        ReferralMapper referralMapper = new ReferralMapper(database);
        List<Referral> referrals = referralMapper.findAll(patients, staff, new ExternalPhysicianMapper(database).findAll(), specialties);
        List<Appointment> appointments = new AppointmentMapper(database).findAll(patients, staff, slots, services, referrals);
        List<Consultation> consultations = new ConsultationMapper(database).findAll(appointments);
        referralMapper.linkConsultations(referrals, consultations);
        // Adds each entry to its patient
        new WaitlistEntryMapper(database).findAll(patients, staff, specialties);

        for (MedicalSpecialty specialty : specialties) {
            specialtyCatalog.add(specialty);
        }
        for (MedicalService service : services) {
            serviceCatalog.add(service);
        }
        for (Patient patient : patients) {
            patientCatalog.add(patient);
        }
        for (AuthorizedClinicStaff member : staff) {
            staffCatalog.add(member);
        }
        for (Referral referral : referrals) {
            referralCatalog.add(referral);
        }
        for (Appointment appointment : appointments) {
            appointmentCatalog.add(appointment);
        }
    }
}
