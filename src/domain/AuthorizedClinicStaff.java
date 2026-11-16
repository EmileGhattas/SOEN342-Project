package domain;

public abstract class AuthorizedClinicStaff extends User {

    protected AuthorizedClinicStaff(int id, String password, String firstName, String lastName, UserRole role) {
        super(id, password, firstName, lastName, role);
    }

    public String getName() {
        return getFullName();
    }
}
