package domain;

// A physician outside the clinic who can request referrals. Not a user of the system
public class ExternalPhysician {
    private String licenseNumber;
    private String name;

    public ExternalPhysician(String licenseNumber, String name) {
        this.licenseNumber = licenseNumber;
        this.name = name;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
