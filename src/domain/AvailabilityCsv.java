package domain;

import domain.catalog.SpecialtyCatalog;
import domain.catalog.StaffCatalog;
import java.io.IOException;
import java.nio.file.Path;

// Imports/exports every professional's time slots as CSV, one slot per line
// Note: new professionals get a temporary password
public class AvailabilityCsv {
    public static final String HEADER = "First Name,Last Name,Title,License Number,Specialty,Date,Start,End,Status";

    private StaffCatalog staffCatalog;
    private SpecialtyCatalog specialtyCatalog;
    private String temporaryPassword = "changeit";

    public AvailabilityCsv(StaffCatalog staffCatalog, SpecialtyCatalog specialtyCatalog, String temporaryPassword) {
        this.staffCatalog = staffCatalog;
        this.specialtyCatalog = specialtyCatalog;
        this.temporaryPassword = temporaryPassword;
    }

    public int importAvailabilities(Path source) throws IOException {
        // TODO (Import from CSV)
        throw new UnsupportedOperationException("TODO: AvailabilityCsv.importAvailabilities");
    }

    public int exportAvailabilities(Path target) throws IOException {
        // TODO (Export to CSV)
        throw new UnsupportedOperationException("TODO: AvailabilityCsv.exportAvailabilities");
    }
}
