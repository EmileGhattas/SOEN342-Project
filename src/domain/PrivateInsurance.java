package domain;

public class PrivateInsurance extends Insurance {

    public PrivateInsurance(String healthInsuranceNum) {
        super(healthInsuranceNum);
    }

    @Override
    public String toString() {
        return "Private insurance " + getHealthInsuranceNum();
    }
}
