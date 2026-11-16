package domain.catalog;

import domain.AuthorizedClinicStaff;
import domain.HealthcareProfessional;
import domain.MedicalSpecialty;
import java.util.ArrayList;
import java.util.List;

public class StaffCatalog extends Catalog<AuthorizedClinicStaff> {
    private List<AuthorizedClinicStaff> staff = new ArrayList<>();

    @Override
    protected List<AuthorizedClinicStaff> items() {
        return staff;
    }

    public List<HealthcareProfessional> findProfessionals(String professional, MedicalSpecialty specialty) {
        // TODO (Search Availability)
        throw new UnsupportedOperationException("TODO: StaffCatalog.findProfessionals");
    }

    // The professional with that license number, or null
    public HealthcareProfessional findProfessionalByLicense(String licenseNumber) {
        for (HealthcareProfessional hcp : getHealthcareProfessionals()) {
            if (licenseNumber.equalsIgnoreCase(hcp.getLicenseNumber())) {
                return hcp;
            }
        }
        return null;
    }

    public List<HealthcareProfessional> getHealthcareProfessionals() {
        List<HealthcareProfessional> professionals = new ArrayList<>();
        for (AuthorizedClinicStaff member : staff) {
            if (member instanceof HealthcareProfessional) {
                professionals.add((HealthcareProfessional) member);
            }
        }
        return professionals;
    }
}
