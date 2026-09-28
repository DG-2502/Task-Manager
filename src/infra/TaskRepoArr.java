package infra;

import domain.Task;
import exception.TaskNotFoundException;
import repo.TaskRepo;

import java.util.ArrayList;
import java.util.List;

public class TaskRepoArr implements TaskRepo {
    protected int ID = -1;
    protected ArrayList<Task> tasks = new ArrayList<>();

    @Override
    public Task getById(int id) {
        for (Task task : tasks) {
            if (task.getID() == id) {
                return task;
            }
        }
        throw new TaskNotFoundException(id);
    }

    @Override
    public void add(Task task) {
        task.setID(++ID);
        tasks.add(task);
    }

    @Override
    public void update(Task updatedTask) {
        Task existingTask = getById(updatedTask.getID());

        existingTask.setActive(updatedTask.isActive());
        existingTask.setTitle(updatedTask.getTitle());
        existingTask.setStartDate(updatedTask.getStartDate());
    }

    @Override
    public void delete(int id) {
        Task task = getById(id);

        tasks.remove(task);
    }

    @Override
    public ArrayList<Task> get() {
        return tasks;
    }

    @Override
    public List<Task> getByState(boolean state) {
        return tasks.stream().filter(task -> task.isActive() == state).toList();
    }

    @Override
    public void deleteByState(boolean state) {
        tasks.removeIf(task -> task.isActive() == state);
    }
}
