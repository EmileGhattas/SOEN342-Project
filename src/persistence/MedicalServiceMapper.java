package persistence;

import domain.MedicalService;
import domain.Resource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

// A service's resources are saved in the MedicalServiceResource join table
public class MedicalServiceMapper extends Mapper {

    public MedicalServiceMapper(Database database) {
        super(database);
    }

    // The service's resources must already be saved
    public void insert(MedicalService service) throws SQLException {
        String sql = "INSERT INTO MedicalService (name, duration, requiresReferral) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, service.getName());
            ps.setInt(2, service.getDuration());
            // true is stored as 1, false as 0
            ps.setInt(3, service.requiresReferral() ? 1 : 0);
            service.setId(insertAndGetId(ps));
        }

        String joinSql = "INSERT INTO MedicalServiceResource (serviceId, resourceId) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(joinSql)) {
            for (Resource resource : service.getResources()) {
                ps.setInt(1, service.getId());
                ps.setInt(2, resource.getId());
                ps.executeUpdate();
            }
        }
    }

    public List<MedicalService> findAll(List<Resource> resources) throws SQLException {
        List<MedicalService> services = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT id, name, duration, requiresReferral FROM MedicalService")) {
            while (rs.next()) {
                MedicalService service = new MedicalService(rs.getString("name"), rs.getInt("duration"),
                        rs.getInt("requiresReferral") == 1);
                service.setId(rs.getInt("id"));
                services.add(service);
            }
        }

        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT serviceId, resourceId FROM MedicalServiceResource")) {
            while (rs.next()) {
                findService(services, rs.getInt("serviceId")).addResource(findResource(resources, rs.getInt("resourceId")));
            }
        }
        return services;
    }
}
