package domain.catalog;

import domain.HealthcareProfessional;
import domain.MedicalService;
import domain.Patient;
import domain.Referral;
import domain.ReferralStatus;
import java.util.ArrayList;
import java.util.List;

public class ReferralCatalog extends Catalog<Referral> {
    private List<Referral> referrals = new ArrayList<>();

    @Override
    protected List<Referral> items() {
        return referrals;
    }

    public Referral findRequiredReferral(Patient patient, HealthcareProfessional hcp, MedicalService service) {
        if (patient == null || hcp == null || service == null) {
            throw new IllegalArgumentException("Patient, professional and service are required.");
        }
        if (!service.requiresReferral()) {
            return null;
        }
        for (Referral referral : referrals) {
            if (referral.getPatient() == patient
                    && referral.getStatus() == ReferralStatus.CONFIRMED
                    && referral.getSpecialty() != null
                    && hcp.getSpecialty() != null
                    && referral.getSpecialty().getName().equalsIgnoreCase(hcp.getSpecialty().getName())) {
                return referral;
            }
        }
        return null;
    }
}
