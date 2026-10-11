package app.domain;


import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    public enum Status {
        ADMIN, USER
    }

    protected User() {
    }

    public User(String username, Status status, String passwordHash) {
        this.username = username;
        this.status = status;
        this.passwordHash = passwordHash;
    }

    @Override
    public String toString() {
        return id + " " + username + " " + status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
