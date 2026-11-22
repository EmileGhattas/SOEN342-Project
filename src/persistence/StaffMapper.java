package persistence;

import domain.AdministrativeStaff;
import domain.AuthorizedClinicStaff;
import domain.Clinic;
import domain.ClinicLocation;
import domain.HealthcareProfessional;
import domain.MedicalSpecialty;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

// HealthcareProfessional and AdministrativeStaff each have their own table
public class StaffMapper extends Mapper {

    public StaffMapper(Database database) {
        super(database);
    }

    // Staff keep the id they were created with. A professional's specialty and locations must already be saved
    public void insert(AuthorizedClinicStaff staff) throws SQLException {
        if (!(staff instanceof HealthcareProfessional)) {
            String sql = "INSERT INTO AdministrativeStaff (id, password, firstName, lastName, role) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, staff.getId());
                ps.setString(2, staff.getPassword());
                ps.setString(3, staff.getFirstName());
                ps.setString(4, staff.getLastName());
                ps.setString(5, staff.getRole().name());
                ps.executeUpdate();
            }
            return;
        }

        HealthcareProfessional hcp = (HealthcareProfessional) staff;
        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO Schedule DEFAULT VALUES",
                Statement.RETURN_GENERATED_KEYS)) {
            hcp.getSchedule().setId(insertAndGetId(ps));
        }

        String sql = "INSERT INTO HealthcareProfessional (id, password, firstName, lastName, role, licenseNumber, "
                + "title, scheduleId, specialtyId) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, hcp.getId());
            ps.setString(2, hcp.getPassword());
            ps.setString(3, hcp.getFirstName());
            ps.setString(4, hcp.getLastName());
            ps.setString(5, hcp.getRole().name());
            ps.setString(6, hcp.getLicenseNumber());
            ps.setString(7, hcp.getTitle());
            ps.setInt(8, hcp.getSchedule().getId());
            setForeignKey(ps, 9, hcp.getSpecialty() == null ? 0 : hcp.getSpecialty().getId());
            ps.executeUpdate();
        }

        String joinSql = "INSERT INTO ProfessionalLocation (professionalId, locationId) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(joinSql)) {
            for (ClinicLocation location : hcp.getLocations()) {
                ps.setInt(1, hcp.getId());
                ps.setInt(2, location.getId());
                ps.executeUpdate();
            }
        }
    }

    public List<AuthorizedClinicStaff> findAll(List<MedicalSpecialty> specialties, List<Clinic> clinics) throws SQLException {
        List<AuthorizedClinicStaff> staff = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT id, password, firstName, lastName FROM AdministrativeStaff")) {
            while (rs.next()) {
                staff.add(new AdministrativeStaff(rs.getInt("id"), rs.getString("password"),
                        rs.getString("firstName"), rs.getString("lastName")));
            }
        }

        String sql = "SELECT id, password, firstName, lastName, licenseNumber, title, scheduleId, specialtyId " + "FROM HealthcareProfessional";
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                HealthcareProfessional hcp = new HealthcareProfessional(rs.getInt("id"), rs.getString("password"),
                        rs.getString("firstName"), rs.getString("lastName"), rs.getString("title"),
                        rs.getString("licenseNumber"), findSpecialty(specialties, rs.getInt("specialtyId")));
                // The constructor creates the schedule: give it its id from the database
                hcp.getSchedule().setId(rs.getInt("scheduleId"));
                staff.add(hcp);
            }
        }

        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT professionalId, locationId FROM ProfessionalLocation")) {
            while (rs.next()) {
                findProfessional(staff, rs.getInt("professionalId")).addLocation(findLocation(clinics, rs.getInt("locationId")));
            }
        }
        return staff;
    }
}
