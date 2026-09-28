import domain.Task;
import infra.TaskRepoArr;
import infra.TaskRepoFile;
import repo.TaskRepo;
import service.TaskService;

void main() throws FileNotFoundException {
    TaskRepo taskRepo = new TaskRepoArr();
    taskRepo = new TaskRepoFile();
    TaskService taskService = new TaskService(taskRepo);

    taskService.createTask(new Task("NEW TASK", LocalDate.now()));

    System.out.println(taskRepo.get());

    taskService.createTask(new Task("Another task", LocalDate.now()));

    System.out.println(taskRepo.get());

    taskService.closeTask(taskRepo.get().getFirst().getID());

    System.out.println(taskRepo.get());

    taskService.deleteClosed();

    System.out.println(taskRepo.get());
}
