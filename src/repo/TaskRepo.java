package repo;

import domain.Task;

import java.util.List;

public interface TaskRepo {
    void add(Task task);
    Task getById(int id);
    void update(Task task);
    void delete(int id);
    List<Task> get();

    List<Task> getByState(boolean state);
    void deleteByState(boolean state);
}
