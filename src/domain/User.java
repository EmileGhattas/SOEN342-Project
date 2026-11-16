package domain;

// Anyone who can log in to the system: a patient or a clinic staff member
public abstract class User {
    private int id;
    private String password;
    private String firstName;
    private String lastName;
    private UserRole role;

    protected User(int id, String password, String firstName, String lastName, UserRole role) {
        this.id = id;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
    }

    public void login(int id, String password) {
        if (this.id != id || !this.password.equals(password)) {
            throw new IllegalArgumentException("Invalid id or password.");
        }
    }

    public int getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public UserRole getRole() {
        return role;
    }

    @Override
    public String toString() {
        return getFullName();
    }
}
