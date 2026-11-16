package domain;

public class RAMQ extends Insurance {

    public RAMQ(String healthInsuranceNum) {
        super(healthInsuranceNum);
    }

    @Override
    public String toString() {
        return "RAMQ " + getHealthInsuranceNum();
    }
}
