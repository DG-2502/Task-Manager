package console;

import domain.User;
import exception.AuthenticationException;
import exception.ValidationException;
import service.TaskService;
import service.UserService;

public class AppConsole extends BasicConsole {
    private final TaskService taskService;
    private final UserService userService;
    private UserConsole userConsole;

    public AppConsole(TaskService taskService, UserService userService) {
        this.taskService = taskService;
        this.userService = userService;
    }

    @Override
    public void run() {
        System.out.println("Running the main application console\nType help to see the commands");
        while (!exitOption) {
            if (userConsole != null) {
                System.out.println("Type help to see the available commands");
                userConsole.run();
                userConsole = null;
                System.out.println("Exited the user console mode, back to main application console");
            }
            readInput();
        }
    }

    @Override
    public void parseCommand(String command, String query) {
        switch (command) {
            case "login" -> login(query);
            case "register" -> {
                if (register(query)) login(query);
            }
            default -> super.parseCommand(command, query);
        }
    }

    @Override
    protected void printHelp() {
        super.printHelp();
        System.out.println("login username - login in as the specified user");
        System.out.println("register username - register and login");
        System.out.println("exit - close the application");
    }

    private void login(String userName) {
        try {
            System.out.println("Enter the password: ");
            String password = readLine();
            User user = userService.login(userName, password);
            if (user.getStatus() == User.Status.ADMIN) {
                this.userConsole = new AdminConsole(user, taskService, userService);
            } else {
                this.userConsole = new UserConsole(user, taskService, userService);
            }
            System.out.println("Logged in as: " + user);
        } catch (AuthenticationException e) {
            System.out.println(e.getMessage());
        }
    }

    private boolean register(String userName) {
        try {
            System.out.println("Enter the password: ");
            String password = readLine();
            System.out.println("Repeat the password: ");
            String passwordRepeat = readLine();

            if (!password.equals(passwordRepeat)) {
                System.out.println("The passwords are different");
                return false;
            }

            userService.register(userName, password, User.Status.USER);
            System.out.println("Registered successfully");
            return true;
        } catch (ValidationException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }
}
