package domain.catalog;

import domain.Patient;
import java.util.ArrayList;
import java.util.List;

public class PatientCatalog extends Catalog<Patient> {
    private List<Patient> patients = new ArrayList<>();

    @Override
    protected List<Patient> items() {
        return patients;
    }

    // The patient covered by that insurance number or null
    public Patient findPatient(String insuranceNumber) {
        for (Patient patient : patients) {
            if (patient.hasInsuranceNumber(insuranceNumber)) {
                return patient;
            }
        }
        return null;
    }
}
