package persistence;

import domain.Appointment;
import domain.AuthorizedClinicStaff;
import domain.Clinic;
import domain.ClinicLocation;
import domain.Consultation;
import domain.ExternalPhysician;
import domain.HealthcareProfessional;
import domain.MedicalService;
import domain.MedicalSpecialty;
import domain.Patient;
import domain.Referral;
import domain.Resource;
import domain.TimeSlot;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

public abstract class Mapper {
    protected Connection connection;

    protected Mapper(Database database) {
        this.connection = database.getConnection();
    }

    protected int insertAndGetId(PreparedStatement ps) throws SQLException {
        ps.executeUpdate();
        try (ResultSet keys = ps.getGeneratedKeys()) {
            keys.next();
            return keys.getInt(1);
        }
    }

    // An id of 0 means no link and is saved as NULL
    protected void setForeignKey(PreparedStatement ps, int index, int id) throws SQLException {
        if (id == 0) {
            ps.setNull(index, Types.INTEGER);
        } else {
            ps.setInt(index, id);
        }
    }

    // Dates and times are saved as text like "2026-12-12" or "09:00". null stays null
    protected String toText(Object value) {
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    protected Patient findPatient(List<Patient> patients, int id) {
        for (Patient patient : patients) {
            if (patient.getId() == id) {
                return patient;
            }
        }
        return null;
    }

    protected HealthcareProfessional findProfessional(List<AuthorizedClinicStaff> staff, int id) {
        for (AuthorizedClinicStaff member : staff) {
            if (member instanceof HealthcareProfessional && member.getId() == id) {
                return (HealthcareProfessional) member;
            }
        }
        return null;
    }

    protected MedicalSpecialty findSpecialty(List<MedicalSpecialty> specialties, int id) {
        for (MedicalSpecialty specialty : specialties) {
            if (specialty.getId() == id) {
                return specialty;
            }
        }
        return null;
    }

    protected ClinicLocation findLocation(List<Clinic> clinics, int id) {
        for (Clinic clinic : clinics) {
            for (ClinicLocation location : clinic.getLocations()) {
                if (location.getId() == id) {
                    return location;
                }
            }
        }
        return null;
    }

    protected MedicalService findService(List<MedicalService> services, int id) {
        for (MedicalService service : services) {
            if (service.getId() == id) {
                return service;
            }
        }
        return null;
    }

    protected Resource findResource(List<Resource> resources, int id) {
        for (Resource resource : resources) {
            if (resource.getId() == id) {
                return resource;
            }
        }
        return null;
    }

    protected ExternalPhysician findPhysician(List<ExternalPhysician> physicians, int id) {
        for (ExternalPhysician physician : physicians) {
            if (physician.getId() == id) {
                return physician;
            }
        }
        return null;
    }

    protected TimeSlot findSlot(List<TimeSlot> slots, int id) {
        for (TimeSlot slot : slots) {
            if (slot.getId() == id) {
                return slot;
            }
        }
        return null;
    }

    protected Referral findReferral(List<Referral> referrals, int id) {
        for (Referral referral : referrals) {
            if (referral.getId() == id) {
                return referral;
            }
        }
        return null;
    }

    protected Appointment findAppointment(List<Appointment> appointments, int id) {
        for (Appointment appointment : appointments) {
            if (appointment.getId() == id) {
                return appointment;
            }
        }
        return null;
    }

    protected Consultation findConsultation(List<Consultation> consultations, int id) {
        for (Consultation consultation : consultations) {
            if (consultation.getId() == id) {
                return consultation;
            }
        }
        return null;
    }
}
