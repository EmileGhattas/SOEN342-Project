package domain;

public class MedicalService {
    private String name;
    private int duration; // minutes
    private boolean requiresReferral;

    public MedicalService(String name, int duration, boolean requiresReferral) {
        this.name = name;
        if (duration <= 0) {
            throw new IllegalArgumentException("Duration must be positive.");
        }
        this.duration = duration;
        this.requiresReferral = requiresReferral;
    }

    public String getName() {
        return name;
    }

    // Duration in minutes
    public int getDuration() {
        return duration;
    }

    public boolean requiresReferral() {
        return requiresReferral;
    }

    @Override
    public String toString() {
        return name + " (" + duration + " min)";
    }
}
