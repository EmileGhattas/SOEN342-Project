package persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

// Opens the connection to the SQLite database file
public class Database {
    private static final String URL = "jdbc:sqlite:data/appointment_tracking.db";

    private Connection connection;

    public Database() throws SQLException {
        connection = DriverManager.getConnection(URL);
        createTables();
    }

    // Creates any missing table. Existing tables and their rows are kept
    private void createTables() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            // SQLite ignores foreign keys unless this is turned on
            statement.execute("PRAGMA foreign_keys = ON");

            statement.execute("CREATE TABLE IF NOT EXISTS MedicalSpecialty ("
                    + "id INTEGER PRIMARY KEY, "
                    + "name TEXT NOT NULL)");

            statement.execute("CREATE TABLE IF NOT EXISTS Schedule ("
                    + "id INTEGER PRIMARY KEY)");

            statement.execute("CREATE TABLE IF NOT EXISTS HealthcareProfessional ("
                    + "id INTEGER PRIMARY KEY, "
                    + "password TEXT NOT NULL, "
                    + "firstName TEXT NOT NULL, "
                    + "lastName TEXT NOT NULL, "
                    + "role TEXT NOT NULL, "
                    + "licenseNumber TEXT NOT NULL, "
                    + "title TEXT, "
                    + "scheduleId INTEGER REFERENCES Schedule(id), "
                    + "specialtyId INTEGER REFERENCES MedicalSpecialty(id))");

            statement.execute("CREATE TABLE IF NOT EXISTS Clinic ("
                    + "id INTEGER PRIMARY KEY, "
                    + "name TEXT NOT NULL)");

            statement.execute("CREATE TABLE IF NOT EXISTS Patient ("
                    + "id INTEGER PRIMARY KEY, "
                    + "password TEXT NOT NULL, "
                    + "firstName TEXT NOT NULL, "
                    + "lastName TEXT NOT NULL, "
                    + "role TEXT NOT NULL, "
                    + "dateOfBirth TEXT, "
                    + "phoneNum TEXT)");

            statement.execute("CREATE TABLE IF NOT EXISTS AdministrativeStaff ("
                    + "id INTEGER PRIMARY KEY, "
                    + "password TEXT NOT NULL, "
                    + "firstName TEXT NOT NULL, "
                    + "lastName TEXT NOT NULL, "
                    + "role TEXT NOT NULL)");

            statement.execute("CREATE TABLE IF NOT EXISTS ExternalPhysician ("
                    + "id INTEGER PRIMARY KEY, "
                    + "name TEXT NOT NULL, "
                    + "licenseNumber TEXT NOT NULL)");

            statement.execute("CREATE TABLE IF NOT EXISTS RAMQ ("
                    + "id INTEGER PRIMARY KEY, "
                    + "healthInsuranceNum TEXT NOT NULL, "
                    + "patientId INTEGER NOT NULL REFERENCES Patient(id))");

            statement.execute("CREATE TABLE IF NOT EXISTS PrivateInsurance ("
                    + "id INTEGER PRIMARY KEY, "
                    + "healthInsuranceNum TEXT NOT NULL, "
                    + "patientId INTEGER NOT NULL REFERENCES Patient(id))");

            // requiresReferral is 0 if false or 1 if true
            statement.execute("CREATE TABLE IF NOT EXISTS MedicalService ("
                    + "id INTEGER PRIMARY KEY, "
                    + "name TEXT NOT NULL, "
                    + "duration INTEGER NOT NULL, "
                    + "requiresReferral INTEGER NOT NULL)");

            statement.execute("CREATE TABLE IF NOT EXISTS Resource ("
                    + "id INTEGER PRIMARY KEY, "
                    + "type TEXT NOT NULL, "
                    + "name TEXT NOT NULL)");

            statement.execute("CREATE TABLE IF NOT EXISTS MedicalServiceResource ("
                    + "serviceId INTEGER NOT NULL REFERENCES MedicalService(id), "
                    + "resourceId INTEGER NOT NULL REFERENCES Resource(id), "
                    + "PRIMARY KEY (serviceId, resourceId))");

            statement.execute("CREATE TABLE IF NOT EXISTS ClinicLocation ("
                    + "id INTEGER PRIMARY KEY, "
                    + "name TEXT NOT NULL, "
                    + "address TEXT NOT NULL, "
                    + "clinicId INTEGER NOT NULL REFERENCES Clinic(id))");

            statement.execute("CREATE TABLE IF NOT EXISTS ProfessionalLocation ("
                    + "professionalId INTEGER NOT NULL REFERENCES HealthcareProfessional(id), "
                    + "locationId INTEGER NOT NULL REFERENCES ClinicLocation(id), "
                    + "PRIMARY KEY (professionalId, locationId))");

