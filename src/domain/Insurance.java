package domain;

// Health coverage of a patient: either RAMQ or a private insurer
public abstract class Insurance {
    private int id;
    private String healthInsuranceNum;

    protected Insurance(String healthInsuranceNum) {
        this.healthInsuranceNum = healthInsuranceNum;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getHealthInsuranceNum() {
        return healthInsuranceNum;
    }
}
