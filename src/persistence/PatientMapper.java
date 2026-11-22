package persistence;

import domain.Insurance;
import domain.Patient;
import domain.PrivateInsurance;
import domain.RAMQ;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PatientMapper extends Mapper {

    public PatientMapper(Database database) {
        super(database);
    }

    // The patient keeps the id it was created with (not chosen by SQLite)
    public void insert(Patient patient) throws SQLException {
        String sql = "INSERT INTO Patient (id, password, firstName, lastName, role, dateOfBirth, phoneNum) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, patient.getId());
            ps.setString(2, patient.getPassword());
            ps.setString(3, patient.getFirstName());
            ps.setString(4, patient.getLastName());
            ps.setString(5, patient.getRole().name());
            ps.setString(6, toText(patient.getDateOfBirth()));
            ps.setString(7, patient.getPhoneNum());
            ps.executeUpdate();
        }

        for (Insurance insurance : patient.getInsurances()) {
            String table = insurance instanceof RAMQ ? "RAMQ" : "PrivateInsurance";
            String insuranceSql = "INSERT INTO " + table + " (healthInsuranceNum, patientId) VALUES (?, ?)";
            try (PreparedStatement ps = connection.prepareStatement(insuranceSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, insurance.getHealthInsuranceNum());
                ps.setInt(2, patient.getId());
                insurance.setId(insertAndGetId(ps));
            }
        }
    }

    public List<Patient> findAll() throws SQLException {
        List<Patient> patients = new ArrayList<>();
        String sql = "SELECT id, password, firstName, lastName, dateOfBirth, phoneNum FROM Patient";
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                String dateOfBirth = rs.getString("dateOfBirth");
                patients.add(new Patient(rs.getInt("id"), rs.getString("password"),
                        rs.getString("firstName"), rs.getString("lastName"),
                        dateOfBirth == null ? null : LocalDate.parse(dateOfBirth), rs.getString("phoneNum")));
            }
        }
        loadInsurances(patients, "RAMQ");
        loadInsurances(patients, "PrivateInsurance");
        return patients;
    }

    // The table the row comes from decides which subclass to create
    private void loadInsurances(List<Patient> patients, String table) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT id, healthInsuranceNum, patientId FROM " + table)) {
            while (rs.next()) {
                Insurance insurance;
                if (table.equals("RAMQ")) {
                    insurance = new RAMQ(rs.getString("healthInsuranceNum"));
                } else {
                    insurance = new PrivateInsurance(rs.getString("healthInsuranceNum"));
                }
                insurance.setId(rs.getInt("id"));
                findPatient(patients, rs.getInt("patientId")).addInsurance(insurance);
            }
        }
    }
}
