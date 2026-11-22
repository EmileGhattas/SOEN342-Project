package persistence;

import domain.ActivityLog;
import domain.AuthorizedClinicStaff;
import domain.Patient;
import domain.User;
import domain.UserRole;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ActivityLogMapper extends Mapper {

    public ActivityLogMapper(Database database) {
        super(database);
    }

    public void insert(ActivityLog log) throws SQLException {
        String sql = "INSERT INTO ActivityLog (userId, userRole, action, timestamp) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, log.getUser().getId());
            ps.setString(2, log.getUser().getRole().name());
            ps.setString(3, log.getAction());
            ps.setString(4, toText(log.getTimestamp()));
            log.setId(insertAndGetId(ps));
        }
    }

    // Oldest first
    public List<ActivityLog> findAll(List<Patient> patients, List<AuthorizedClinicStaff> staff) throws SQLException {
        List<ActivityLog> logs = new ArrayList<>();
        String sql = "SELECT id, userId, userRole, action, timestamp FROM ActivityLog ORDER BY timestamp";
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                // A patient and a staff member can share an id, so the role says where to look
                User user;
                if (UserRole.valueOf(rs.getString("userRole")) == UserRole.PATIENT) {
                    user = findPatient(patients, rs.getInt("userId"));
                } else {
                    user = findStaff(staff, rs.getInt("userId"));
                }
                ActivityLog log = new ActivityLog(user, rs.getString("action"), LocalDateTime.parse(rs.getString("timestamp")));
                log.setId(rs.getInt("id"));
                logs.add(log);
            }
        }
        return logs;
    }

    private AuthorizedClinicStaff findStaff(List<AuthorizedClinicStaff> staff, int id) {
        for (AuthorizedClinicStaff member : staff) {
            if (member.getId() == id) {
                return member;
            }
        }
        return null;
    }
}
