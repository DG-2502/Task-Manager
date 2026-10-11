package app.service;

public interface PasswordHasher {
    String hash(String passwordText);
    boolean matches(String passwordText, String passwordHash);
}
