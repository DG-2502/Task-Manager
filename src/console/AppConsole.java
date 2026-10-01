package console;

import domain.User;
import service.TaskService;
import service.UserService;

public class AppConsole extends BasicConsole {
    TaskService taskService;
    UserService userService;

    Console userConsole;
    String userName;
    boolean loginOption;
    boolean registerOption;

    public AppConsole(TaskService taskService, UserService userService) {
        this.taskService = taskService;
        this.userService = userService;
    }

    @Override
    public void run() {
        executeCommands();
        if (userConsole != null) {
            while (!userConsole.getExitOption()) {
                userConsole.run();
            }
            userConsole = null;
            setHelpOption(true);
            executeCommands();
        }
        readInput();
    }

    @Override
    public boolean parseCommand(String command, String option) {
        if (command.equals("login")) {
            userName = option;
            return loginOption = true;
        }
        if (command.equals("register")) {
            userName = option;
            return registerOption = true;
        }
        return super.parseCommand(command, option);
    }

    @Override
    public void executeCommands() {
        super.executeCommands();
        if (getHelpOption()) {
            printHelp();
        }
        if (loginOption) {
            login();
            loginOption = false;
        }
        if (registerOption) {
            if (register()) login();
            registerOption = false;
        }
    }

    @Override
    protected void printHelp() {
        super.printHelp();
        System.out.println("login username - login in as the specified user");
        System.out.println("register username - register and login");
        System.out.println("exit - close the application");
    }

    private void login() {
        try {
            System.out.println("Enter the password: ");
            String password = readName(true);
            User user = userService.login(userName, password);
            if (user.isAdmin()) {
                this.userConsole = new AdminConsole(user, taskService, userService);
            } else {
                this.userConsole = new UserConsole(user, taskService);
            }
            System.out.println("Logged in as: " + user);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private boolean register() {
        try {
            System.out.println("Enter the password: ");
            String password = readName(true);
            System.out.println("Repeat the password: ");
            String passwordRepeat = readName(true);

            if (!password.equals(passwordRepeat)) {
                System.out.println("The passwords are different");
                return false;
            }

            userService.register(userName, password, false);
            System.out.println("Registered successfully");
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }
}
