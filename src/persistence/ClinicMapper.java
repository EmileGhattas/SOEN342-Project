package persistence;

import domain.Clinic;
import domain.ClinicLocation;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

// A clinic's locations are saved in the ClinicLocation table
public class ClinicMapper extends Mapper {

    public ClinicMapper(Database database) {
        super(database);
    }

    public void insert(Clinic clinic) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO Clinic (name) VALUES (?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, clinic.getName());
            clinic.setId(insertAndGetId(ps));
        }

        String sql = "INSERT INTO ClinicLocation (name, address, clinicId) VALUES (?, ?, ?)";
        for (ClinicLocation location : clinic.getLocations()) {
            try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, location.getName());
                ps.setString(2, location.getAddress());
                ps.setInt(3, clinic.getId());
                location.setId(insertAndGetId(ps));
            }
        }
    }

    public List<Clinic> findAll() throws SQLException {
        List<Clinic> clinics = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT id, name FROM Clinic")) {
            while (rs.next()) {
                Clinic clinic = new Clinic(rs.getString("name"));
                clinic.setId(rs.getInt("id"));
                clinics.add(clinic);
            }
        }

        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT id, name, address, clinicId FROM ClinicLocation")) {
            while (rs.next()) {
                Clinic clinic = null;
                for (Clinic c : clinics) {
                    if (c.getId() == rs.getInt("clinicId")) {
                        clinic = c;
                    }
                }
                // A location can only be created through its clinic
                ClinicLocation location = clinic.addLocation(rs.getString("name"), rs.getString("address"));
                location.setId(rs.getInt("id"));
            }
        }
        return clinics;
    }
}
