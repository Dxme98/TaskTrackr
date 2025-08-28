package com.dev.tasktrackr.project.service;

import com.dev.tasktrackr.project.api.dtos.ProjectMapper;
import com.dev.tasktrackr.project.api.dtos.request.ProjectRequest;
import com.dev.tasktrackr.project.api.dtos.response.ProjectOverviewDto;
import com.dev.tasktrackr.project.domain.Project;
import com.dev.tasktrackr.project.domain.ProjectType;
import com.dev.tasktrackr.project.repository.ProjectRepository;
import com.dev.tasktrackr.project.repository.ProjectTypeQueryRepository;
import com.dev.tasktrackr.shared.exception.custom.ProjectTypeNotFoundException;
import com.dev.tasktrackr.user.UserEntity;
import com.dev.tasktrackr.user.UserId;
import com.dev.tasktrackr.user.UserService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ProjectServiceImpl implements ProjectService {
    private final ProjectRepository projectRepository;
    private final ProjectTypeQueryRepository projectTypeQueryRepository;
    private final UserService userService;
    private final ProjectMapper projectMapper;

    public ProjectServiceImpl(ProjectRepository projectRepository,
                              ProjectTypeQueryRepository projectTypeQueryRepository,
                              UserService userService,
                              ProjectMapper projectMapper) {
        this.projectRepository = projectRepository;
        this.projectTypeQueryRepository = projectTypeQueryRepository;
        this.userService = userService;
        this.projectMapper = projectMapper;
    }

    @Override
    @Transactional
    public ProjectOverviewDto createProject(UserId userId, ProjectRequest projectRequest){
        UserEntity creator = userService.findUserById(userId);
        ProjectType projectType = findProjectTypeById(projectRequest.getProjectTypeId());

        Project createdProject = Project.create(projectRequest, creator, projectType);
        Project savedProject = projectRepository.save(createdProject);

        savedProject.addMember(creator);

        log.info("Project {} created successfully for user: {}", savedProject.getName(), creator.getUsername());

        return projectMapper.toOverviewDto(savedProject);
    }

    @Override
    public List<ProjectOverviewDto> findProjectsByUserId(UserId userId) {
        List<Project> projectsByUserId = projectRepository.findProjectsByUserId(userId.value());
        return projectsByUserId.stream().map(projectMapper::toOverviewDto).toList();
    }

    private ProjectType findProjectTypeById(int projectTypeId) {
        return projectTypeQueryRepository.findById(projectTypeId)
                .orElseThrow(() -> new ProjectTypeNotFoundException(projectTypeId));
    }
}
