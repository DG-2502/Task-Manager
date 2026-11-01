package app.repo;

import app.domain.Group;
import org.springframework.data.jpa.domain.Specification;

public final class GroupSpecifications {
    private GroupSpecifications() {
    }

    public static Specification<Group> byCreatorId(int creatorId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("creatorId"), creatorId);
    }

    public static Specification<Group> titleContains(String search) {
        String pattern = "%" + search.toLowerCase() + "%";
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), pattern);
    }
}
