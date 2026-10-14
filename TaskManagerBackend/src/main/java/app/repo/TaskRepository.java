package app.repo;

import app.domain.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TaskRepository extends JpaRepository<Task, Integer>, JpaSpecificationExecutor<Task> {
    void deleteByCreatorId(int creatorId);
    void deleteByCreatorIdAndState(int creatorId, Task.State state);
}
