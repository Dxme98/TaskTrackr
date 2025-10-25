package com.dev.tasktrackr.project.service;

import com.dev.tasktrackr.project.api.dtos.mapper.ProjectMapper;
import com.dev.tasktrackr.project.api.dtos.request.ProjectRequest;
import com.dev.tasktrackr.project.api.dtos.response.ProjectOverviewDto;
import com.dev.tasktrackr.project.domain.Project;
import com.dev.tasktrackr.project.repository.ProjectRepository;
import com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions.ProjectNotFoundException;
import com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions.UserNotFoundException;
import com.dev.tasktrackr.user.UserEntity;
import com.dev.tasktrackr.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMapper projectMapper;
    private final ProjectAccessService projectAccessService;

    @Override
    @Transactional
    public ProjectOverviewDto createProject(String userId, ProjectRequest projectRequest){
        UserEntity creator = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        Project createdProject = Project.create(projectRequest, creator);
        Project savedProject = projectRepository.save(createdProject);

        log.info("Project {} created successfully for user: {}", savedProject.getName(), creator.getUsername());

        return projectMapper.toOverviewDto(savedProject);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectOverviewDto> findProjectsByUserId(String userId, PageRequest pageRequest) {
        Page<Project> projectsByUserId = projectRepository.findProjectsByUserId(userId, pageRequest);
        return projectsByUserId.map(projectMapper::toOverviewDto);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectOverviewDto getProjectDetails(Long projectId, String userId) {
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new ProjectNotFoundException(projectId));

        projectAccessService.checkProjectMemberShip(projectId, userId);

        return projectMapper.toOverviewDto(project);
    }
}
