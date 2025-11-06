package com.dev.tasktrackr.ProjectTests.controller;

import com.dev.tasktrackr.config.JpaAuditingConfig;
import com.dev.tasktrackr.basicdetails.api.controller.TaskController;
import com.dev.tasktrackr.basicdetails.api.dtos.request.CreateTaskRequest;
import com.dev.tasktrackr.project.api.dtos.response.ProjectMemberOverviewDto;
import com.dev.tasktrackr.basicdetails.api.dtos.response.TaskResponseDto;
import com.dev.tasktrackr.project.domain.enums.Priority;
import com.dev.tasktrackr.project.domain.enums.Status;
import com.dev.tasktrackr.basicdetails.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TaskController.class)
@ImportAutoConfiguration(exclude = JpaAuditingConfig.class)
@DisplayName("TaskController (WebMvcTest)")
class TaskControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TaskService taskService;

    private static final String API_BASE_URL_TEMPLATE = "/api/v1/projects/{projectId}/tasks";

    private String testUserId;
    private String testUsername;
    private Long testProjectId;
    private LocalDateTime fixedFutureDate;

    @BeforeEach
    void setUp() {
        testUserId = "user-id-123";
        testUsername = "testuser";
        testProjectId = 1L;
        // Fixes Datum in der Zukunft für konsistente Validierung
        fixedFutureDate = LocalDateTime.now().plusDays(5);
    }

    @Test
    @DisplayName("POST /projects/{projectId}/tasks - Sollte 201 Created zurückgeben und Task erstellen")
    void createTask_whenValidRequest_shouldReturn201AndTask() throws Exception {
        // Given
        CreateTaskRequest request = CreateTaskRequest.builder()
                .title("Neue Task")
                .description("Beschreibung der Task")
                .priority(Priority.HIGH)
                .dueDate(fixedFutureDate)
                .assignedToMemberIds(Set.of(10L, 20L))
                .build();

        TaskResponseDto expectedResponse = TaskResponseDto.builder()
                .id(123L) // ID der neuen Task
                .projectId(testProjectId)
                .title("Neue Task")
                .status(Status.IN_PROGRESS) // Angenommener Standardstatus
                .priority(Priority.HIGH)
                .createdBy(new ProjectMemberOverviewDto(1L, testUsername)) // Angenommener Ersteller
                .build();

        when(taskService.createTask(eq(testProjectId), any(CreateTaskRequest.class), eq(testUserId)))
                .thenReturn(expectedResponse);

        // When & Then
        mockMvc.perform(post(API_BASE_URL_TEMPLATE, testProjectId)
                        .with(jwt().jwt(jwt -> jwt
                                .claim("sub", testUserId)
                                .claim("preferred_username", testUsername)
                        ))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(123L))
                .andExpect(jsonPath("$.title").value("Neue Task"))
                .andExpect(jsonPath("$.status").value(Status.IN_PROGRESS.toString()))
                .andExpect(jsonPath("$.createdBy.username").value(testUsername));

        verify(taskService).createTask(eq(testProjectId), any(CreateTaskRequest.class), eq(testUserId));
    }

    @Test
    @DisplayName("POST /projects/{projectId}/tasks - Sollte 400 Bad Request bei ungültiger Anfrage zurückgeben (z.B. Titel fehlt)")
    void createTask_whenInvalidRequest_shouldReturn400() throws Exception {
        // Given
        // Titel fehlt -> @NotBlank verletzt
        CreateTaskRequest request = CreateTaskRequest.builder()
                .description("Beschreibung ohne Titel")
                .priority(Priority.LOW)
                .dueDate(fixedFutureDate)
                .assignedToMemberIds(Set.of(10L))
                .build();

        // When & Then
        mockMvc.perform(post(API_BASE_URL_TEMPLATE, testProjectId)
                        .with(jwt().jwt(jwt -> jwt.claim("sub", testUserId)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        // Service darf nicht aufgerufen werden
        verify(taskService, never()).createTask(anyLong(), any(), anyString());
    }

    @Test
    @DisplayName("PUT /projects/{projectId}/tasks/{taskId}/complete - Sollte 200 OK zurückgeben und Task als 'COMPLETED' markieren")
    void completeTask_whenValidRequest_shouldReturn200AndCompletedTask() throws Exception {
        // Given
        Long taskId = 42L;
        TaskResponseDto expectedResponse = TaskResponseDto.builder()
                .id(taskId)
                .projectId(testProjectId)
                .title("Abgeschlossene Task")
                .status(Status.COMPLETED) // Wichtige Änderung
                .build();

        when(taskService.completeTask(eq(testProjectId), eq(taskId), eq(testUserId)))
                .thenReturn(expectedResponse);

        // When & Then
        mockMvc.perform(put(API_BASE_URL_TEMPLATE + "/{taskId}/complete", testProjectId, taskId)
                        .with(jwt().jwt(jwt -> jwt
                                .claim("sub", testUserId)
                                .claim("preferred_username", testUsername)
                        )))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(taskId))
                .andExpect(jsonPath("$.status").value(Status.COMPLETED.toString()));

        verify(taskService).completeTask(eq(testProjectId), eq(taskId), eq(testUserId));
    }

    @Test
    @DisplayName("DELETE /projects/{projectId}/tasks/{taskId} - Sollte 204 No Content bei erfolgreichem Löschen zurückgeben")
    void deleteTask_whenValidRequest_shouldReturn204() throws Exception {
        // Given
        Long taskId = 42L;
        // Service-Methode ist void
        doNothing().when(taskService).deleteTask(eq(testProjectId), eq(taskId), eq(testUserId));

        // When & Then
        mockMvc.perform(delete(API_BASE_URL_TEMPLATE + "/{taskId}", testProjectId, taskId)
                        .with(jwt().jwt(jwt -> jwt
                                .claim("sub", testUserId)
                                .claim("preferred_username", testUsername)
                        )))
                .andExpect(status().isNoContent());

        verify(taskService).deleteTask(eq(testProjectId), eq(taskId), eq(testUserId));
    }


    @Test
    @DisplayName("GET /projects/{projectId}/tasks - Sollte 200 OK und eine Seite mit Tasks zurückgeben (Standardparameter)")
    void findAllTasks_whenDefaultParams_shouldReturn200AndPage() throws Exception {
        // Given
        int page = 0;
        int size = 10;
        String sortBy = "createdAt";
        String sortDir = "asc";

        TaskResponseDto task1 = TaskResponseDto.builder().id(100L).title("Task 1").build();
        TaskResponseDto task2 = TaskResponseDto.builder().id(101L).title("Task 2").build();
        List<TaskResponseDto> taskList = Arrays.asList(task1, task2);

        Pageable expectedPageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));
        Page<TaskResponseDto> mockPage = new PageImpl<>(taskList, expectedPageable, taskList.size());

        // Standardaufruf: assigned=false, status=null
        when(taskService.findAllTasks(eq(testProjectId), any(Pageable.class), eq(testUserId), eq(false), eq(null)))
                .thenReturn(mockPage);

        // When & Then
        mockMvc.perform(get(API_BASE_URL_TEMPLATE, testProjectId)
                        .with(jwt().jwt(jwt -> jwt.claim("sub", testUserId)))
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size))
                        .param("sortBy", sortBy)
                        .param("sortDir", sortDir))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].title", is("Task 1")))
                .andExpect(jsonPath("$.content[1].title", is("Task 2")))
                .andExpect(jsonPath("$.page", is(page)))
                .andExpect(jsonPath("$.size", is(size)))
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.last", is(true)));

        verify(taskService).findAllTasks(eq(testProjectId), eq(expectedPageable), eq(testUserId), eq(false), eq(null));
    }

    @Test
    @DisplayName("GET /projects/{projectId}/tasks - Sollte 200 OK und Service mit Filterparametern aufrufen")
    void findAllTasks_whenFilteredAndSorted_shouldCallServiceWithFilters() throws Exception {
        // Given
        int page = 1;
        int size = 5;
        Status status = Status.IN_PROGRESS;
        boolean assigned = true;
        String sortBy = "title";
        String sortDir = "desc";

        Page<TaskResponseDto> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(page, size), 0);

        // Service-Mock für den gefilterten Aufruf
        when(taskService.findAllTasks(eq(testProjectId), any(Pageable.class), eq(testUserId), eq(assigned), eq(status)))
                .thenReturn(emptyPage);

        // ArgumentCaptor, um das Pageable-Objekt abzufangen
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        // When & Then
        mockMvc.perform(get(API_BASE_URL_TEMPLATE, testProjectId)
                        .with(jwt().jwt(jwt -> jwt.claim("sub", testUserId)))
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size))
                        .param("status", status.toString())
                        .param("assigned", String.valueOf(assigned))
                        .param("sortBy", sortBy)
                        .param("sortDir", sortDir))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.page", is(page)));

        // Verifizieren, dass der Service mit den korrekten Filtern UND dem korrekten Pageable aufgerufen wurde
        verify(taskService).findAllTasks(eq(testProjectId), pageableCaptor.capture(), eq(testUserId), eq(assigned), eq(status));

        Pageable capturedPageable = pageableCaptor.getValue();
        assertEquals(page, capturedPageable.getPageNumber());
        assertEquals(size, capturedPageable.getPageSize());
        assertEquals(Sort.by(Sort.Direction.DESC, "title"), capturedPageable.getSort());
    }
}