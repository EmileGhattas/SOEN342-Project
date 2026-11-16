package domain;

public class AdministrativeStaff extends AuthorizedClinicStaff {

    public AdministrativeStaff(int id, String password, String firstName, String lastName) {
        super(id, password, firstName, lastName, UserRole.ADMINISTRATIVE_STAFF);
    }
}
