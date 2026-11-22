package domain;

import java.time.LocalDateTime;

// One action done by a user, e.g. "Booked appointment"
public class ActivityLog {
    private int id;
    private User user;
    private String action;
    private LocalDateTime timestamp;

    public ActivityLog(User user, String action, LocalDateTime timestamp) {
        this.user = user;
        this.action = action;
        this.timestamp = timestamp;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    public int getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getAction() {
        return action;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
