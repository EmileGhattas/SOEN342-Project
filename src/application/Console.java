package application;

import domain.AvailabilityCsv;
import java.io.IOException;
import java.nio.file.Path;

// Controller: Receives the system operations from the UI
// TODO: add remaining use cases from PoC
public class Console {
    private AvailabilityCsv availabilityCsv;

    public Console(AvailabilityCsv availabilityCsv) {
        this.availabilityCsv = availabilityCsv;
    }

    public int importAvailabilities(Path source) throws IOException {
        return availabilityCsv.importAvailabilities(source);
    }

    public int exportAvailabilities(Path target) throws IOException {
        return availabilityCsv.exportAvailabilities(target);
    }
}
