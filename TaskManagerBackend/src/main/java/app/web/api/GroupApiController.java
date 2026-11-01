package app.web.api;

import app.domain.Group;
import app.domain.User;
import app.service.GroupService;
import app.web.dto.request.group.CreateGroupRequest;
import app.web.dto.request.group.GroupQueryRequest;
import app.web.dto.request.group.UpdateGroupRequest;
import app.web.dto.response.GroupResponse;
import app.web.dto.response.MessageResponse;
import app.web.dto.response.PageResponse;
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
@Tag(name = "Groups")
@RequestMapping(value = "/api/groups", produces = MediaType.APPLICATION_JSON_VALUE)
public class GroupApiController {
    private final GroupService groupService;

    public GroupApiController(GroupService groupService) {
        this.groupService = groupService;
    }

    @Operation(summary = "Get my groups", description = "Retrieves a paginated, filtered, and sorted list of groups belonging to the authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of groups"),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized")
    })
    @GetMapping("")
    public PageResponse<GroupResponse> myGroups(@Valid @ModelAttribute GroupQueryRequest query,
                                                @AuthenticationPrincipal User me) {
        Page<Group> groupPage = groupService.getGroups(me, me.getId(), query);
        return PageResponse.from(groupPage, GroupResponse::from);
    }

    @Operation(summary = "Create a new group", description = "Creates a new group for the authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Group created successfully"),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized")
    })
    @PostMapping("")
    public ResponseEntity<GroupResponse> createGroup(@AuthenticationPrincipal User me, @Valid @RequestBody CreateGroupRequest body) {
        Group group = groupService.createGroup(me, me.getId(), body);
        return ResponseEntity.status(HttpStatus.CREATED).body(GroupResponse.from(group));
    }

    @Operation(summary = "Delete a group", description = "Permanently deletes a specific group along with all tasks in it. Users can only delete their own tasks.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Group deleted successfully"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroup(@PathVariable int id, @AuthenticationPrincipal User me) {
        groupService.deleteGroup(me, id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update a group", description = "Updates a group's title. Users can only update their own tasks.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Group updated successfully"),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound")
    })
    @PatchMapping("/{id}")
    public MessageResponse updateGroup(@PathVariable int id, @AuthenticationPrincipal User me, @Valid @RequestBody UpdateGroupRequest body) {
        groupService.updateGroup(me, id, body);
        return new MessageResponse("Updated group with id: " + id);
    }
}
