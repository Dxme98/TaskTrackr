package com.dev.tasktrackr.project.service;

import com.dev.tasktrackr.project.api.dtos.ProjectMapper;
import com.dev.tasktrackr.project.api.dtos.request.ProjectRequest;
import com.dev.tasktrackr.project.api.dtos.response.ProjectOverviewDto;
import com.dev.tasktrackr.project.domain.Project;
import com.dev.tasktrackr.project.domain.ProjectType;
import com.dev.tasktrackr.project.repository.ProjectRepository;
import com.dev.tasktrackr.project.repository.ProjectTypeQueryRepository;
import com.dev.tasktrackr.shared.exception.custom.ProjectTypeNotFoundException;
import com.dev.tasktrackr.shared.exception.custom.UserNotFoundException;
import com.dev.tasktrackr.user.UserEntity;
import com.dev.tasktrackr.user.UserId;
import com.dev.tasktrackr.user.UserRepository;
import com.dev.tasktrackr.user.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {
    private final ProjectRepository projectRepository;
    private final ProjectTypeQueryRepository projectTypeQueryRepository;
    private final UserRepository userRepository;
    private final ProjectMapper projectMapper;

    @Override
    @Transactional
    public ProjectOverviewDto createProject(UserId userId, ProjectRequest projectRequest){
        UserEntity creator = userRepository.findById(userId.value())
                .orElseThrow(() -> new UserNotFoundException(userId.value()));

        ProjectType projectType = findProjectTypeById(projectRequest.getProjectTypeId());

        Project createdProject = Project.create(projectRequest, creator, projectType);
        Project savedProject = projectRepository.save(createdProject); // save before .addMember for ID

        savedProject.addMember(creator);
        projectRepository.save(savedProject);

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
