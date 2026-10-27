package app.repo;

import app.domain.Task;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Order;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

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

    public static Specification<Task> byPriority(Task.Priority priority) {
        return ((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("priority"), priority));
    }

    public static Specification<Task> sortByPriority(boolean isAscending) {
        return ((root, query, criteriaBuilder) -> {
            Expression<?> priorityOrder = criteriaBuilder.selectCase()
                    .when(criteriaBuilder.equal(root.get("priority"), Task.Priority.LOW), 1)
                    .when(criteriaBuilder.equal(root.get("priority"), Task.Priority.MEDIUM), 2)
                    .when(criteriaBuilder.equal(root.get("priority"), Task.Priority.HIGH), 3)
                    .otherwise(4);

            Order primarySort = isAscending ? criteriaBuilder.asc(priorityOrder) : criteriaBuilder.desc(priorityOrder);
            Order tieBreaker = criteriaBuilder.asc(root.get("id"));

            query.orderBy(List.of(primarySort, tieBreaker));

            return criteriaBuilder.conjunction();
        });
    }

    public static Specification<Task> sortByTitleLower(boolean isAscending) {
        return (root, query, criteriaBuilder) -> {
            Order primarySort = isAscending
                    ? criteriaBuilder.asc(criteriaBuilder.lower(root.get("title")))
                    : criteriaBuilder.desc(criteriaBuilder.lower(root.get("title")));
            
            Order tieBreaker = criteriaBuilder.asc(root.get("id"));

            query.orderBy(List.of(primarySort, tieBreaker));

            return criteriaBuilder.conjunction();
        };
    }
}
