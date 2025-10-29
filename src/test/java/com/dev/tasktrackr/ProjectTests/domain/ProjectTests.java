package com.dev.tasktrackr.ProjectTests.domain;

import com.dev.tasktrackr.project.api.dtos.request.ProjectRequest;
import com.dev.tasktrackr.project.domain.Project;
import com.dev.tasktrackr.project.domain.ProjectMember;
import com.dev.tasktrackr.project.domain.ProjectRole;
import com.dev.tasktrackr.project.domain.enums.ProjectType;
import com.dev.tasktrackr.project.domain.enums.RoleType;
// Import für die neue Exception
import com.dev.tasktrackr.shared.exception.custom.AccessDeniedExceptions.InvalidProjectTypeException;
import com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions.RoleNotFoundException;
import com.dev.tasktrackr.user.domain.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Project Entity Tests")
public class ProjectTests {

    @Mock
    private UserEntity mockUser;

    private Project project;
    private ProjectRequest projectRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup mock user with ID
        when(mockUser.getId()).thenReturn("user123");

        // Setup project request
        projectRequest = new ProjectRequest("Test Project", ProjectType.BASIC);

        // Create fresh project for each test
        project = Project.create(projectRequest, mockUser);

        // Set manual Ids
        ProjectMember member = project.getProjectMembers().stream()
                .filter(m -> m.getUser().getId().equals("user123"))
                .findFirst().get();
        ReflectionTestUtils.setField(member, "id", 1L);

        // Role IDs
        ProjectRole ownerRole = project.getOwnerRole();
        ReflectionTestUtils.setField(ownerRole, "id", 1);

        ProjectRole baseRole = project.getBaseRole();
        ReflectionTestUtils.setField(baseRole, "id", 2);
    }

    @Nested
    @DisplayName("Project Creation Tests")
    class ProjectCreationTests {

        @Test
        @DisplayName("Should create project with correct name and type")
        void shouldCreateProjectWithNameAndProjectType() {
            Project createdProject = Project.create(projectRequest, mockUser);

            assertNotNull(createdProject);
            assertEquals("Test Project", createdProject.getName());
            assertEquals(ProjectType.BASIC, createdProject.getProjectType());
        }

        @Test
        @DisplayName("Should initialize base roles on creation")
        void shouldInitializeBaseRolesOnCreation() {
            Project createdProject = Project.create(projectRequest, mockUser);
            ProjectRole ownerRole = createdProject.getOwnerRole();
            ProjectRole baseRole = createdProject.getBaseRole();

            assertNotNull(ownerRole);
            assertNotNull(baseRole);
            assertEquals(RoleType.OWNER, ownerRole.getRoleType());
            assertEquals(RoleType.BASE, baseRole.getRoleType());
            assertEquals(2, createdProject.getProjectRoles().size());
        }

        @Test
        @DisplayName("Should add creator as ProjectMember with Owner Role")
        void shouldAddCreatorAsOwnerMember() {
            Project createdProject = Project.create(projectRequest, mockUser);

            assertEquals(1, createdProject.getProjectMembers().size());

            ProjectMember ownerMember = createdProject.getProjectMembers().iterator().next();
            assertEquals(RoleType.OWNER, ownerMember.getProjectRole().getRoleType());
            assertEquals(mockUser, ownerMember.getUser());
        }

        @Test
        @DisplayName("Should assign BasicDetails for BASIC project type")
        void shouldAssignBasicDetailsForBasicProject() {
            Project basicProject = Project.create(projectRequest, mockUser);

            assertNotNull(basicProject.getBasicDetails());
            // Testet, dass der Typ korrekt geprüft wird
            assertThrows(InvalidProjectTypeException.class, basicProject::getScrumDetails);
        }

        @Test
        @DisplayName("Should assign ScrumDetails for SCRUM project type")
        void shouldAssignScrumDetailsForScrumProject() {
            ProjectRequest scrumRequest = new ProjectRequest("Scrum Project", ProjectType.SCRUM);
            Project scrumProject = Project.create(scrumRequest, mockUser);

            assertNotNull(scrumProject.getScrumDetails());
            // Testet, dass der Typ korrekt geprüft wird
            assertThrows(InvalidProjectTypeException.class, scrumProject::getBasicDetails);
        }
    }

    @Nested
    @DisplayName("Member Tests")
    class MemberTests {

        @Test
        @DisplayName("Should add new Member with Role successfully")
        void shouldAddMemberWithRoleSuccessfully() {
            UserEntity newUser = mock(UserEntity.class);
            when(newUser.getId()).thenReturn("newUser123");
            ProjectRole testRole = ProjectRole.createCustomRole(project, "TestRolle", new HashSet<>());

            // Die einzige verbliebene Logik: Das Hinzufügen zur Collection
            ProjectMember addedMember = project.addMemberWithRole(newUser, testRole);

            assertEquals(2, project.getProjectMembers().size());
            assertEquals(RoleType.CUSTOM, addedMember.getProjectRole().getRoleType());
            assertEquals("TestRolle", addedMember.getProjectRole().getName());
        }
    }


    @Nested
    @DisplayName("Helper Method Tests")
    class HelperMethodTests {
        @Test
        @DisplayName("Should get owner role successfully")
        void shouldGetOwnerRoleSuccessfully() {
            ProjectRole ownerRole = project.getOwnerRole();

            assertNotNull(ownerRole);
            assertEquals(RoleType.OWNER, ownerRole.getRoleType());
            assertEquals("OWNER", ownerRole.getName());
        }

        @Test
        @DisplayName("Should get base role successfully")
        void shouldGetBaseRoleSuccessfully() {
            ProjectRole baseRole = project.getBaseRole();

            assertNotNull(baseRole);
            assertEquals(RoleType.BASE, baseRole.getRoleType());
            assertEquals("BASE", baseRole.getName());
        }

        @Test
        @DisplayName("Should throw when getting base role if not initialized")
        void shouldThrowWhenGettingBaseRoleIfNotInitialized() {
            Project emptyProject = new Project(); // Leeres Projekt ohne init
            assertThrows(RoleNotFoundException.class, emptyProject::getBaseRole);
        }
    }
}