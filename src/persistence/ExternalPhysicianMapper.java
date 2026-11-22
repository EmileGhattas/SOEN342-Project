package persistence;

import domain.ExternalPhysician;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ExternalPhysicianMapper extends Mapper {

    public ExternalPhysicianMapper(Database database) {
        super(database);
    }

    public void insert(ExternalPhysician physician) throws SQLException {
        String sql = "INSERT INTO ExternalPhysician (name, licenseNumber) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, physician.getName());
            ps.setString(2, physician.getLicenseNumber());
            physician.setId(insertAndGetId(ps));
        }
    }

    public List<ExternalPhysician> findAll() throws SQLException {
        List<ExternalPhysician> physicians = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT id, name, licenseNumber FROM ExternalPhysician")) {
            while (rs.next()) {
                ExternalPhysician physician = new ExternalPhysician(rs.getString("licenseNumber"), rs.getString("name"));
                physician.setId(rs.getInt("id"));
                physicians.add(physician);
            }
        }
        return physicians;
    }
}
