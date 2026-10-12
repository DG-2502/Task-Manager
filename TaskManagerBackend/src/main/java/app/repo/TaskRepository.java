package app.repo;

import app.domain.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Integer> {
    Page<Task> findByCreatorId(int creatorId, Pageable pageable);
    Page<Task> findByCreatorIdAndState(int creatorId, Task.State state, Pageable pageable);

    void deleteByCreatorId(int creatorId);
    void deleteByCreatorIdAndState(int creatorId, Task.State state);
}
