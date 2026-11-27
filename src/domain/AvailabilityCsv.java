package domain;

import domain.catalog.PatientCatalog;
import domain.catalog.SpecialtyCatalog;
import domain.catalog.StaffCatalog;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

// Imports/exports every professional's time slots as CSV, one slot per line
// Note: new professionals get a temporary password
public class AvailabilityCsv {
    public static final String HEADER = "First Name,Last Name,Title,License Number,Specialty,Date,Start,End,Status";

    private StaffCatalog staffCatalog;
    private PatientCatalog patientCatalog;
    private SpecialtyCatalog specialtyCatalog;
    private String temporaryPassword = "changeit";

    public AvailabilityCsv(StaffCatalog staffCatalog, PatientCatalog patientCatalog,
                           SpecialtyCatalog specialtyCatalog, String temporaryPassword) {
        this.staffCatalog = staffCatalog;
        this.patientCatalog = patientCatalog;
        this.specialtyCatalog = specialtyCatalog;
        this.temporaryPassword = temporaryPassword;
    }

    // Returns the number of slots imported
    public int importAvailabilities(Path source) throws IOException {
        List<String> lines = Files.readAllLines(source);
        int count = 0;
        // line 0 is the header
        for (int i = 1; i < lines.size(); i++) {
            if (!lines.get(i).isBlank()) {
                importLine(lines.get(i));
                count++;
            }
        }
        return count;
    }

    // Returns the number of slots exported
    public int exportAvailabilities(Path target) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add(HEADER);
        for (HealthcareProfessional hcp : staffCatalog.getHealthcareProfessionals()) {
            for (TimeSlot slot : hcp.getSchedule().getTimeSlots()) {
                lines.add(hcp.getFirstName() + "," + hcp.getLastName() + "," + hcp.getTitle() + ","
                        + hcp.getLicenseNumber() + "," + hcp.getSpecialty() + ","
                        + slot.getDate() + "," + slot.getStartTime() + "," + slot.getEndTime() + ","
                        + slot.getStatus());
            }
        }
        Files.write(target, lines);
        return lines.size() - 1;
    }

    // Adds one slot, creating the specialty and professional if they are new
    private void importLine(String line) {
        String[] fields = line.split(",");
        String firstName = fields[0];
        String lastName = fields[1];
        String title = fields[2];
        String licenseNumber = fields[3];
        String specialtyName = fields[4];
        LocalDate date = LocalDate.parse(fields[5]);
        LocalTime start = LocalTime.parse(fields[6]);
        LocalTime end = LocalTime.parse(fields[7]);
        SlotStatus status = SlotStatus.valueOf(fields[8]);

        MedicalSpecialty specialty = specialtyCatalog.findSpecialty(specialtyName);
        if (specialty == null) {
            specialty = new MedicalSpecialty(specialtyName);
            specialtyCatalog.add(specialty);
        }

        HealthcareProfessional hcp = staffCatalog.findProfessionalByLicense(licenseNumber);
        if (hcp == null) {
            // The next id after every staff member and patient, so ids stay unique across users
            int id = 1;
            for (AuthorizedClinicStaff member : staffCatalog.getAll()) {
                id = Math.max(id, member.getId() + 1);
            }
            for (Patient patient : patientCatalog.getAll()) {
                id = Math.max(id, patient.getId() + 1);
            }
            hcp = new HealthcareProfessional(id, temporaryPassword, firstName, lastName, title, licenseNumber, specialty);
            staffCatalog.add(hcp);
        }

        TimeSlot slot = hcp.getSchedule().addTimeSlot(date, start, end);
        slot.setStatus(status);
    }
}
