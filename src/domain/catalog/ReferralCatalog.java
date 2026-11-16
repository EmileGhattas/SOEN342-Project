package domain.catalog;

import domain.HealthcareProfessional;
import domain.MedicalService;
import domain.Patient;
import domain.Referral;
import java.util.ArrayList;
import java.util.List;

public class ReferralCatalog extends Catalog<Referral> {
    private List<Referral> referrals = new ArrayList<>();

    @Override
    protected List<Referral> items() {
        return referrals;
    }

    public Referral findRequiredReferral(Patient patient, HealthcareProfessional hcp, MedicalService service) {
        // TODO (Book Appointment)
        throw new UnsupportedOperationException("TODO: ReferralCatalog.findRequiredReferral");
    }
}
