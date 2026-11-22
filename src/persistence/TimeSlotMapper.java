package persistence;

import domain.AuthorizedClinicStaff;
import domain.Clinic;
import domain.HealthcareProfessional;
import domain.SlotStatus;
import domain.TimeSlot;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class TimeSlotMapper extends Mapper {

    public TimeSlotMapper(Database database) {
        super(database);
    }

    // The professional must already be saved
    public void insert(TimeSlot slot) throws SQLException {
        String sql = "INSERT INTO TimeSlot (date, startTime, endTime, status, scheduleId, locationId) " + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, toText(slot.getDate()));
            ps.setString(2, toText(slot.getStartTime()));
            ps.setString(3, toText(slot.getEndTime()));
            ps.setString(4, slot.getStatus().name());
            ps.setInt(5, slot.getProfessional().getSchedule().getId());
            setForeignKey(ps, 6, slot.getLocation() == null ? 0 : slot.getLocation().getId());
            slot.setId(insertAndGetId(ps));
        }
    }

    // Booking and cancelling change a slot's status
    public void update(TimeSlot slot) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("UPDATE TimeSlot SET status = ? WHERE id = ?")) {
            ps.setString(1, slot.getStatus().name());
            ps.setInt(2, slot.getId());
            ps.executeUpdate();
        }
    }

    // Adds each slot back into its professional's schedule
    public List<TimeSlot> findAll(List<AuthorizedClinicStaff> staff, List<Clinic> clinics) throws SQLException {
        List<TimeSlot> slots = new ArrayList<>();
        String sql = "SELECT id, date, startTime, endTime, status, scheduleId, locationId FROM TimeSlot";
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                HealthcareProfessional hcp = findProfessionalBySchedule(staff, rs.getInt("scheduleId"));
                TimeSlot slot = hcp.getSchedule().addTimeSlot(LocalDate.parse(rs.getString("date")),
                        LocalTime.parse(rs.getString("startTime")), LocalTime.parse(rs.getString("endTime")));
                slot.setId(rs.getInt("id"));

                // A new slot starts AVAILABLE
                if (SlotStatus.valueOf(rs.getString("status")) == SlotStatus.BOOKED) {
                    slot.setStatus(SlotStatus.BOOKED);
                }
                slot.setLocation(findLocation(clinics, rs.getInt("locationId")));
                slots.add(slot);
            }
        }
        return slots;
    }

    private HealthcareProfessional findProfessionalBySchedule(List<AuthorizedClinicStaff> staff, int scheduleId) {
        for (AuthorizedClinicStaff member : staff) {
            if (member instanceof HealthcareProfessional
                    && ((HealthcareProfessional) member).getSchedule().getId() == scheduleId) {
                return (HealthcareProfessional) member;
            }
        }
        return null;
    }
}
