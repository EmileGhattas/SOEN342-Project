package persistence;

import domain.Resource;
import domain.ResourceType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ResourceMapper extends Mapper {

    public ResourceMapper(Database database) {
        super(database);
    }

    public void insert(Resource resource) throws SQLException {
        String sql = "INSERT INTO Resource (type, name) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, resource.getType().name());
            ps.setString(2, resource.getName());
            resource.setId(insertAndGetId(ps));
        }
    }

    public List<Resource> findAll() throws SQLException {
        List<Resource> resources = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT id, type, name FROM Resource")) {
            while (rs.next()) {
                Resource resource = new Resource(ResourceType.valueOf(rs.getString("type")), rs.getString("name"));
                resource.setId(rs.getInt("id"));
                resources.add(resource);
            }
        }
        return resources;
    }
}
