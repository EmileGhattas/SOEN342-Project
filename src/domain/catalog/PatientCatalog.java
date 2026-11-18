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
        if (insuranceNumber == null || insuranceNumber.isBlank()) return null;
        for (Patient patient : patients) {
            if (patient.hasInsuranceNumber(insuranceNumber)) {
                return patient;
            }
        }
        return null;
    }

    public Patient findPatient(int id) {
        for (Patient patient : patients) {
            if (patient.getId() == id) return patient;
        }
        return null;
    }
}
