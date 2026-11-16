package domain.catalog;

import domain.MedicalSpecialty;
import java.util.ArrayList;
import java.util.List;

public class SpecialtyCatalog extends Catalog<MedicalSpecialty> {
    private List<MedicalSpecialty> specialties = new ArrayList<>();

    @Override
    protected List<MedicalSpecialty> items() {
        return specialties;
    }

    // The specialty with that name (or null)
    public MedicalSpecialty findSpecialty(String name) {
        for (MedicalSpecialty specialty : specialties) {
            if (specialty.getName().equalsIgnoreCase(name)) { //case-insensitive
                return specialty;
            }
        }
        return null;
    }
}
