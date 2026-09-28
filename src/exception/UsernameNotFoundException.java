package exception;

public class UsernameNotFoundException extends RuntimeException {
    public UsernameNotFoundException(String username) {
        super("User with username: " + username + " was not found!");
    }
}
