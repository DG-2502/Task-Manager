package service;

import domain.Task;
import domain.User;
import exception.AccessDeniedException;
import repo.TaskRepo;

import java.time.LocalDate;
import java.util.List;

public class TaskService {
    private TaskRepo taskRepo;

    public TaskService(TaskRepo taskRepo) {
        this.taskRepo = taskRepo;
    }

    public void createTask(String title, User user) {
        taskRepo.add(new Task(title, LocalDate.now(), true, user.getID()));
    }

    public void closeTask(int id, User user) throws AccessDeniedException {
        Task task = taskRepo.getById(id);

        if (!user.isAdmin() && task.getCreatorID() != user.getID()) {
            throw new AccessDeniedException("You are not allowed to close this task.");
        }

        task.setActive(false);
        taskRepo.update(task);
    }

    public void deleteTask(int id, User user) {
        Task task = taskRepo.getById(id);

        if (!user.isAdmin() && task.getCreatorID() != user.getID()) {
            throw new AccessDeniedException("You are not allowed to delete this task.");
        }

        taskRepo.delete(task.getID());
    }

    public void deleteClosed(User user) {
        taskRepo.deleteByState(false, user.getID());
    }

    public List<Task> getTasks(int option, User user) {
        if (option == 1) return taskRepo.getByState(true, user.getID());
        if (option == 2) return taskRepo.getByState(false, user.getID());
        return taskRepo.get(user.getID());
    }
}
