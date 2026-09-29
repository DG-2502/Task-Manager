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
        return super.parseCommand(command, option);
    }

    @Override
    public void executeCommands() {
        super.executeCommands();
        if (getHelpOption()) {
            System.out.println("login name/index - login in as the specified user");
            System.out.println("exit - close the application");
            setHelpOption(false);
        }
        if (loginOption) {
            login();
            loginOption = false;
        }
    }

    private void login() {
        try {
            User user = userService.login(userName);
            this.userConsole = new UserConsole(user, taskService);
            System.out.println("Logged in as: " + user);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Could not log in due to the above exception!");
        }
    }
}
