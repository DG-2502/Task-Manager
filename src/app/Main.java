import console.AppConsole;
import app.BootStrapConsole;
import domain.Task;
import domain.User;
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

    TaskService taskService = new TaskService(taskRepo, userRepo);
    UserService userService = new UserService(userRepo, passwordHasher, taskRepo);

    if (userService.isEmpty()) {
        System.out.println("Bootstrapping the user's repository");
        BootStrapConsole console = new BootStrapConsole();
        while (true) {
            try {
                userService.register(console.getUsername(), console.getPassword(), User.Status.ADMIN);
                break;
            } catch (ValidationException e) {
                System.out.println(e.getMessage());
            }
        }
        System.out.println("GENERATED THE ROOT ADMIN USER");
    }

    AppConsole app = new AppConsole(taskService, userService);
    app.run();
}
