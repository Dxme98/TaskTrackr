package com.dev.tasktrackr.ProjectTests.service.shared;

import com.dev.tasktrackr.ProjectTests.service.TestDataFactory;
import com.dev.tasktrackr.project.api.dtos.mapper.*;
import com.dev.tasktrackr.project.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;


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
@Import(TestDataFactory.class)
public abstract class ProjectManagementBaseTest extends BaseTestContainerConfig {
    @Autowired
    protected TestDataFactory testDataFactory;
}
