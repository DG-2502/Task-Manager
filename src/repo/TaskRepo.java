package repo;

import domain.Task;

import java.util.List;

public interface TaskRepo {
    void add(Task task);
    Task getById(int id);
    void update(Task task);
    void delete(int id);
    List<Task> getByUser(int userId);

    List<Task> getByStateAndUser(boolean state, int userId);
    void deleteByState(boolean state, int userId);
    void deleteByUserId(int userId);
}
