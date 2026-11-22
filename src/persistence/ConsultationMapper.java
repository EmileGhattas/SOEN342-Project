package persistence;

import domain.Appointment;
import domain.ClinicalRecord;
import domain.Consultation;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Saves and loads a consultation together with its clinical record
public class ConsultationMapper extends Mapper {

    public ConsultationMapper(Database database) {
        super(database);
    }

    // The appointment must already be saved. The clinical record is saved here first
    public void insert(Consultation consultation) throws SQLException {
        ClinicalRecord record = consultation.getClinicalRecord();
        if (record != null) {
            String recordSql = "INSERT INTO ClinicalRecord (observations, diagnoses, treatmentRecommendations, "
                    + "followUpRequirements) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = connection.prepareStatement(recordSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, record.getObservations());
                ps.setString(2, record.getDiagnoses());
                ps.setString(3, record.getTreatmentRecommendations());
                ps.setString(4, record.getFollowUpRequirements());
                record.setId(insertAndGetId(ps));
            }
        }

        String sql = "INSERT INTO Consultation (date, appointmentId, clinicalRecordId) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, toText(consultation.getDate()));
            ps.setInt(2, consultation.getAppointment().getId());
            setForeignKey(ps, 3, record == null ? 0 : record.getId());
            consultation.setId(insertAndGetId(ps));
        }
    }

    // LEFT JOIN reads each consultation with its record in one query, and keeps consultations that have none
    public List<Consultation> findAll(List<Appointment> appointments) throws SQLException {
        List<Consultation> consultations = new ArrayList<>();
        String sql = "SELECT c.id, c.date, c.appointmentId, c.clinicalRecordId, r.observations, r.diagnoses, "
                + "r.treatmentRecommendations, r.followUpRequirements "
                + "FROM Consultation c LEFT JOIN ClinicalRecord r ON c.clinicalRecordId = r.id";
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                ClinicalRecord record = null;
                if (rs.getInt("clinicalRecordId") != 0) {
                    record = new ClinicalRecord(rs.getString("observations"), rs.getString("diagnoses"),
                            rs.getString("treatmentRecommendations"), rs.getString("followUpRequirements"));
                    record.setId(rs.getInt("clinicalRecordId"));
                }
                Consultation consultation = new Consultation(LocalDate.parse(rs.getString("date")),
                        findAppointment(appointments, rs.getInt("appointmentId")), record);
                consultation.setId(rs.getInt("id"));
                consultations.add(consultation);
            }
        }
        return consultations;
    }
}
