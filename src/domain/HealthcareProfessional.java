package domain;

import java.util.ArrayList;
import java.util.List;

public class HealthcareProfessional extends AuthorizedClinicStaff {
    private String title;// e.g. "Dr.", "Nurse Practitioner", null
    private String licenseNumber;
    private MedicalSpecialty specialty;
    private Schedule schedule;
    private List<ClinicLocation> locations = new ArrayList<>();

    public HealthcareProfessional(int id, String password, String firstName, String lastName,
                                  String title, String licenseNumber, MedicalSpecialty specialty) {
        super(id, password, firstName, lastName, UserRole.HEALTHCARE_PROFESSIONAL);
        this.title = title;
        this.licenseNumber = licenseNumber;
        this.specialty = specialty;
        this.schedule = new Schedule(this);
    }

    // A clinic location the professional practices at
    public void addLocation(ClinicLocation location) {
        if (!locations.contains(location)) {
            locations.add(location);
        }
    }

    public List<ClinicLocation> getLocations() {
        return new ArrayList<>(locations);
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public String getTitle() {
        return title;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public MedicalSpecialty getSpecialty() {
        return specialty;
    }

    public boolean hasSpecialty(MedicalSpecialty specialty) {
        return this.specialty.equals(specialty);
    }
}
