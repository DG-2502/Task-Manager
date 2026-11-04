package app.web.api;

import app.domain.Task;
import app.domain.Group;
import app.domain.User;
import app.service.GroupService;
import app.service.TaskService;
import app.service.UserService;
import app.web.dto.request.group.CreateGroupRequest;
import app.web.dto.request.group.GroupQueryRequest;
import app.web.dto.request.task.CreateTaskRequest;
import app.web.dto.request.task.TaskQueryRequest;
import app.web.dto.request.user.ChangePasswordRequest;
import app.web.dto.request.user.CreateUserRequest;
import app.web.dto.request.user.UpdateUserRequest;
import app.web.dto.request.user.UserQueryRequest;
import app.web.dto.response.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Admin", description = "Endpoints for administrators to manage users and their tasks")
@RequestMapping(value = "/api/admin", produces = MediaType.APPLICATION_JSON_VALUE)
public class AdminApiController {
    private final TaskService taskService;
    private final UserService userService;
    private final GroupService groupService;

    public AdminApiController(TaskService taskService, UserService userService, GroupService groupService) {
        this.taskService = taskService;
        this.userService = userService;
        this.groupService = groupService;
    }

    @Operation(summary = "Get all users", description = "Retrieves a paginated list of all users, with optional filtering by status and search by username.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden")
    })
    @GetMapping("/users")
    public PageResponse<UserResponse> getUsers(@Valid @ModelAttribute UserQueryRequest query,
                                               @AuthenticationPrincipal User admin) {
        Page<User> users = userService.getUsers(admin, query);
        return PageResponse.from(users, UserResponse::from);
    }

    @Operation(summary = "Get tasks of a specific user", description = "Allows an admin to view a paginated list of tasks belonging to any user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully"),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden")
    })
    @GetMapping("/users/{id}/tasks")
    public PageResponse<TaskResponse> getTasks(@PathVariable int id,
                                               @Valid @ModelAttribute TaskQueryRequest query,
                                               @AuthenticationPrincipal User admin) {
        Page<Task> taskPage = taskService.getTasks(admin, id, query);
        return PageResponse.from(taskPage, TaskResponse::from);
    }

    @Operation(summary = "Get groups of a specific user", description = "Allows an admin to view a paginated list of groups belonging to any user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Groups retrieved successfully"),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden")
    })
    @GetMapping("/users/{id}/groups")
    public PageResponse<GroupResponse> getGroups(@PathVariable int id,
                                                 @Valid @ModelAttribute GroupQueryRequest query,
                                                 @AuthenticationPrincipal User admin) {
        Page<Group> groupPage = groupService.getGroups(admin, id, query);
        return PageResponse.from(groupPage, GroupResponse::from);
    }

    @Operation(summary = "Delete a user", description = "Permanently deletes a user and all their associated tasks. Admins cannot delete themselves.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden"),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound")
    })
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable int userId, @AuthenticationPrincipal User admin) {
        userService.deleteUserById(admin, userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Create a task for a user", description = "Allows an admin to create a task on behalf of any specific user.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Task created successfully"),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden"),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound")
    })
    @PostMapping("/users/{userId}/tasks")
    public ResponseEntity<TaskResponse> createTask(@PathVariable int userId, @AuthenticationPrincipal User admin, @Valid @RequestBody CreateTaskRequest body) {
        Task task = taskService.createTask(admin, userId, body);
        return ResponseEntity.status(HttpStatus.CREATED).body(TaskResponse.from(task));
    }

    @Operation(summary = "Create a group for a user", description = "Allows an admin to create a group on behalf of any specific user.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Group created successfully"),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden"),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound")
    })
    @PostMapping("/users/{userId}/groups")
    public ResponseEntity<GroupResponse> createGroup(@PathVariable int userId, @AuthenticationPrincipal User admin, @Valid @RequestBody CreateGroupRequest body) {
        Group group = groupService.createGroup(admin, userId, body);
        return ResponseEntity.status(HttpStatus.CREATED).body(GroupResponse.from(group));
    }

    @Operation(summary = "Create a new user", description = "Allows an admin to create a new user with a specific role (ADMIN or USER).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden")
    })
    @PostMapping("/users")
    public ResponseEntity<UserResponse> createUser(@AuthenticationPrincipal User admin, @Valid @RequestBody CreateUserRequest body) {
        User user = userService.createNewUser(admin, body.username(), body.password(), body.status());
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(user));
    }

    @Operation(summary = "Update a user", description = "Allows an admin to update a user's username, role, or reset their password. Admins cannot demote themselves.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden"),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound")
    })
    @PatchMapping("/users/{userId}")
    public MessageResponse updateUser(@PathVariable int userId, @AuthenticationPrincipal User admin, @Valid @RequestBody UpdateUserRequest body) {
        userService.update(admin, userId, body.username(), body.status());

        if (body.password() != null && !body.password().isBlank()) {
            userService.changePassword(admin, userId, new ChangePasswordRequest(admin.getPassword(), body.password()));
        }

        return new MessageResponse("Updated user with id: " + userId);
    }
}