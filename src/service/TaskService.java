package service;

import domain.Task;
import repo.TaskRepo;

public class TaskService {
    private TaskRepo taskRepo;

    public TaskService(TaskRepo taskRepo) {
        this.taskRepo = taskRepo;
    }

    public void createTask(Task task) {
        taskRepo.add(task);
    }

    public void closeTask(int id) {
        Task task = taskRepo.getById(id);
        task.setState(false);
        taskRepo.update(task);
    }

    public void deleteClosed() {
        taskRepo.deleteByState(false);
    }
}