            // Dates are stored as text like '2026-12-12' and times like '09:00'
            statement.execute("CREATE TABLE IF NOT EXISTS TimeSlot ("
                    + "id INTEGER PRIMARY KEY, "
                    + "date TEXT NOT NULL, "
                    + "startTime TEXT NOT NULL, "
                    + "endTime TEXT NOT NULL, "
                    + "status TEXT NOT NULL, "
                    + "scheduleId INTEGER NOT NULL REFERENCES Schedule(id), "
                    + "locationId INTEGER REFERENCES ClinicLocation(id), "
                    + "CHECK (startTime < endTime))");

            statement.execute("CREATE TABLE IF NOT EXISTS ClinicalRecord ("
                    + "id INTEGER PRIMARY KEY, "
                    + "observations TEXT, "
                    + "diagnoses TEXT, "
                    + "treatmentRecommendations TEXT, "
                    + "followUpRequirements TEXT)");

            statement.execute("CREATE TABLE IF NOT EXISTS Appointment ("
                    + "id INTEGER PRIMARY KEY, "
                    + "reason TEXT, "
                    + "status TEXT NOT NULL, "
                    + "arrivalTime TEXT, "
                    + "additionalNotes TEXT, "
                    + "timeSlotId INTEGER NOT NULL REFERENCES TimeSlot(id), "
                    + "serviceId INTEGER NOT NULL REFERENCES MedicalService(id), "
                    + "patientId INTEGER NOT NULL REFERENCES Patient(id), "
                    + "professionalId INTEGER NOT NULL REFERENCES HealthcareProfessional(id), "
                    + "referralId INTEGER REFERENCES Referral(id))");

            statement.execute("CREATE TABLE IF NOT EXISTS Consultation ("
                    + "id INTEGER PRIMARY KEY, "
                    + "date TEXT NOT NULL, "
                    + "appointmentId INTEGER NOT NULL REFERENCES Appointment(id), "
                    + "clinicalRecordId INTEGER REFERENCES ClinicalRecord(id))");

            statement.execute("CREATE TABLE IF NOT EXISTS Referral ("
                    + "id INTEGER PRIMARY KEY, "
                    + "clinicalReason TEXT NOT NULL, "
                    + "priority TEXT NOT NULL, "
                    + "status TEXT NOT NULL, "
                    + "type TEXT NOT NULL, "
                    + "consultationId INTEGER REFERENCES Consultation(id), "
                    + "patientId INTEGER NOT NULL REFERENCES Patient(id), "
                    + "referringProfessionalId INTEGER REFERENCES HealthcareProfessional(id), "
                    + "externalPhysicianId INTEGER REFERENCES ExternalPhysician(id), "
                    + "specialtyId INTEGER REFERENCES MedicalSpecialty(id), "
                    + "requestedProfessionalId INTEGER REFERENCES HealthcareProfessional(id), "
                    + "CHECK (referringProfessionalId IS NOT NULL OR externalPhysicianId IS NOT NULL), "
                    + "CHECK (specialtyId IS NOT NULL OR requestedProfessionalId IS NOT NULL))");

            statement.execute("CREATE TABLE IF NOT EXISTS WaitlistEntry ("
                    + "id INTEGER PRIMARY KEY, "
                    + "priority INTEGER NOT NULL, "
                    + "requestedAt TEXT NOT NULL, "
                    + "startDate TEXT, "
                    + "endDate TEXT, "
                    + "status TEXT NOT NULL, "
                    + "patientId INTEGER NOT NULL REFERENCES Patient(id), "
                    + "professionalId INTEGER REFERENCES HealthcareProfessional(id), "
                    + "specialtyId INTEGER REFERENCES MedicalSpecialty(id), "
                    + "CHECK (priority >= 1), "
                    + "CHECK (professionalId IS NOT NULL OR specialtyId IS NOT NULL OR startDate IS NOT NULL))");

            // A patient and a staff member can share an id, so userRole says which table it is in
            statement.execute("CREATE TABLE IF NOT EXISTS ActivityLog ("
                    + "id INTEGER PRIMARY KEY, "
                    + "userId INTEGER NOT NULL, "
                    + "userRole TEXT NOT NULL, "
                    + "action TEXT NOT NULL, "
                    + "timestamp TEXT NOT NULL)");
        }
    }

    public Connection getConnection() {
        return connection;
    }

    // Groups the changes that follow into one transaction, until commit or rollback
    public void begin() throws SQLException {
        connection.setAutoCommit(false);
    }

    // Saves every change since begin
    public void commit() throws SQLException {
        connection.commit();
        connection.setAutoCommit(true);
    }

    // Cancels every change since begin
    public void rollback() throws SQLException {
        connection.rollback();
        connection.setAutoCommit(true);
    }

    public void close() throws SQLException {
        connection.close();
    }
}
