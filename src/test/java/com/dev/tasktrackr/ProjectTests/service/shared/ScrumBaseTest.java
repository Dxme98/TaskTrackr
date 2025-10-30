package com.dev.tasktrackr.ProjectTests.service.shared;

import com.dev.tasktrackr.ProjectTests.service.TestDataFactory;
import com.dev.tasktrackr.project.api.dtos.mapper.*;
import com.dev.tasktrackr.project.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@EnableDatabaseTest
@SpringBootTest(classes = {
        // 1. Management-Services,
        ProjectAccessService.class,

        // 2. Scrum-Services
        ScrumBoardServiceImpl.class,
        ScrumReportsService.class,
        SprintServiceImpl.class,
        UserStoryServiceImpl.class,

        // 3. Mapper
        UserStoryMapper.class,
        ProjectMapperImpl.class,
        ProjectMemberMapperImpl.class,
        ScrumBoardMapper.class,
        SprintBacklogItemMapper.class,
        CommentMapperImpl.class,
        SprintMapper.class,
        SprintSummaryItemMapperImpl.class
})
@Import(TestDataFactory.class)
public abstract class ScrumBaseTest extends BaseTestContainerConfig{

    @Autowired
    protected TestDataFactory testDataFactory;
}
