package app.exception;

public class TaskNotFoundException extends RuntimeException {
    public TaskNotFoundException(int ID) {
        super("Task with ID: " + ID + " was not found!");
    }
}
