package domain;

import java.time.LocalDate;

// The visit that took place for an appointment
public class Consultation {
    private int id;
    private LocalDate date;
    private Appointment appointment;
    private ClinicalRecord clinicalRecord;

    public Consultation(LocalDate date, Appointment appointment, ClinicalRecord clinicalRecord) {
        this.date = date;
        this.appointment = appointment;
        this.clinicalRecord = clinicalRecord;
    }
    
    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public Appointment getAppointment() {
        return appointment;
    }

    public ClinicalRecord getClinicalRecord() {
        return clinicalRecord;
    }
}
