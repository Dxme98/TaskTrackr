package com.dev.tasktrackr;

import com.dev.EnableDatabaseTest;
import com.dev.tasktrackr.project.api.dtos.mapper.*;
import com.dev.tasktrackr.project.api.dtos.request.ProjectRequest;
import com.dev.tasktrackr.project.domain.Project;
import com.dev.tasktrackr.project.domain.enums.ProjectType;
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
public abstract class ProjectFeatureBaseTest extends BaseTestContainerConfig {
    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected ProjectRepository projectRepository;

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
}
