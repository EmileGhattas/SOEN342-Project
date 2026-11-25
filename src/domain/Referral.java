package domain;

// A patient's referral to a specialty, requested by either a clinic professional or an external physician
public class Referral {
    private int id;
    private String clinicalReason;
    private ReferralPriority priority;
    private ReferralStatus status;
    private ReferralType type;
    private Patient patient;
    private MedicalSpecialty specialty;
    private HealthcareProfessional requestedByProfessional;
    private ExternalPhysician requestedByPhysician;
    private HealthcareProfessional requestedProfessional; // may be null
    private Consultation consultation; // may be null

    // Requested by a clinic professional
    public Referral(String clinicalReason, ReferralPriority priority, ReferralType type, Patient patient, MedicalSpecialty specialty,
                    HealthcareProfessional requestedProfessional, HealthcareProfessional requestedBy) {
        this(clinicalReason, priority, type, patient, specialty, requestedProfessional, requestedBy, null);
    }

    // Requested by an external physician
    public Referral(String clinicalReason, ReferralPriority priority, ReferralType type, Patient patient, MedicalSpecialty specialty,
                    HealthcareProfessional requestedProfessional, ExternalPhysician requestedBy) {
        this(clinicalReason, priority, type, patient, specialty, requestedProfessional, null, requestedBy);
    }

    // Every referral must identify a requested specialty or professional.
    private Referral(String clinicalReason, ReferralPriority priority, ReferralType type, Patient patient, MedicalSpecialty specialty,
                     HealthcareProfessional requestedProfessional, HealthcareProfessional requestedByProfessional, ExternalPhysician requestedByPhysician) {
        this.clinicalReason = clinicalReason;
        this.priority = priority;
        this.type = type;
        this.patient = patient;
        this.specialty = specialty;
        this.requestedProfessional = requestedProfessional;
        this.requestedByProfessional = requestedByProfessional;
        this.requestedByPhysician = requestedByPhysician;
        this.status = ReferralStatus.REQUESTED;
        if (requestedByProfessional == null && requestedByPhysician == null) {
            throw new IllegalArgumentException("A referral must have a referring professional or physician.");
        }
        if (specialty == null && requestedProfessional == null) {
            throw new IllegalArgumentException("A referral must have a specialty or a requested professional.");
        }
    }

    public void setStatus(ReferralStatus status) {
        this.status = status;
    }

    // The consultation the referral came from
    public void setConsultation(Consultation consultation) {
        this.consultation = consultation;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
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

    public HealthcareProfessional getRequestedProfessional() {
        return requestedProfessional;
    }

    public Consultation getConsultation() {
        return consultation;
    }

    @Override
    public String toString() {
        return type + " referral to " + specialty + " [" + status + "]";
    }
}
