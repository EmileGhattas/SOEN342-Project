package persistence;

import domain.AuthorizedClinicStaff;
import domain.DateRange;
import domain.MedicalSpecialty;
import domain.Patient;
import domain.WaitlistEntry;
import domain.WaitlistStatus;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// The entry's DateRange is saved as two columns, startDate and endDate
public class WaitlistEntryMapper extends Mapper {

    public WaitlistEntryMapper(Database database) {
        super(database);
    }

    public void insert(WaitlistEntry entry) throws SQLException {
        String sql = "INSERT INTO WaitlistEntry (priority, requestedAt, startDate, endDate, status, patientId, "
                + "professionalId, specialtyId) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, entry.getPriority());
            ps.setString(2, toText(entry.getRequestedAt()));
            ps.setString(3, toText(entry.getDateRange().getStart()));
            ps.setString(4, toText(entry.getDateRange().getEnd()));
            ps.setString(5, entry.getStatus().name());
            ps.setInt(6, entry.getPatient().getId());
            setForeignKey(ps, 7, entry.getProfessional() == null ? 0 : entry.getProfessional().getId());
            setForeignKey(ps, 8, entry.getSpecialty() == null ? 0 : entry.getSpecialty().getId());
            entry.setId(insertAndGetId(ps));
        }
    }

    // Also adds each entry back to its patient
    public List<WaitlistEntry> findAll(List<Patient> patients, List<AuthorizedClinicStaff> staff, List<MedicalSpecialty> specialties) throws SQLException {
        List<WaitlistEntry> entries = new ArrayList<>();
        String sql = "SELECT id, priority, requestedAt, startDate, endDate, status, patientId, professionalId, " + "specialtyId FROM WaitlistEntry";
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                Patient patient = findPatient(patients, rs.getInt("patientId"));
                DateRange range = new DateRange(LocalDate.parse(rs.getString("startDate")),
                        LocalDate.parse(rs.getString("endDate")));
                WaitlistEntry entry = new WaitlistEntry(patient, findProfessional(staff, rs.getInt("professionalId")),
                        findSpecialty(specialties, rs.getInt("specialtyId")), rs.getInt("priority"),
                        LocalDate.parse(rs.getString("requestedAt")), range);
                entry.setId(rs.getInt("id"));
                entry.setStatus(WaitlistStatus.valueOf(rs.getString("status")));
                patient.addWaitlistEntry(entry);
                entries.add(entry);
            }
        }
        return entries;
    }
}
