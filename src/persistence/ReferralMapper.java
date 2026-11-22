package persistence;

import domain.AuthorizedClinicStaff;
import domain.Consultation;
import domain.ExternalPhysician;
import domain.HealthcareProfessional;
import domain.MedicalSpecialty;
import domain.Patient;
import domain.Referral;
import domain.ReferralPriority;
import domain.ReferralStatus;
import domain.ReferralType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ReferralMapper extends Mapper {

    public ReferralMapper(Database database) {
        super(database);
    }

    // Every object the referral links to must already be saved
    public void insert(Referral referral) throws SQLException {
        Consultation consultation = referral.getConsultation();
        HealthcareProfessional referringProfessional = referral.getRequestedByProfessional();
        ExternalPhysician physician = referral.getRequestedByPhysician();
        MedicalSpecialty specialty = referral.getSpecialty();
        HealthcareProfessional requestedProfessional = referral.getRequestedProfessional();

        String sql = "INSERT INTO Referral (clinicalReason, priority, status, type, patientId, consultationId, "
                + "referringProfessionalId, externalPhysicianId, specialtyId, requestedProfessionalId) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, referral.getClinicalReason());
            ps.setString(2, referral.getPriority().name());
            ps.setString(3, referral.getStatus().name());
            ps.setString(4, referral.getType().name());
            ps.setInt(5, referral.getPatient().getId());
            setForeignKey(ps, 6, consultation == null ? 0 : consultation.getId());
            setForeignKey(ps, 7, referringProfessional == null ? 0 : referringProfessional.getId());
            setForeignKey(ps, 8, physician == null ? 0 : physician.getId());
            setForeignKey(ps, 9, specialty == null ? 0 : specialty.getId());
            setForeignKey(ps, 10, requestedProfessional == null ? 0 : requestedProfessional.getId());
            referral.setId(insertAndGetId(ps));
        }
    }

    // First pass: the referrals without their consultation which is not loaded yet
    public List<Referral> findAll(List<Patient> patients, List<AuthorizedClinicStaff> staff,
                                  List<ExternalPhysician> physicians, List<MedicalSpecialty> specialties) throws SQLException {
        List<Referral> referrals = new ArrayList<>();
        String sql = "SELECT id, clinicalReason, priority, status, type, patientId, referringProfessionalId, "
                + "externalPhysicianId, specialtyId, requestedProfessionalId FROM Referral";
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                String reason = rs.getString("clinicalReason");
                ReferralPriority priority = ReferralPriority.valueOf(rs.getString("priority"));
                ReferralType type = ReferralType.valueOf(rs.getString("type"));
                Patient patient = findPatient(patients, rs.getInt("patientId"));
                MedicalSpecialty specialty = findSpecialty(specialties, rs.getInt("specialtyId"));

                // The requester column that is filled in decides which constructor to use
                Referral referral;
                HealthcareProfessional referringProfessional = findProfessional(staff, rs.getInt("referringProfessionalId"));
                if (referringProfessional != null) {
                    referral = new Referral(reason, priority, type, patient, specialty, referringProfessional);
                } else {
                    referral = new Referral(reason, priority, type, patient, specialty,
                            findPhysician(physicians, rs.getInt("externalPhysicianId")));
                }
                referral.setId(rs.getInt("id"));
                referral.setStatus(ReferralStatus.valueOf(rs.getString("status")));
                referral.setRequestedProfessional(findProfessional(staff, rs.getInt("requestedProfessionalId")));
                referrals.add(referral);
            }
        }
        return referrals;
    }

    // Second pass, once consultations are loaded
    public void linkConsultations(List<Referral> referrals, List<Consultation> consultations) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT id, consultationId FROM Referral WHERE consultationId IS NOT NULL")) {
            while (rs.next()) {
                findReferral(referrals, rs.getInt("id")).setConsultation(findConsultation(consultations, rs.getInt("consultationId")));
            }
        }
    }
}
