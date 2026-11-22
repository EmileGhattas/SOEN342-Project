package domain;

public class ClinicLocation {
    private int id;
    private Clinic clinic;
    private String name;
    private String address;

    // Created by Clinic
    ClinicLocation(Clinic clinic, String name, String address) {
        this.clinic = clinic;
        this.name = name;
        this.address = address;
    }
    
    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public Clinic getClinic() {
        return clinic;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }
}
