package com.dev.tasktrackr.project.service;

import com.dev.tasktrackr.project.api.dtos.ProjectMapper;
import com.dev.tasktrackr.project.api.dtos.request.ProjectRequest;
import com.dev.tasktrackr.project.api.dtos.response.ProjectOverviewDto;
import com.dev.tasktrackr.project.domain.Project;
import com.dev.tasktrackr.project.domain.ProjectType;
import com.dev.tasktrackr.project.repository.ProjectRepository;
import com.dev.tasktrackr.project.repository.ProjectTypeRepository;
import com.dev.tasktrackr.shared.exception.custom.ProjectTypeNotFoundException;
import com.dev.tasktrackr.user.UserEntity;
import com.dev.tasktrackr.user.UserId;
import com.dev.tasktrackr.user.UserService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Service
@Slf4j
public class ProjectServiceImpl implements ProjectService {
    private final ProjectRepository projectRepository;
    private final ProjectTypeRepository projectTypeRepository; // Direct use of repository, because only use is to get ProjectType
    private final UserService userService;
    private final ProjectMemberService projectMemberService;
    private final ProjectMapper projectMapper;

    public ProjectServiceImpl(ProjectRepository projectRepository,
                              ProjectTypeRepository projectTypeRepository,
                              UserService userService,
                              ProjectMemberService projectMemberService,
                              ProjectMapper projectMapper) {
        this.projectRepository = projectRepository;
        this.projectTypeRepository = projectTypeRepository;
        this.userService = userService;
        this.projectMemberService = projectMemberService;
        this.projectMapper = projectMapper;
    }

    @Override
    @Transactional
    public ProjectOverviewDto createProject(UserId userId, ProjectRequest projectDto){
        // 1. User laden
        UserEntity creator = userService.findUserById(userId);

        // 2. ProjectType laden
        ProjectType projectType = findProjectTypeById(projectDto.getProjectTypeId());

        // 3. Project erstellen
        Project createdProject = Project.builder()
                .name(projectDto.getName())
                .creator(creator)
                .projectType(projectType)
                .projectMembers(new HashSet<>())
                .build();

        // 4. Project speichern für id
        Project savedProject = projectRepository.save(createdProject);

        // 5. ProjectMember erstellen (Creator wird automatisch Member)
        projectMemberService.createProjectMember(creator, savedProject);

        log.info("Project {} created successfully for user: {}", savedProject.getName(), creator.getUsername());


        // 6. Response mit MapStruct erstellen
        return projectMapper.toOverviewDto(savedProject);
    }

    @Override
    public List<ProjectOverviewDto> findProjectsByUserId(UserId userId) {
        List<Project> projectsByUserId = projectRepository.findProjectsByUserId(userId.value());
        return projectsByUserId.stream().map(projectMapper::toOverviewDto).toList();
    }


    private ProjectType findProjectTypeById(int projectTypeId) {
        return projectTypeRepository.findById(projectTypeId)
                .orElseThrow(() -> new ProjectTypeNotFoundException(projectTypeId));
    }
}
