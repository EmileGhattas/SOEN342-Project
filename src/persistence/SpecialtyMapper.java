package persistence;

import domain.MedicalSpecialty;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SpecialtyMapper extends Mapper {

    public SpecialtyMapper(Database database) {
        super(database);
    }

    public void insert(MedicalSpecialty specialty) throws SQLException {
        String sql = "INSERT INTO MedicalSpecialty (name) VALUES (?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, specialty.getName());
            specialty.setId(insertAndGetId(ps));
        }
    }

    public List<MedicalSpecialty> findAll() throws SQLException {
        List<MedicalSpecialty> specialties = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT id, name FROM MedicalSpecialty")) {
            while (rs.next()) {
                MedicalSpecialty specialty = new MedicalSpecialty(rs.getString("name"));
                specialty.setId(rs.getInt("id"));
                specialties.add(specialty);
            }
        }
        return specialties;
    }
}
