package com.dev.tasktrackr.ProjectTests.service.shared;

import com.dev.tasktrackr.ProjectTests.service.TestDataFactory;
import com.dev.tasktrackr.project.api.dtos.mapper.*;
import com.dev.tasktrackr.project.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@EnableDatabaseTest
@SpringBootTest(classes = {
        // 1. Management-Services
        ProjectServiceImpl.class,
        ProjectMemberServiceImpl.class,
        ProjectInviteServiceImpl.class,
        ProjectRoleServiceImpl.class,
        ProjectAccessService.class,

        // 2. BasicDetails-Services
        TaskServiceImpl.class,
        ProjectInformationServiceImpl.class,

        // 3. Mapper
        ProjectMapperImpl.class,
        ProjectMemberMapperImpl.class,
        ProjectInviteMapperImpl.class,
        RoleMapperImpl.class,
        TaskMapperImpl.class
})
@Import(TestDataFactory.class)
public abstract class BasicDetailsBaseTest extends BaseTestContainerConfig {

    @Autowired
    protected TestDataFactory testDataFactory;
}
