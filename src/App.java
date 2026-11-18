import application.Console;
import domain.AvailabilityCsv;
import domain.catalog.SpecialtyCatalog;
import domain.catalog.StaffCatalog;
import domain.catalog.PatientCatalog;
import domain.catalog.ServiceCatalog;
import domain.catalog.ReferralCatalog;
import domain.catalog.AppointmentCatalog;
import ui.CommandLine;

public class App {
    public static void main(String[] args) {
        // The system starts fresh: empty catalogs until availabilities are imported
        StaffCatalog staffCatalog = new StaffCatalog();
        SpecialtyCatalog specialtyCatalog = new SpecialtyCatalog();
        AvailabilityCsv availabilityCsv = new AvailabilityCsv(staffCatalog, specialtyCatalog, "changeit");
        Console console = new Console(new PatientCatalog(), staffCatalog, specialtyCatalog,
                new ServiceCatalog(), new ReferralCatalog(), new AppointmentCatalog(), availabilityCsv);
        new CommandLine(console).run();
    }
}
