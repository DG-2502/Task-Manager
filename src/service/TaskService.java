package service;

import domain.Task;
import domain.User;
import exception.AccessDeniedException;
import exception.TaskNotFoundException;
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

    public void closeTask(int id, User requester) throws TaskNotFoundException {
        Task task = taskRepo.getById(id);

        if (!requester.isAdmin() && task.getCreatorID() != requester.getID()) {
            throw new TaskNotFoundException(id);
        }

        task.setActive(false);
        taskRepo.update(task);
    }

    public void deleteTask(int id, User requester) throws TaskNotFoundException {
        Task task = taskRepo.getById(id);

        if (!requester.isAdmin() && task.getCreatorID() != requester.getID()) {
            throw new TaskNotFoundException(id);
        }

        taskRepo.delete(task.getID());
    }

    public void deleteClosed(User user) {
        taskRepo.deleteByState(false, user.getID());
    }

    public void updateTask(Task updatedTask, User requester) throws TaskNotFoundException {
        Task task = taskRepo.getById(updatedTask.getID());

        if (!requester.isAdmin() && task.getCreatorID() != requester.getID()) {
            throw new TaskNotFoundException(updatedTask.getID());
        }

        taskRepo.update(updatedTask);
    }

    public enum TaskFilter {
        ALL, ACTIVE, CLOSED;
    }

    public List<Task> getTasks(TaskFilter filter, int userID) {
        return switch (filter) {
            case ALL -> taskRepo.getByUser(userID);
            case ACTIVE -> taskRepo.getByStateAndUser(true, userID);
            case CLOSED -> taskRepo.getByStateAndUser(false, userID);
        };
    }

    public void deleteTasksByUserId(int id, User user) throws AccessDeniedException {
        if (!user.isAdmin()) {
            throw new AccessDeniedException("Only admins can do that");
        }
        taskRepo.deleteByUserId(id);
    }

    public Task getById(int id, User user) {
        Task task = taskRepo.getById(id);

        if (!user.isAdmin() && task.getCreatorID() != user.getID()) {
            throw new TaskNotFoundException(id);
        }

        return task;
    }
}
