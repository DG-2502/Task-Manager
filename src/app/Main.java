import console.AppConsole;
import console.Console;
import infra.TaskRepoArr;
import infra.TaskRepoFile;
import infra.UserRepoFile;
import repo.TaskRepo;
import repo.UserRepo;
import service.TaskService;
import service.UserService;

void main() throws FileNotFoundException {
    TaskRepo taskRepo = new TaskRepoArr();
    taskRepo = new TaskRepoFile();
//    TaskService taskService = new TaskService(taskRepo);
//
//    taskService.createTask(new Task("NEW TASK", LocalDate.now()));
//
//    System.out.println(taskRepo.get());
//
//    taskService.createTask(new Task("Another task", LocalDate.now()));
//
//    System.out.println(taskRepo.get());
//
//    taskService.closeTask(taskRepo.get().getFirst().getID());
//
//    System.out.println(taskRepo.get());
//
//    taskService.deleteClosed();
//
//    System.out.println(taskRepo.get());

    UserRepo userRepo = new UserRepoFile();
    TaskService taskService = new TaskService(taskRepo);
    UserService userService = new UserService(userRepo);

    Console app = new AppConsole(taskService, userService);
    while (!app.getExitOption()) {
        app.run();
    }
}
