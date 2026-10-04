package domain;

public class User {
    private int ID;
    private String username;
    private Status status;
    private String passwordHash;

    public enum Status {
        ADMIN, USER
    }

    public User(String username, Status status, String passwordHash) {
        this.username = username;
        this.status = status;
        this.passwordHash = passwordHash;
    }

    @Override
    public String toString() {
        return ID + " " + username + " " + status;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public boolean isAdmin() {
        return status == Status.ADMIN;
    }
}
