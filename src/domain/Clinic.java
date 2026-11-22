package domain;

import java.util.ArrayList;
import java.util.List;

public class Clinic {
    private int id;
    private String name;
    private List<ClinicLocation> locations = new ArrayList<>();

    public Clinic(String name) {
        this.name = name;
    }

    public ClinicLocation addLocation(String name, String address) {
        ClinicLocation location = new ClinicLocation(this, name, address);
        locations.add(location);
        return location;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<ClinicLocation> getLocations() {
        return new ArrayList<>(locations);
    }
}
