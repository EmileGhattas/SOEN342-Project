package domain;

// What the professional recorded during a consultation
public class ClinicalRecord {
    private int id;
    private String observations;
    private String diagnoses;
    private String treatmentRecommendations;
    private String followUpRequirements;

    public ClinicalRecord(String observations, String diagnoses, String treatmentRecommendations, String followUpRequirements) {
        this.observations = observations;
        this.diagnoses = diagnoses;
        this.treatmentRecommendations = treatmentRecommendations;
        this.followUpRequirements = followUpRequirements;
    }
    
    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getObservations() {
        return observations;
    }

    public String getDiagnoses() {
        return diagnoses;
    }

    public String getTreatmentRecommendations() {
        return treatmentRecommendations;
    }

    public String getFollowUpRequirements() {
        return followUpRequirements;
    }
}
