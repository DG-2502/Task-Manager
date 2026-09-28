package console;

import domain.Task;
import domain.User;
import service.TaskService;

import java.util.List;

public class UserConsole extends BasicConsole {
    User user;
    boolean createOption;
    boolean displayOption;

    TaskService taskService;

    public UserConsole(User user, TaskService taskService) {
        this.user = user;
        this.taskService = taskService;
    }

    @Override
    public boolean parseCommand(String command, String query) {
        if (command.equals("create")) {
            return createOption = true;
        }
        if (command.equals("display")) {
            return displayOption = true;
        }
        return super.parseCommand(command, query);
    }

    @Override
    public void executeCommands() {
        super.executeCommands();
        if (getHelpOption()) {
            System.out.println("exit - Log out of the system");
            System.out.println("create - Create a new task");
            System.out.println("display - display tasks");
            setHelpOption(false);
        }
        if (createOption) {
            createTask();
            createOption = false;
            System.out.println("Task was successfully created");
        }
        if (displayOption) {
            displayTasks();
            displayOption = false;
        }
    }

    private void createTask() {
        System.out.println("Create a new task");
        System.out.println("Enter the title of the task");
        String title = readName(true);
        taskService.createTask(title);
    }

    private void displayTasks() {
        List<Task> tasks = taskService.getTasks();
        System.out.println("*** Tasks ***");
        for (Task task : tasks) {
            System.out.println(task);
        }
    }
}
