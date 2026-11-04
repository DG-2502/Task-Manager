package app.service;

import app.domain.Group;
import app.domain.User;
import app.exception.AccessDeniedException;
import app.exception.GroupNotFoundException;
import app.exception.UserNotFoundException;
import app.repo.GroupRepository;
import app.repo.GroupSpecifications;
import app.repo.UserRepository;
import app.web.dto.request.group.CreateGroupRequest;
import app.web.dto.request.group.GroupQueryRequest;
import app.web.dto.request.group.UpdateGroupRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class GroupService {
    private final GroupRepository groupRepo;
    private final UserRepository userRepo;

    public GroupService(GroupRepository groupRepo, UserRepository userRepo) {
        this.groupRepo = groupRepo;
        this.userRepo = userRepo;
    }

    public Group createGroup(User requester, int userId, CreateGroupRequest body) {
        verifyAdminAccess(requester, userId, "Only admins can create groups for another user");

        User user = userRepo.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        Group group = new Group(body.title(), user.getId());
        return groupRepo.save(group);
    }

    public Page<Group> getGroups(User requester, int userId, GroupQueryRequest query) {
        verifyAdminAccess(requester, userId, "Only admins can get others' groups");

        Sort.Direction direction = query.direction();

        Sort sort = switch (query.sortBy()) {
            case TITLE -> Sort.by(direction, "title").and(Sort.by("id"));
            case ID -> Sort.by(direction, "id");
        };

        Pageable pageable = PageRequest.of(query.page(), query.size(), sort);

        Specification<Group> spec = Specification.where(GroupSpecifications.byCreatorId(userId));

        if (query.search() != null && !query.search().isBlank())
            spec = spec.and(GroupSpecifications.titleContains(query.search()));

        return groupRepo.findAll(spec, pageable);
    }

    public void deleteGroup(User requester, int groupId) {
        Group group = getGroupOrThrowNotFound(requester, groupId);

        groupRepo.delete(group);
    }

    public void updateGroup(User requester, int groupId, UpdateGroupRequest body) {
        Group group = getGroupOrThrowNotFound(requester, groupId);

        if (body.title() != null) {
            group.setTitle(body.title());
        }

        groupRepo.save(group);
    }

    private Group getGroupOrThrowNotFound(User requester, int groupId) throws GroupNotFoundException {
        Group group = groupRepo.findById(groupId).orElseThrow(() -> new GroupNotFoundException(groupId));

        if (!requester.isAdmin() && group.getCreatorId() != requester.getId()) {
            throw new GroupNotFoundException(groupId);
        }

        return group;
    }

    private void verifyAdminAccess(User requester, int userId, String message) throws AccessDeniedException {
        if (!requester.isAdmin() && userId != requester.getId()) {
            throw new AccessDeniedException(message);
        }
    }
}