package app.repo;

import app.domain.Task;
import org.springframework.data.jpa.domain.Specification;

public final class TaskSpecifications {
    private TaskSpecifications() {
    }

    public static Specification<Task> byCreatorId(int creatorId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("creatorId"), creatorId);
    }

    public static Specification<Task> byState(Task.State state) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("state"), state);
    }

    public static Specification<Task> titleContains(String search) {
        String pattern = "%" + search.toLowerCase() + "%";
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), pattern);
    }

    public static  Specification<Task> byPriority(Task.Priority priority) {
        return ((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("priority"), priority));
    }
}
