package domain;

// Health coverage of a patient: either RAMQ or a private insurer
public abstract class Insurance {
    private String healthInsuranceNum;

    protected Insurance(String healthInsuranceNum) {
        this.healthInsuranceNum = healthInsuranceNum;
    }

    public String getHealthInsuranceNum() {
        return healthInsuranceNum;
    }
}
