import console.AppConsole;
import console.Console;
import infra.PasswordHasherBCrypt;
import infra.TaskRepoFile;
import infra.UserRepoFile;
import repo.*;
import service.*;

void main() throws FileNotFoundException {
    TaskRepo taskRepo = new TaskRepoFile("data/tasks.txt");
    UserRepo userRepo = new UserRepoFile("data/users.txt");
    PasswordHasher passwordHasher = new PasswordHasherBCrypt();

    TaskService taskService = new TaskService(taskRepo);
    UserService userService = new UserService(userRepo, passwordHasher, taskRepo);

    Console app = new AppConsole(taskService, userService);
    while (!app.getExitOption()) {
        app.run();
    }
}
