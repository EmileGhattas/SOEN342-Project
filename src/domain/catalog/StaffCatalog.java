package domain.catalog;

import domain.AuthorizedClinicStaff;
import domain.HealthcareProfessional;
import domain.MedicalSpecialty;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StaffCatalog extends Catalog<AuthorizedClinicStaff> {
    private List<AuthorizedClinicStaff> staff = new ArrayList<>();

    @Override
    protected List<AuthorizedClinicStaff> items() {
        return staff;
    }

    public List<HealthcareProfessional> findProfessionals(String professional, MedicalSpecialty specialty) {
        String query = professional == null ? "" : professional.trim().toLowerCase(Locale.ROOT);
        List<HealthcareProfessional> matches = new ArrayList<>();
        for (HealthcareProfessional hcp : getHealthcareProfessionals()) {
            boolean professionalMatches = query.isEmpty()
                    || hcp.getFullName().toLowerCase(Locale.ROOT).contains(query)
                    || hcp.getLicenseNumber().equalsIgnoreCase(query);
            boolean specialtyMatches = specialty == null || (hcp.getSpecialty() != null
                    && hcp.getSpecialty().getName().equalsIgnoreCase(specialty.getName()));
            if (professionalMatches && specialtyMatches) {
                matches.add(hcp);
            }
        }
        return matches;
    }

    public AuthorizedClinicStaff findStaff(int id) {
        for (AuthorizedClinicStaff member : staff) {
            if (member.getId() == id) return member;
        }
        return null;
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
