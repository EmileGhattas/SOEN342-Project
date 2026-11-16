package domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Patient extends User {
    private LocalDate dateOfBirth;
    private String phoneNum;
    private List<Insurance> insurances = new ArrayList<>();

    public Patient(int id, String password, String firstName, String lastName, LocalDate dateOfBirth, String phoneNum) {
        super(id, password, firstName, lastName, UserRole.PATIENT);
        this.dateOfBirth = dateOfBirth;
        this.phoneNum = phoneNum;
    }

    public void addInsurance(Insurance insurance) {
        insurances.add(insurance);
    }

    public boolean hasInsuranceNumber(String number) {
        for (Insurance insurance : insurances) {
            if (insurance.getHealthInsuranceNum().equalsIgnoreCase(number)) {
                return true;
            }
        }
        return false;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public List<Insurance> getInsurances() {
        return Collections.unmodifiableList(insurances);
    }

    @Override
    public String toString() {
        return getFullName() + " (" + phoneNum + ")";
    }
}
