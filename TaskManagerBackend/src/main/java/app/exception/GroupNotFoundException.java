package app.exception;

public class GroupNotFoundException extends RuntimeException {
    public GroupNotFoundException(int ID) {
      super("Group with ID: " + ID + " was not found!");
    }
}
