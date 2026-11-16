package domain;

// A patient's referral to a specialty, requested by either a clinic professional or an external physician
public class Referral {
    private String clinicalReason;
    private ReferralPriority priority;
    private ReferralStatus status;
    private ReferralType type;
    private Patient patient;
    private MedicalSpecialty specialty;
    private HealthcareProfessional requestedByProfessional;
    private ExternalPhysician requestedByPhysician;

    // Requested by a clinic professional
    public Referral(String clinicalReason, ReferralPriority priority, ReferralType type, Patient patient, MedicalSpecialty specialty, HealthcareProfessional requestedBy) {
        this(clinicalReason, priority, type, patient, specialty, requestedBy, null);
    }

    // Requested by an external physician
    public Referral(String clinicalReason, ReferralPriority priority, ReferralType type, Patient patient, MedicalSpecialty specialty, ExternalPhysician requestedBy) {
        this(clinicalReason, priority, type, patient, specialty, null, requestedBy);
    }

    private Referral(String clinicalReason, ReferralPriority priority, ReferralType type, Patient patient, MedicalSpecialty specialty,
                     HealthcareProfessional requestedByProfessional, ExternalPhysician requestedByPhysician) {
        this.clinicalReason = clinicalReason;
        this.priority = priority;
        this.type = type;
        this.patient = patient;
        this.specialty = specialty;
        this.requestedByProfessional = requestedByProfessional;
        this.requestedByPhysician = requestedByPhysician;
        this.status = ReferralStatus.REQUESTED;
    }

    public void setStatus(ReferralStatus status) {
        this.status = status;
    }

    public String getClinicalReason() {
        return clinicalReason;
    }

    public ReferralPriority getPriority() {
        return priority;
    }

    public ReferralStatus getStatus() {
        return status;
    }

    public ReferralType getType() {
        return type;
    }

    public Patient getPatient() {
        return patient;
    }

    public MedicalSpecialty getSpecialty() {
        return specialty;
    }

    // Null when an external physician requested it
    public HealthcareProfessional getRequestedByProfessional() {
        return requestedByProfessional;
    }

    // Null when a clinic professional requested it
    public ExternalPhysician getRequestedByPhysician() {
        return requestedByPhysician;
    }

    @Override
    public String toString() {
        return type + " referral to " + specialty + " [" + status + "]";
    }
}
