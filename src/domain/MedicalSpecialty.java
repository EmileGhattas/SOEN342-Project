package domain;

public class MedicalSpecialty {
    private int id;
    private String name;

    public MedicalSpecialty(String name) {
        this.name = name;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
