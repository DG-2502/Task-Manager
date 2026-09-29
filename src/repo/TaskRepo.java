package repo;

import domain.Task;
import domain.User;

import java.util.List;

public interface TaskRepo {
    void add(Task task);
    Task getById(int id);
    void update(Task task);
    void delete(int id);
    List<Task> get(int userId);

    List<Task> getByState(boolean state, int userId);
    void deleteByState(boolean state, int userId);
}
