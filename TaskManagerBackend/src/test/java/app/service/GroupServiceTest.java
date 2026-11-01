package app.service;

import app.domain.Group;
import app.domain.User;
import app.exception.AccessDeniedException;
import app.exception.GroupNotFoundException;
import app.exception.UserNotFoundException;
import app.repo.GroupRepository;
import app.repo.UserRepository;
import app.web.dto.request.group.CreateGroupRequest;
import app.web.dto.request.group.GroupQueryRequest;
import app.web.dto.request.group.UpdateGroupRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GroupServiceTest {
    @Mock
    private GroupRepository groupRepo;

    @Mock
    private UserRepository userRepo;

    @InjectMocks
    private GroupService groupService;

    private User adminUser;
    private User normalUser;
    private User otherUser;
    private Group existingGroup;

    @BeforeEach
    void setUp() {
        adminUser = new User("admin", User.Status.ADMIN, "hash");
        adminUser.setId(1);

        normalUser = new User("user", User.Status.USER, "hash");
        normalUser.setId(2);

        otherUser = new User("other", User.Status.USER, "hash");
        otherUser.setId(3);

        existingGroup = new Group("Existing Group", normalUser.getId());
        existingGroup.setId(10);
    }

    @Test
    void createGroup_ShouldThrowAccessDenied_WhenNonAdminCreatesForAnotherUser() {
        CreateGroupRequest request = new CreateGroupRequest("New Group");

        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () ->
                groupService.createGroup(normalUser, otherUser.getId(), request));

        assertEquals("Only admins can create groups for another user", exception.getMessage());
        verify(groupRepo, never()).save(any(Group.class));
    }

    @Test
    void createGroup_ShouldThrowUserNotFound_WhenTargetUserDoesNotExist() {
        CreateGroupRequest request = new CreateGroupRequest("New Group");
        when(userRepo.findById(anyInt())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () ->
                groupService.createGroup(adminUser, 999, request));

        verify(groupRepo, never()).save(any(Group.class));
    }

    @Test
    void createGroup_ShouldSaveWithCorrectCreatorId_WhenUserCreatesForThemselves() {
        CreateGroupRequest request = new CreateGroupRequest("My Group");
        when(userRepo.findById(normalUser.getId())).thenReturn(Optional.of(normalUser));
        when(groupRepo.save(any(Group.class))).thenAnswer(inv -> inv.getArgument(0));

        Group result = groupService.createGroup(normalUser, normalUser.getId(), request);

        ArgumentCaptor<Group> captor = ArgumentCaptor.forClass(Group.class);
        verify(groupRepo).save(captor.capture());

        Group savedGroup = captor.getValue();
        assertEquals("My Group", savedGroup.getTitle());
        assertEquals(normalUser.getId(), savedGroup.getCreatorId());
        assertEquals(result, savedGroup);
    }

    @Test
    void createGroup_ShouldSaveWithTargetCreatorId_WhenAdminCreatesForAnotherUser() {
        CreateGroupRequest request = new CreateGroupRequest("Admin Made Group");
        when(userRepo.findById(normalUser.getId())).thenReturn(Optional.of(normalUser));
        when(groupRepo.save(any(Group.class))).thenAnswer(inv -> inv.getArgument(0));

        groupService.createGroup(adminUser, normalUser.getId(), request);

        ArgumentCaptor<Group> captor = ArgumentCaptor.forClass(Group.class);
        verify(groupRepo).save(captor.capture());

        assertEquals(normalUser.getId(), captor.getValue().getCreatorId());
    }

    @Test
    void getGroups_ShouldThrowAccessDenied_WhenNonAdminRequestsOthersGroups() {
        GroupQueryRequest query = new GroupQueryRequest(0, 10, null, null, null);

        assertThrows(AccessDeniedException.class, () ->
                groupService.getGroups(normalUser, otherUser.getId(), query));

        verify(groupRepo, never()).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getGroups_ShouldReturnPage_WhenUserRequestsOwnGroups() {
        GroupQueryRequest query = new GroupQueryRequest(0, 10, null, null, null);
        when(groupRepo.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());

        Page<Group> result = groupService.getGroups(normalUser, normalUser.getId(), query);

        assertNotNull(result);
        verify(groupRepo).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getGroups_ShouldReturnPage_WhenAdminRequestsOthersGroups() {
        GroupQueryRequest query = new GroupQueryRequest(0, 10, null, null, null);
        when(groupRepo.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());

        Page<Group> result = groupService.getGroups(adminUser, otherUser.getId(), query);

        assertNotNull(result);
        verify(groupRepo).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getGroups_ShouldApplyAscendingSort_ByDefault() {
        GroupQueryRequest query = new GroupQueryRequest(0, 10, null, null, null);
        when(groupRepo.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());

        groupService.getGroups(normalUser, normalUser.getId(), query);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(groupRepo).findAll(any(Specification.class), captor.capture());

        Sort.Order idOrder = captor.getValue().getSort().getOrderFor("id");
        assertNotNull(idOrder);
        assertEquals(Sort.Direction.ASC, idOrder.getDirection());
    }

    @Test
    void getGroups_ShouldApplyDescendingSort_WhenDirectionIsDesc() {
        GroupQueryRequest query = new GroupQueryRequest(0, 10, null, Sort.Direction.DESC, null);
        when(groupRepo.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());

        groupService.getGroups(normalUser, normalUser.getId(), query);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(groupRepo).findAll(any(Specification.class), captor.capture());

        Sort.Order idOrder = captor.getValue().getSort().getOrderFor("id");
        assertNotNull(idOrder);
        assertEquals(Sort.Direction.DESC, idOrder.getDirection());
    }

    @Test
    void getGroups_ShouldApplySortByTitleWithIdTieBreaker_WhenSortByIsTitle() {
        GroupQueryRequest query = new GroupQueryRequest(
                0, 10, GroupQueryRequest.SortField.TITLE, Sort.Direction.ASC, null);
        when(groupRepo.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());

        groupService.getGroups(normalUser, normalUser.getId(), query);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(groupRepo).findAll(any(Specification.class), captor.capture());

        Sort sort = captor.getValue().getSort();
        Sort.Order titleOrder = sort.getOrderFor("title");
        Sort.Order idOrder = sort.getOrderFor("id");

        assertNotNull(titleOrder);
        assertEquals(Sort.Direction.ASC, titleOrder.getDirection());
        assertNotNull(idOrder);
        assertEquals(Sort.Direction.ASC, idOrder.getDirection());
    }

    @Test
    void getGroups_ShouldRespectPaginationParameters() {
        GroupQueryRequest query = new GroupQueryRequest(2, 25, null, null, null);
        when(groupRepo.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());

        groupService.getGroups(normalUser, normalUser.getId(), query);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(groupRepo).findAll(any(Specification.class), captor.capture());

        assertEquals(2, captor.getValue().getPageNumber());
        assertEquals(25, captor.getValue().getPageSize());
    }

    @Test
    void getGroups_ShouldCallFindAllWithSpecification() {
        GroupQueryRequest query = new GroupQueryRequest(0, 10, null, null, "backend");
        when(groupRepo.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());

        groupService.getGroups(normalUser, normalUser.getId(), query);

        verify(groupRepo).findAll((Specification<Group>) argThat(spec -> spec != null), any(Pageable.class));
    }

    @Test
    void deleteGroup_ShouldThrowGroupNotFound_WhenGroupDoesNotExist() {
        when(groupRepo.findById(999)).thenReturn(Optional.empty());

        assertThrows(GroupNotFoundException.class, () ->
                groupService.deleteGroup(normalUser, 999));

        verify(groupRepo, never()).delete(any(Group.class));
    }

    @Test
    void deleteGroup_ShouldThrowGroupNotFound_WhenNonOwnerTriesToDelete() {
        when(groupRepo.findById(existingGroup.getId())).thenReturn(Optional.of(existingGroup));

        assertThrows(GroupNotFoundException.class, () ->
                groupService.deleteGroup(otherUser, existingGroup.getId()));

        verify(groupRepo, never()).delete(any(Group.class));
    }

    @Test
    void deleteGroup_ShouldDelete_WhenOwnerDeletes() {
        when(groupRepo.findById(existingGroup.getId())).thenReturn(Optional.of(existingGroup));

        groupService.deleteGroup(normalUser, existingGroup.getId());

        verify(groupRepo).delete(existingGroup);
    }

    @Test
    void deleteGroup_ShouldDelete_WhenAdminDeletesAnyGroup() {
        when(groupRepo.findById(existingGroup.getId())).thenReturn(Optional.of(existingGroup));

        groupService.deleteGroup(adminUser, existingGroup.getId());

        verify(groupRepo).delete(existingGroup);
    }

    @Test
    void updateGroup_ShouldThrowGroupNotFound_WhenGroupDoesNotExist() {
        UpdateGroupRequest request = new UpdateGroupRequest("New Title");
        when(groupRepo.findById(999)).thenReturn(Optional.empty());

        assertThrows(GroupNotFoundException.class, () ->
                groupService.updateGroup(normalUser, 999, request));

        verify(groupRepo, never()).save(any(Group.class));
    }

    @Test
    void updateGroup_ShouldThrowGroupNotFound_WhenNonOwnerTriesToUpdate() {
        UpdateGroupRequest request = new UpdateGroupRequest("Hacked Title");
        when(groupRepo.findById(existingGroup.getId())).thenReturn(Optional.of(existingGroup));

        assertThrows(GroupNotFoundException.class, () ->
                groupService.updateGroup(otherUser, existingGroup.getId(), request));

        verify(groupRepo, never()).save(any(Group.class));
    }

    @Test
    void updateGroup_ShouldUpdateTitle_WhenOwnerUpdates() {
        UpdateGroupRequest request = new UpdateGroupRequest("Renamed Group");
        when(groupRepo.findById(existingGroup.getId())).thenReturn(Optional.of(existingGroup));

        groupService.updateGroup(normalUser, existingGroup.getId(), request);

        assertEquals("Renamed Group", existingGroup.getTitle());
        verify(groupRepo).save(existingGroup);
    }

    @Test
    void updateGroup_ShouldUpdateTitle_WhenAdminUpdates() {
        UpdateGroupRequest request = new UpdateGroupRequest("Admin Renamed");
        when(groupRepo.findById(existingGroup.getId())).thenReturn(Optional.of(existingGroup));

        groupService.updateGroup(adminUser, existingGroup.getId(), request);

        assertEquals("Admin Renamed", existingGroup.getTitle());
        verify(groupRepo).save(existingGroup);
    }

    @Test
    void updateGroup_ShouldNotChangeTitle_WhenTitleIsNull() {
        UpdateGroupRequest request = new UpdateGroupRequest(null);
        when(groupRepo.findById(existingGroup.getId())).thenReturn(Optional.of(existingGroup));

        groupService.updateGroup(normalUser, existingGroup.getId(), request);

        assertEquals("Existing Group", existingGroup.getTitle());
        verify(groupRepo).save(existingGroup);
    }

    @Test
    void updateGroup_ShouldSetEmptyTitle_WhenEmptyStringProvided() {
        UpdateGroupRequest request = new UpdateGroupRequest("");
        when(groupRepo.findById(existingGroup.getId())).thenReturn(Optional.of(existingGroup));

        groupService.updateGroup(normalUser, existingGroup.getId(), request);

        assertEquals("", existingGroup.getTitle());
        verify(groupRepo).save(existingGroup);
    }
}