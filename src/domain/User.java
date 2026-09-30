package domain;

public class User {
    private int ID;
    private String username;
    private boolean admin;
    private String passwordHash;

    public User(String username, boolean admin, String passwordHash) {
        this.username = username;
        this.admin = admin;
        this.passwordHash = passwordHash;
    }

    @Override
    public String toString() {
        return ID + " " + username + " " + (admin ? "Admin" : "User");
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

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}
