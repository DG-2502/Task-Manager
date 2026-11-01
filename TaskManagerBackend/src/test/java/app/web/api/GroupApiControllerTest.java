package app.web.api;

import app.domain.Group;
import app.domain.User;
import app.exception.GroupNotFoundException;
import app.repo.UserRepository;
import app.service.GroupService;
import app.service.JwtService;
import app.web.config.SecurityConfig;
import app.web.dto.request.group.CreateGroupRequest;
import app.web.dto.request.group.UpdateGroupRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GroupApiController.class)
@Import(SecurityConfig.class)
@AutoConfigureMockMvc(addFilters = false)
class GroupApiControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GroupService groupService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepo;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("testUser", User.Status.USER, "hashed_password");
        testUser.setId(1);
    }

    private static RequestPostProcessor authenticatedUser(User user) {
        return request -> {
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities())
            );
            return request;
        };
    }

    @Test
    void getGroups_ShouldReturn200_AndUseDefaults_WhenNoParamsProvided() throws Exception {
        when(groupService.getGroups(any(), anyInt(), any())).thenReturn(Page.empty());

        mockMvc.perform(get("/api/groups")
                        .with(authenticatedUser(testUser)))
                .andExpect(status().isOk());

        verify(groupService).getGroups(eq(testUser), eq(1), argThat(query ->
                query.page() == 0 && query.size() == 10 &&
                        query.direction() == Sort.Direction.ASC &&
                        query.search() == null
        ));
    }

    @Test
    void getGroups_ShouldReturn200_WhenSearchAndDirectionProvided() throws Exception {
        when(groupService.getGroups(any(), anyInt(), any())).thenReturn(Page.empty());

        mockMvc.perform(get("/api/groups?search=backend&direction=DESC")
                        .with(authenticatedUser(testUser)))
                .andExpect(status().isOk());

        verify(groupService).getGroups(eq(testUser), eq(1), argThat(query ->
                "backend".equals(query.search()) &&
                        query.direction() == Sort.Direction.DESC
        ));
    }

    @Test
    void getGroups_ShouldReturn400_WhenPageIsNegative() throws Exception {
        mockMvc.perform(get("/api/groups?page=-1")
                        .with(authenticatedUser(testUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("page"))
                .andExpect(jsonPath("$.message").value("Page cannot be negative"));
    }

    @Test
    void getGroups_ShouldReturn400_WhenSizeIsTooLarge() throws Exception {
        mockMvc.perform(get("/api/groups?size=101")
                        .with(authenticatedUser(testUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("size"))
                .andExpect(jsonPath("$.message").value("Size cannot exceed 100"));
    }

    @Test
    void getGroups_ShouldReturn400_WhenDirectionEnumIsInvalid() throws Exception {
        mockMvc.perform(get("/api/groups?direction=SIDEWAYS")
                        .with(authenticatedUser(testUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("direction"))
                .andExpect(jsonPath("$.message").value("Invalid value: SIDEWAYS for parameter: direction"));
    }

    @Test
    void createGroup_ShouldReturn201_WhenValid() throws Exception {
        String jsonBody = """
                {
                    "title": "Backend Tasks"
                }
                """;

        Group mockGroup = new Group("Backend Tasks", 1);
        mockGroup.setId(10);

        when(groupService.createGroup(any(User.class), anyInt(), any(CreateGroupRequest.class)))
                .thenReturn(mockGroup);

        mockMvc.perform(post("/api/groups")
                        .with(authenticatedUser(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.title").value("Backend Tasks"))
                .andExpect(jsonPath("$.creatorId").value(1));

        ArgumentCaptor<CreateGroupRequest> captor = ArgumentCaptor.forClass(CreateGroupRequest.class);
        verify(groupService).createGroup(eq(testUser), eq(1), captor.capture());
        assertEquals("Backend Tasks", captor.getValue().title());
    }

    @Test
    void createGroup_ShouldReturn400_WhenTitleIsBlank() throws Exception {
        String jsonBody = """
                {
                    "title": "   "
                }
                """;

        mockMvc.perform(post("/api/groups")
                        .with(authenticatedUser(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("title"))
                .andExpect(jsonPath("$.message").value("Title is required"));

        verify(groupService, never()).createGroup(any(), anyInt(), any());
    }

    @Test
    void createGroup_ShouldReturn400_WhenTitleIsTooLong() throws Exception {
        String jsonBody = """
                {
                    "title": "%s"
                }
                """.formatted("C".repeat(256));

        mockMvc.perform(post("/api/groups")
                        .with(authenticatedUser(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("title"))
                .andExpect(jsonPath("$.message").value("Title is too long"));

        verify(groupService, never()).createGroup(any(), anyInt(), any());
    }

    @Test
    void deleteGroup_ShouldReturn204_WhenValid() throws Exception {
        mockMvc.perform(delete("/api/groups/10")
                        .with(authenticatedUser(testUser)))
                .andExpect(status().isNoContent());

        verify(groupService).deleteGroup(eq(testUser), eq(10));
    }

    @Test
    void deleteGroup_ShouldReturn404_WhenGroupNotFound() throws Exception {
        doThrow(new GroupNotFoundException(10))
                .when(groupService).deleteGroup(any(), eq(10));

        mockMvc.perform(delete("/api/groups/10")
                        .with(authenticatedUser(testUser)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Group with ID: 10 was not found!"));
    }

    @Test
    void updateGroup_ShouldReturn200_WhenValid() throws Exception {
        String jsonBody = """
                {
                    "title": "Renamed Group"
                }
                """;

        mockMvc.perform(patch("/api/groups/10")
                        .with(authenticatedUser(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Updated group with id: 10"));

        ArgumentCaptor<UpdateGroupRequest> captor = ArgumentCaptor.forClass(UpdateGroupRequest.class);
        verify(groupService).updateGroup(eq(testUser), eq(10), captor.capture());
        assertEquals("Renamed Group", captor.getValue().title());
    }

    @Test
    void updateGroup_ShouldReturn400_WhenTitleIsTooLong() throws Exception {
        String jsonBody = """
                {
                    "title": "%s"
                }
                """.formatted("C".repeat(256));

        mockMvc.perform(patch("/api/groups/10")
                        .with(authenticatedUser(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("title"))
                .andExpect(jsonPath("$.message").value("Title is too long"));

        verify(groupService, never()).updateGroup(any(), anyInt(), any());
    }

    @Test
    void updateGroup_ShouldReturn404_WhenGroupNotFound() throws Exception {
        String jsonBody = """
                {
                    "title": "Renamed Group"
                }
                """;

        doThrow(new GroupNotFoundException(10))
                .when(groupService).updateGroup(any(), eq(10), any());

        mockMvc.perform(patch("/api/groups/10")
                        .with(authenticatedUser(testUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Group with ID: 10 was not found!"));
    }
}