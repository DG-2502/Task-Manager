package infra;

import domain.Task;
import exception.TaskNotFoundException;
import repo.TaskRepo;

import java.util.ArrayList;
import java.util.List;

public class TaskRepoArr implements TaskRepo {
    protected int ID = -1;
    protected ArrayList<Task> tasks = new ArrayList<>();

    private Task copy(Task task) {
        Task copy = new Task(task.getTitle(), task.getStartDate(), task.getState(), task.getCreatorID());
        copy.setID(task.getID());
        return copy;
    }

    @Override
    public Task getById(int id) {
        for (Task task : tasks) {
            if (task.getID() == id) return copy(task);
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
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getID() == updatedTask.getID()) {
                tasks.set(i, updatedTask);
                return;
            }
        }
        throw new TaskNotFoundException(updatedTask.getID());
    }

    @Override
    public void delete(int id) {
        tasks.removeIf(task -> task.getID() == id);
    }

    @Override
    public List<Task> getByUser(int userID) {
        return tasks.stream().filter(task -> task.getCreatorID() == userID).map(this::copy).toList();
    }

    @Override
    public List<Task> getByStateAndUser(Task.State state, int userID) {
        return tasks.stream().filter(task -> task.getState() == state && task.getCreatorID() == userID).map(this::copy).toList();
    }

    @Override
    public void deleteByStateAndUser(Task.State state, int userId) {
        tasks.removeIf(task -> task.getState() == state && task.getCreatorID() == userId);
    }

    @Override
    public void deleteByUserId(int userId) {
        tasks.removeIf(task -> task.getCreatorID() == userId);
    }
}
