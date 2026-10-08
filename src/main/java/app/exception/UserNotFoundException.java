package app.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(int ID) {
        super("User with ID: " + ID + " was not found!");
    }
}
