package com.dev.tasktrackr.ProjectTests.service;

/**
@DisplayName("TaskService Integration Tests")
public class TaskServiceIntegrationTests extends BaseIntegrationTest {

    @Autowired
    private TaskServiceImpl taskService;

    @Autowired
    private TaskQueryRepository taskQueryRepository;

    @Autowired
    private TaskMapper taskMapper;

    private UserEntity ownerUser;
    private UserEntity anotherUser;
    private UserEntity thirdUser;
    private Project testProject;
    private ProjectMember ownerMember;
    private ProjectMember anotherMember;
    private ProjectMember thirdMember;

    @BeforeEach
    void setUp() {
        ownerUser = createTestUser("user123", "testuser");
        anotherUser = createTestUser("user456", "anotheruser");
        thirdUser = createTestUser("user789", "thirduser");

        // Create project with test user as owner
        testProject = createTestProject("Test Project", ProjectType.BASIC, ownerUser);

        // Add additional members to project
        ownerMember = testProject.findProjectMember(ownerUser.getId());
        testProject.addMember(anotherUser);
        testProject.addMember(thirdUser);

        projectRepository.save(testProject);

        // Get actual saved members
        anotherMember = testProject.findProjectMember(anotherUser.getId());
        thirdMember = testProject.findProjectMember(thirdUser.getId());
    }

    @Nested
    @DisplayName("Create Task Tests")
    class CreateTaskTests {

        @Test
        @DisplayName("Should create task successfully")
        @Rollback
        void shouldCreateTaskSuccessfully() {
            // Given

            CreateTaskRequest request = CreateTaskRequest.builder()
                    .title("Test Task")
                    .description("Test Description")
                    .priority(Priority.HIGH)
                    .dueDate(LocalDateTime.now().plusDays(7))
                    .assignedToMemberIds(Set.of(thirdMember.getId()))
                    .build();

            // When
            TaskResponseDto result = taskService.createTask(testProject.getId(), request, ownerUser.getId());

            // Then
            assertNotNull(result);
            assertEquals("Test Task", result.getTitle());
            assertEquals("Test Description", result.getDescription());
            assertEquals(Priority.HIGH, result.getPriority());
            assertEquals(Status.IN_PROGRESS, result.getStatus());
            assertNotNull(result.getDueDate());
            assertNotNull(result.getCreatedAt());
            assertEquals(ownerMember.getId(), result.getCreatedBy().getId());
            assertEquals(1, result.getAssignedToMembers().size());

            // Verify in database
            Project savedProject = projectRepository.findById(testProject.getId()).get();
            BasicDetails basicDetails = savedProject.getBasicDetails();
            assertEquals(1, basicDetails.getTasks().size());

            Task savedTask = basicDetails.getTasks().iterator().next();
            assertEquals("Test Task", savedTask.getTitle());
            assertEquals(Status.IN_PROGRESS, savedTask.getStatus());
            assertEquals(1, savedTask.getAssignedMembers().size());
        }

        @Test
        @DisplayName("Should create task with multiple assigned members")
        @Rollback
        void shouldCreateTaskWithMultipleAssignedMembers() {
            // Given
            CreateTaskRequest request = CreateTaskRequest.builder()
                    .title("Multi-Member Task")
                    .description("Task with multiple members")
                    .priority(Priority.MEDIUM)
                    .dueDate(LocalDateTime.now().plusDays(5))
                    .assignedToMemberIds(Set.of(anotherMember.getId(), thirdMember.getId()))
                    .build();

            // When
            TaskResponseDto result = taskService.createTask(testProject.getId(), request, ownerUser.getId());

            // Then
            assertEquals(2, result.getAssignedToMembers().size());

            // Verify in database
            Project savedProject = projectRepository.findById(testProject.getId()).get();
            Task savedTask = savedProject.getBasicDetails().getTasks().iterator().next();
            assertEquals(2, savedTask.getAssignedMembers().size());
        }

        @Test
        @DisplayName("Should throw exception for non-existent project")
        @Rollback
        void shouldThrowExceptionForNonExistentProject() {
            // Given
            CreateTaskRequest request = CreateTaskRequest.builder()
                    .title("Test Task")
                    .description("Test Description")
                    .priority(Priority.HIGH)
                    .assignedToMemberIds(Set.of(anotherMember.getId()))
                    .build();

            // When/Then
            assertThrows(ProjectNotFoundException.class,
                    () -> taskService.createTask(999L, request, ownerUser.getId()));
        }

        @Test
        @DisplayName("Should throw exception for non-existent user")
        @Rollback
        void shouldThrowExceptionForNonExistentUser() {
            // Given
            CreateTaskRequest request = CreateTaskRequest.builder()
                    .title("Test Task")
                    .description("Test Description")
                    .priority(Priority.HIGH)
                    .assignedToMemberIds(Set.of(anotherMember.getId()))
                    .build();

            // When/Then
            assertThrows(UserNotProjectMemberException.class,
                    () -> taskService.createTask(testProject.getId(), request, "nonexistent-user"));
        }

        @Test
        @DisplayName("Should throw exception for non-existent assigned member")
        @Rollback
        void shouldThrowExceptionForNonExistentAssignedMember() {
            // Given
            CreateTaskRequest request = CreateTaskRequest.builder()
                    .title("Test Task")
                    .description("Test Description")
                    .priority(Priority.HIGH)
                    .assignedToMemberIds(Set.of(999L))
                    .build();

            // When/Then
            assertThrows(ProjectMemberNotFoundException.class,
                    () -> taskService.createTask(testProject.getId(), request, ownerUser.getId()));
        }

        @Test
        @DisplayName("Should create task without due date")
        @Rollback
        void shouldCreateTaskWithoutDueDate() {
            // Given
            CreateTaskRequest request = CreateTaskRequest.builder()
                    .title("No Due Date Task")
                    .description("Task without due date")
                    .priority(Priority.LOW)
                    .assignedToMemberIds(Set.of(anotherMember.getId()))
                    .build();

            // When
            TaskResponseDto result = taskService.createTask(testProject.getId(), request, ownerUser.getId());

            // Then
            assertNotNull(result);
            assertNull(result.getDueDate());
        }
    }

    @Nested
    @DisplayName("Complete Task Tests")
    class CompleteTaskTests {

        private Task testTask;

        @BeforeEach
        void setUpTask() {
            CreateTaskRequest request = CreateTaskRequest.builder()
                    .title("Completable Task")
                    .description("Task to be completed")
                    .priority(Priority.MEDIUM)
                    .assignedToMemberIds(Set.of(anotherMember.getId()))
                    .build();

            TaskResponseDto createdTask = taskService.createTask(testProject.getId(), request, ownerUser.getId());

            // Refresh project to get the created task
            testProject = projectRepository.findById(testProject.getId()).get();
            testTask = testProject.getBasicDetails().getTasks().stream()
                    .filter(task -> task.getId().equals(createdTask.getId()))
                    .findFirst().orElseThrow();
        }

        @Test
        @DisplayName("Should complete task as creator")
        @Rollback
        void shouldCompleteTaskAsCreator() {
            // When
            TaskResponseDto result = taskService.completeTask(testProject.getId(), testTask.getId(), ownerUser.getId());

            // Then
            assertNotNull(result);
            assertEquals(Status.COMPLETED, result.getStatus());
            assertNotNull(result.getUpdatedAt());

            // Verify in database
            Project savedProject = projectRepository.findById(testProject.getId()).get();
            Task savedTask = savedProject.getBasicDetails().findTask(testTask.getId());
            assertEquals(Status.COMPLETED, savedTask.getStatus());
        }

        @Test
        @DisplayName("Should complete task as assigned member")
        @Rollback
        void shouldCompleteTaskAsAssignedMember() {
            // When
            TaskResponseDto result = taskService.completeTask(testProject.getId(), testTask.getId(), anotherUser.getId());

            // Then
            assertNotNull(result);
            assertEquals(Status.COMPLETED, result.getStatus());
        }

        @Test
        @DisplayName("Should throw exception when non-assigned member tries to complete task")
        @Rollback
        void shouldThrowExceptionWhenNonAssignedMemberTriesToComplete() {
            // When/Then
            assertThrows(ProjectMemberNotAllowedToCompleteTaskException.class,
                    () -> taskService.completeTask(testProject.getId(), testTask.getId(), thirdUser.getId()));
        }

        @Test
        @DisplayName("Should throw exception for non-existent task")
        @Rollback
        void shouldThrowExceptionForNonExistentTask() {
            // When/Then
            assertThrows(TaskNotFoundException.class,
                    () -> taskService.completeTask(testProject.getId(), 999L, ownerUser.getId()));
        }

        @Test
        @DisplayName("Should throw exception for non-existent project")
        @Rollback
        void shouldThrowExceptionForNonExistentProjectWhenCompleting() {
            // When/Then
            assertThrows(ProjectNotFoundException.class,
                    () -> taskService.completeTask(999L, testTask.getId(), ownerUser.getId()));
        }
    }

    @Nested
    @DisplayName("Delete Task Tests")
    class DeleteTaskTests {

        private Task testTask;

        @BeforeEach
        void setUpTask() {
            CreateTaskRequest request = CreateTaskRequest.builder()
                    .title("Deletable Task")
                    .description("Task to be deleted")
                    .priority(Priority.LOW)
                    .assignedToMemberIds(Set.of(anotherMember.getId()))
                    .build();

            TaskResponseDto createdTask = taskService.createTask(testProject.getId(), request, ownerUser.getId());

            // Refresh project to get the created task
            testProject = projectRepository.findById(testProject.getId()).get();
            testTask = testProject.getBasicDetails().getTasks().stream()
                    .filter(task -> task.getId().equals(createdTask.getId()))
                    .findFirst().orElseThrow();
        }

        @Test
        @DisplayName("Should delete task as owner")
        @Rollback
        void shouldDeleteTaskAsOwner() {
            // Given
            assertEquals(1, testProject.getBasicDetails().getTasks().size());

            // When
            taskService.deleteTask(testProject.getId(), testTask.getId(), ownerUser.getId());

            // Then
            // Verify in database
            Project savedProject = projectRepository.findById(testProject.getId()).get();
            assertEquals(0, savedProject.getBasicDetails().getTasks().size());
        }

        @Test
        @DisplayName("Should throw exception when member without Permission tries to delete task")
        @Rollback
        void shouldThrowExceptionWhenMemberWithoutPermissionTriesToDeleteTask() {
            // When/Then
            assertThrows(PermissionDeniedException.class,
                    () -> taskService.deleteTask(testProject.getId(), testTask.getId(), anotherUser.getId()));
        }

        @Test
        @DisplayName("Should throw exception for non-existent task")
        @Rollback
        void shouldThrowExceptionForNonExistentTaskWhenDeleting() {
            // When/Then
            assertThrows(TaskNotFoundException.class,
                    () -> taskService.deleteTask(testProject.getId(), 999L, ownerUser.getId()));
        }

        @Test
        @DisplayName("Should throw exception for non-existent project")
        @Rollback
        void shouldThrowExceptionForNonExistentProjectWhenDeleting() {
            // When/Then
            assertThrows(ProjectNotFoundException.class,
                    () -> taskService.deleteTask(999L, testTask.getId(), ownerUser.getId()));
        }
    }

    @Nested
    @DisplayName("Find All Tasks Tests")
    class FindAllTasksTests {

        private Task completedTask;
        private Task inProgressTask;
        private Task assignedToUserTask;

        @BeforeEach
        void setUpTasks() {
            // Create completed task
            CreateTaskRequest completedRequest = CreateTaskRequest.builder()
                    .title("Completed Task")
                    .description("This task is completed")
                    .priority(Priority.HIGH)
                    .assignedToMemberIds(Set.of(anotherMember.getId()))
                    .build();

            TaskResponseDto completedTaskDto = taskService.createTask(testProject.getId(), completedRequest, ownerUser.getId());
            taskService.completeTask(testProject.getId(), completedTaskDto.getId(), ownerUser.getId());

            // Create in-progress task
            CreateTaskRequest inProgressRequest = CreateTaskRequest.builder()
                    .title("In Progress Task")
                    .description("This task is in progress")
                    .priority(Priority.MEDIUM)
                    .assignedToMemberIds(Set.of(thirdMember.getId()))
                    .build();

            taskService.createTask(testProject.getId(), inProgressRequest, ownerUser.getId());

            // Create task assigned to another user
            CreateTaskRequest assignedRequest = CreateTaskRequest.builder()
                    .title("Assigned Task")
                    .description("This task is assigned to another user")
                    .priority(Priority.LOW)
                    .assignedToMemberIds(Set.of(anotherMember.getId()))
                    .build();

            taskService.createTask(testProject.getId(), assignedRequest, ownerUser.getId());
        }

        @Test
        @DisplayName("Should find all tasks without filters")
        @Rollback
        void shouldFindAllTasksWithoutFilters() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<TaskResponseDto> result = taskService.findAllTasks(
                    testProject.getId(), pageable, ownerUser.getId(), false, null);

            // Then
            assertEquals(3, result.getTotalElements());
            assertEquals(3, result.getContent().size());
        }

        @Test
        @DisplayName("Should find tasks by status")
        @Rollback
        void shouldFindTasksByStatus() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When - Find completed tasks
            Page<TaskResponseDto> completedTasks = taskService.findAllTasks(
                    testProject.getId(), pageable, ownerUser.getId(), false, Status.COMPLETED);

            // Then
            assertEquals(1, completedTasks.getTotalElements());
            assertEquals(Status.COMPLETED, completedTasks.getContent().get(0).getStatus());

            // When - Find in-progress tasks
            Page<TaskResponseDto> inProgressTasks = taskService.findAllTasks(
                    testProject.getId(), pageable, ownerUser.getId(), false, Status.IN_PROGRESS);

            // Then
            assertEquals(2, inProgressTasks.getTotalElements());
            inProgressTasks.getContent().forEach(task ->
                    assertEquals(Status.IN_PROGRESS, task.getStatus()));
        }

        @Test
        @DisplayName("Should find tasks assigned to user with no results")
        @Rollback
        void shouldFindTasksAssignedToUserWithNoResults() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When - Find tasks assigned to thirdUser (only one task)
            Page<TaskResponseDto> assignedTasks = taskService.findAllTasks(
                    testProject.getId(), pageable, thirdUser.getId(), true, null);

            // Then
            assertEquals(1, assignedTasks.getTotalElements());
        }

        @Test
        @DisplayName("Should throw exception for non-existent project")
        @Rollback
        void shouldThrowExceptionForNonExistentProjectWhenFinding() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When/Then
            assertThrows(ProjectNotFoundException.class,
                    () -> taskService.findAllTasks(999L, pageable, ownerUser.getId(), false, null));
        }

        @Test
        @DisplayName("Should throw exception for non-member user")
        @Rollback
        void shouldThrowExceptionForNonMemberUser() {
            // Given
            UserEntity nonMember = createTestUser("nonmember", "nonmember");
            Pageable pageable = PageRequest.of(0, 10);

            // When/Then
            assertThrows(UserNotProjectMemberException.class,
                    () -> taskService.findAllTasks(testProject.getId(), pageable, nonMember.getId(), false, null));
        }

        @Test
        @DisplayName("Should handle pagination correctly")
        @Rollback
        void shouldHandlePaginationCorrectly() {
            // Given
            Pageable pageable = PageRequest.of(0, 2, Sort.by("id"));

            // When
            Page<TaskResponseDto> result = taskService.findAllTasks(
                    testProject.getId(), pageable, ownerUser.getId(), false, null);

            // Then
            assertEquals(3, result.getTotalElements());
            assertEquals(2, result.getSize());
            assertEquals(2, result.getNumberOfElements());
            assertEquals(2, result.getTotalPages());
            assertTrue(result.hasNext());
        }
    }
}
 */