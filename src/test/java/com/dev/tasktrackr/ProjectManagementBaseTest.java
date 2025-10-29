package com.dev.tasktrackr;

import com.dev.EnableDatabaseTest;
import com.dev.tasktrackr.project.api.dtos.mapper.*;
import com.dev.tasktrackr.project.api.dtos.request.ProjectRequest;
import com.dev.tasktrackr.project.domain.Project;
import com.dev.tasktrackr.project.domain.ProjectInvite;
import com.dev.tasktrackr.project.domain.ProjectMember;
import com.dev.tasktrackr.project.domain.ProjectRole;
import com.dev.tasktrackr.project.domain.enums.ProjectType;
import com.dev.tasktrackr.project.domain.enums.RoleType;
import com.dev.tasktrackr.project.repository.ProjectInviteRepository;
import com.dev.tasktrackr.project.repository.ProjectMemberRepository;
import com.dev.tasktrackr.project.repository.ProjectRepository;
import com.dev.tasktrackr.project.service.*;
import com.dev.tasktrackr.user.domain.UserEntity;
import com.dev.tasktrackr.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@EnableDatabaseTest
@SpringBootTest(classes = {
        // Services:
        ProjectServiceImpl.class,
        ProjectMemberServiceImpl.class,
        ProjectInviteServiceImpl.class,
        ProjectRoleServiceImpl.class,
        ProjectAccessService.class,

        // Mappers:
        ProjectMapperImpl.class,
        ProjectMemberMapperImpl.class,
        ProjectInviteMapperImpl.class,
        RoleMapperImpl.class,
})
public abstract class ProjectManagementBaseTest extends BaseTestContainerConfig {
    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected ProjectRepository projectRepository;

    @Autowired
    protected ProjectInviteRepository projectInviteRepository;

    @Autowired
    protected ProjectMemberRepository projectMemberRepository;

    @Autowired
    protected ProjectMemberService projectMemberService;

    /**
     * Standard Test-User erstellen
     */
    protected UserEntity createTestUser(String id, String username) {
        UserEntity user = UserEntity.builder()
                .id(id)
                .username(username)
                .build();
        return userRepository.save(user);
    }

    /**
     * Standard Test-Project erstellen
     */
    protected Project createTestProject(String name, ProjectType type, UserEntity creator) {
        ProjectRequest request = new ProjectRequest(name, type);
        Project project = Project.create(request, creator);
        return projectRepository.save(project);
    }

    protected ProjectInvite createTestInvite(Project project, UserEntity sender, UserEntity receiver) {
        ProjectInvite invite = ProjectInvite.createInvite(sender, receiver, project);

        return projectInviteRepository.save(invite);
    }

    protected ProjectMember createTestMember(Project project, UserEntity user) {
        ProjectRole role = project.getBaseRole();
        ProjectMember member = ProjectMember.createMember(user, project, role);

        return projectMemberRepository.save(member);
    }

}
