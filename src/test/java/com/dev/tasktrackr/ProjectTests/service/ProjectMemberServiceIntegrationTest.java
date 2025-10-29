package com.dev.tasktrackr.ProjectTests.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
@DisplayName("ProjectMemberService Integration Tests")
public class ProjectMemberServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ProjectMemberServiceImpl projectMemberService;

    private UserEntity ownerUser;
    private UserEntity regularUser1;
    private UserEntity regularUser2;
    private UserEntity nonMemberUser;
    private Project testProject;
    private Long regularMember1Id;
    private Long regularMember2Id;

    @BeforeEach
    void setUp() {
        ownerUser = createTestUser("owner123", "owner");
        regularUser1 = createTestUser("regular456", "regular1");
        regularUser2 = createTestUser("regular789", "regular2");
        nonMemberUser = createTestUser("nonmember999", "nonmember");

        testProject = createTestProject("Test Project", ProjectType.BASIC, ownerUser);
        testProject.addMember(regularUser1);
        testProject.addMember(regularUser2);
        projectRepository.save(testProject);

        // Get member IDs
        regularMember1Id = testProject.getProjectMembers().stream()
                .filter(member -> member.getUser().getId().equals(regularUser1.getId()))
                .findFirst().get().getId();

        regularMember2Id = testProject.getProjectMembers().stream()
                .filter(member -> member.getUser().getId().equals(regularUser2.getId()))
                .findFirst().get().getId();
    }

    @Nested
    @DisplayName("Remove Member Tests")
    class RemoveMemberTests {

        @Test
        @DisplayName("Should remove regular member successfully")
        @Rollback
        void shouldRemoveRegularMemberSuccessfully() {
            // Given
            int initialMemberCount = testProject.getProjectMembers().size();

            // When
            assertDoesNotThrow(() -> projectMemberService.removeMemberFromProject(
                    ownerUser.getId(),
                    testProject.getId(),
                    regularMember1Id
            ));

            // Then
            Project savedProject = projectRepository.findProjectWithInvitesAndMember(testProject.getId()).get();
            assertThat(savedProject.getProjectMembers()).hasSize(initialMemberCount - 1);

            boolean memberExists = savedProject.getProjectMembers().stream()
                    .anyMatch(member -> member.getId().equals(regularMember1Id));
            assertThat(memberExists).isFalse();
        }

        @Test
        @DisplayName("Should throw exception when regular user tries to remove member without permission")
        @Rollback
        void shouldThrowExceptionWhenRegularUserTriesToRemoveMemberWithoutPermission() {
            // When & Then
            assertThatThrownBy(() -> projectMemberService.removeMemberFromProject(
                    regularUser1.getId(),
                    testProject.getId(),
                    regularMember2Id
            )).isInstanceOf(PermissionDeniedException.class);
        }

        @Test
        @DisplayName("Should throw exception for non-existent project")
        @Rollback
        void shouldThrowExceptionForNonExistentProject() {
            // When & Then
            assertThatThrownBy(() -> projectMemberService.removeMemberFromProject(
                    ownerUser.getId(),
                    9999L,
                    regularMember1Id
            )).isInstanceOf(ProjectNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Get All Project Members Tests")
    class GetAllProjectMembersTests {

        @Test
        @DisplayName("Should get all project members with pagination")
        @Rollback
        void shouldGetAllProjectMembersWithPagination() {
            // Given
            PageRequest pageRequest = PageRequest.of(0, 10);

            // When
            Page<ProjectMemberDto> result = projectMemberService.getAllProjectMembers(
                    ownerUser.getId(),
                    testProject.getId(),
                    pageRequest
            );

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(3); // owner + 2 regular
            assertThat(result.getTotalElements()).isEqualTo(3);

            // Verify all members are returned
            assertThat(result.getContent())
                    .extracting(ProjectMemberDto::getUsername)
                    .containsExactlyInAnyOrder(
                            ownerUser.getUsername(),
                            regularUser1.getUsername(),
                            regularUser2.getUsername()
                    );
        }
    }
}
*/