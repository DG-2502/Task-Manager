package app.repo;

import app.domain.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Integer> {
    List<Task> findByCreatorId(int creatorId);
    List<Task> findByCreatorIdAndState(int creatorId, Task.State state);

    void deleteByCreatorId(int creatorId);
    void deleteByCreatorIdAndState(int creatorId, Task.State state);
}
