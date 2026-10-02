import console.AppConsole;
import app.BootStrapConsole;
import console.Console;
import exception.ValidationException;
import infra.PasswordHasherBCrypt;
import infra.TaskRepoFile;
import infra.UserRepoFile;
import repo.*;
import service.*;

void main() throws IOException {
    TaskRepo taskRepo = new TaskRepoFile("data/tasks.txt");
    UserRepo userRepo = new UserRepoFile("data/users.txt");
    PasswordHasher passwordHasher = new PasswordHasherBCrypt();

    TaskService taskService = new TaskService(taskRepo);
    UserService userService = new UserService(userRepo, passwordHasher, taskRepo);

    if (userService.getUsers().isEmpty()) {
        System.out.println("Bootstrapping the user's repository");
        BootStrapConsole console = new BootStrapConsole();
        while (true) {
            try {
                userService.register(console.getUsername(), console.getPassword(), true);
                break;
            } catch (ValidationException e) {
                System.out.println(e.getMessage());
            }
        }
        System.out.println("GENERATED THE ROOT ADMIN USER");
    }


    Console app = new AppConsole(taskService, userService);
    while (!app.getExitOption()) {
        app.run();
    }
}
