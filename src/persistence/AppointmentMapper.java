package persistence;

import domain.Appointment;
import domain.AppointmentStatus;
import domain.AuthorizedClinicStaff;
import domain.MedicalService;
import domain.Patient;
import domain.Referral;
import domain.TimeSlot;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class AppointmentMapper extends Mapper {

    public AppointmentMapper(Database database) {
        super(database);
    }

    // The time slot, service, patient, professional and referral must already be saved
    public void insert(Appointment appointment) throws SQLException {
        String sql = "INSERT INTO Appointment (reason, status, arrivalTime, additionalNotes, timeSlotId, serviceId, "
                + "patientId, professionalId, referralId) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, appointment.getReason());
            ps.setString(2, appointment.getStatus().name());
            ps.setString(3, toText(appointment.getArrivalTime()));
            ps.setString(4, appointment.getAdditionalNotes());
            ps.setInt(5, appointment.getTimeSlot().getId());
            ps.setInt(6, appointment.getMedicalService().getId());
            ps.setInt(7, appointment.getPatient().getId());
            ps.setInt(8, appointment.getProfessional().getId());
            setForeignKey(ps, 9, appointment.getReferral() == null ? 0 : appointment.getReferral().getId());
            appointment.setId(insertAndGetId(ps));
        }
    }

    // Arrival, completion and cancelling change these columns
    public void update(Appointment appointment) throws SQLException {
        String sql = "UPDATE Appointment SET status = ?, arrivalTime = ?, additionalNotes = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, appointment.getStatus().name());
            ps.setString(2, toText(appointment.getArrivalTime()));
            ps.setString(3, appointment.getAdditionalNotes());
            ps.setInt(4, appointment.getId());
            ps.executeUpdate();
        }
    }

    public List<Appointment> findAll(List<Patient> patients, List<AuthorizedClinicStaff> staff, List<TimeSlot> slots,
                                     List<MedicalService> services, List<Referral> referrals) throws SQLException {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT id, reason, status, arrivalTime, additionalNotes, timeSlotId, serviceId, patientId, "
                + "professionalId, referralId FROM Appointment";
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                Appointment appointment = new Appointment(findPatient(patients, rs.getInt("patientId")),
                        findProfessional(staff, rs.getInt("professionalId")), findSlot(slots, rs.getInt("timeSlotId")),
                        findService(services, rs.getInt("serviceId")), rs.getString("reason"));
                appointment.setId(rs.getInt("id"));
                appointment.setReferral(findReferral(referrals, rs.getInt("referralId")));
                appointment.setStatus(AppointmentStatus.valueOf(rs.getString("status")));
                String arrival = rs.getString("arrivalTime");
                appointment.setArrivalTime(arrival == null ? null : LocalTime.parse(arrival));
                appointment.setAdditionalNotes(rs.getString("additionalNotes"));
                appointments.add(appointment);
            }
        }
        return appointments;
    }
}
