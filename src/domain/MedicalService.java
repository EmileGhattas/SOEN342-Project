package domain;

import java.util.ArrayList;
import java.util.List;

public class MedicalService {
    private int id;
    private String name;
    private int duration; // minutes
    private boolean requiresReferral;
    private List<Resource> resources = new ArrayList<>();

    public MedicalService(String name, int duration, boolean requiresReferral) {
        this.name = name;
        if (duration <= 0) {
            throw new IllegalArgumentException("Duration must be positive.");
        }
        this.duration = duration;
        this.requiresReferral = requiresReferral;
    }

    public void addResource(Resource resource) {
        if (!resources.contains(resource)) {
            resources.add(resource);
        }
    }

    public void setId(int id) {
        this.id = id;
    }
    
    public int getId() {
        return id;
    }

    public List<Resource> getResources() {
        return new ArrayList<>(resources);
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
