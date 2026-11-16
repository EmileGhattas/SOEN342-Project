import application.Console;
import domain.AvailabilityCsv;
import domain.catalog.SpecialtyCatalog;
import domain.catalog.StaffCatalog;
import ui.CommandLine;

public class App {
    public static void main(String[] args) {
        // The system starts fresh: empty catalogs until availabilities are imported
        StaffCatalog staffCatalog = new StaffCatalog();
        SpecialtyCatalog specialtyCatalog = new SpecialtyCatalog();
        AvailabilityCsv availabilityCsv = new AvailabilityCsv(staffCatalog, specialtyCatalog, "changeit");
        Console console = new Console(availabilityCsv);
        new CommandLine(console).run();
    }
}
