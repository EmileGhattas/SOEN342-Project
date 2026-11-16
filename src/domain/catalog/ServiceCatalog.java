package domain.catalog;

import domain.MedicalService;
import java.util.ArrayList;
import java.util.List;

public class ServiceCatalog extends Catalog<MedicalService> {
    private List<MedicalService> services = new ArrayList<>();

    @Override
    protected List<MedicalService> items() {
        return services;
    }

    // The service with that name (or null)
    public MedicalService findService(String name) {
        for (MedicalService service : services) {
            if (service.getName().equalsIgnoreCase(name)) { //case-insensitive
                return service;
            }
        }
        return null;
    }
}
