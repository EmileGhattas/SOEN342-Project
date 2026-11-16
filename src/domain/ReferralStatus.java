package domain;

// Only a CONFIRMED referral can be used to book an appointment
public enum ReferralStatus {
    REQUESTED,
    CONFIRMED,
    COMPLETED
}
