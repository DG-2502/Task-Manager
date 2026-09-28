package service;

import domain.Task;
import repo.TaskRepo;

import java.time.LocalDate;
import java.util.List;

public class TaskService {
    private TaskRepo taskRepo;

    public TaskService(TaskRepo taskRepo) {
        this.taskRepo = taskRepo;
    }

    public void createTask(String title) {
        taskRepo.add(new Task(title, LocalDate.now(), true));
    }

    public void closeTask(int id) {
        Task task = taskRepo.getById(id);
        task.setActive(false);
        taskRepo.update(task);
    }

    public void deleteClosed() {
        taskRepo.deleteByState(false);
    }

    public List<Task> getTasks() {
        return taskRepo.get();
    }
}
