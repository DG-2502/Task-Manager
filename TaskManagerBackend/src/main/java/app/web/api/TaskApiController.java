package app.web.api;

import app.domain.Task;
import app.domain.User;
import app.service.TaskService;
import app.web.dto.request.task.CreateTaskRequest;
import app.web.dto.request.task.TaskQueryRequest;
import app.web.dto.request.task.UpdateTaskRequest;
import app.web.dto.response.MessageResponse;
import app.web.dto.response.PageResponse;
import app.web.dto.response.TaskResponse;
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
@Tag(name = "Tasks", description = "Endpoints for managing tasks")
@RequestMapping(value = "/api/tasks", produces = MediaType.APPLICATION_JSON_VALUE)
public class TaskApiController {
    private final TaskService taskService;

    public TaskApiController(TaskService taskService) {
        this.taskService = taskService;
    }

    @Operation(summary = "Get my tasks", description = "Retrieves a paginated, filtered, and sorted list of tasks belonging to the authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of tasks"),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized")
    })
    @GetMapping("")
    public PageResponse<TaskResponse> myTasks(@Valid @ModelAttribute TaskQueryRequest query,
                                              @AuthenticationPrincipal User me) {
        Page<Task> taskPage = taskService.getTasks(me, me.getId(), query);
        return PageResponse.from(taskPage, TaskResponse::from);
    }

    @Operation(summary = "Create a new task", description = "Creates a new task for the authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Task created successfully"),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized")
    })
    @PostMapping("")
    public ResponseEntity<TaskResponse> createTask(@AuthenticationPrincipal User me, @Valid @RequestBody CreateTaskRequest body) {
        Task task = taskService.createTask(me, me.getId(), body);
        return ResponseEntity.status(HttpStatus.CREATED).body(TaskResponse.from(task));
    }

    @Operation(summary = "Close a task", description = "Marks a specific task as CLOSED. Users can only close their own tasks.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task closed successfully"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound")
    })
    @PostMapping("/{id}/close")
    public MessageResponse closeTask(@PathVariable int id, @AuthenticationPrincipal User me) {
        taskService.closeTask(me, id);
        return new MessageResponse("Closed task with id: " + id);
    }

    @Operation(summary = "Reopen a task", description = "Marks a specific task as ACTIVE. Users can only reopen their own tasks.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task reopened successfully"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound")
    })
    @PostMapping("/{id}/open")
    public MessageResponse reopenTask(@PathVariable int id, @AuthenticationPrincipal User me) {
        taskService.reopenTask(me, id);
        return new MessageResponse("Opened task with id: " + id);
    }

    @Operation(summary = "Delete a task", description = "Permanently deletes a specific task. Users can only delete their own tasks.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Task deleted successfully"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable int id, @AuthenticationPrincipal User me) {
        taskService.deleteTask(me, id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update a task", description = "Partially updates a task's title or priority. Users can only update their own tasks.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task updated successfully"),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound")
    })
    @PatchMapping("/{id}")
    public MessageResponse updateTask(@PathVariable int id, @AuthenticationPrincipal User me, @Valid @RequestBody UpdateTaskRequest body) {
        taskService.updateTask(me, id, body);
        return new MessageResponse("Updated task with id: " + id);
    }

    @Operation(summary = "Ungroup a task", description = "Sets the group of a task to null. Users can only ungroup their own tasks.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task ungrouped successfully"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound")
    })
    @PostMapping("/{id}/ungroup")
    public MessageResponse ungroupTask(@PathVariable int id, @AuthenticationPrincipal User me) {
        taskService.ungroupTask(me, id);
        return new MessageResponse("Ungrouped task with id: " + id);
    }

    @Operation(summary = "Set group of a task", description = "Change the group of a task to an existing group. Users can only change group of their own tasks.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Set group successfully"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound")
    })
    @PostMapping("/{taskId}/group/{groupId}")
    public MessageResponse setGroup(@PathVariable int taskId, @PathVariable int groupId, @AuthenticationPrincipal User me) {
        taskService.setGroup(me, taskId, groupId);
        return new MessageResponse("Set group of task with id: " + taskId + " to: " + groupId);
    }
}