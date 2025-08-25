package com.dev.tasktrackr.project.service;

import com.dev.tasktrackr.project.api.dtos.ProjectDto;
import com.dev.tasktrackr.project.api.dtos.ProjectMapper;
import com.dev.tasktrackr.project.domain.Project;
import com.dev.tasktrackr.project.domain.ProjectMember;
import com.dev.tasktrackr.project.domain.ProjectType;
import com.dev.tasktrackr.project.repository.ProjectRepository;
import com.dev.tasktrackr.project.repository.ProjectTypeRepository;
import com.dev.tasktrackr.shared.exception.custom.ProjectTypeNotFoundException;
import com.dev.tasktrackr.shared.exception.custom.ResourceNotFoundException;
import com.dev.tasktrackr.user.UserEntity;
import com.dev.tasktrackr.user.UserId;
import com.dev.tasktrackr.user.UserService;
import com.dev.tasktrackr.user.UserServiceImpl;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
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
    public ProjectDto.Response createProject(UserId userId, ProjectDto.Request projectDto){
        // 1. User laden
        UserEntity creator = userService.findUserById(userId);

        // 2. ProjectType laden (mit optimierter Validation)
        ProjectType projectType = findProjectTypeById(projectDto.getProjectTypeId());

        // 3. Project erstellen
        Project createdProject = Project.builder()
                .name(projectDto.getName())
                .creator(creator)
                .projectType(projectType)
                .build();

        // 4. Project speichern -- nötig für die ID
        Project savedProject = projectRepository.save(createdProject);

        // 5. ProjectMember erstellen (Creator wird automatisch Member)
        projectMemberService.createProjectMember(creator, savedProject);

        // 6. Response mit MapStruct erstellen
        return projectMapper.toResponse(savedProject);
    }

    private ProjectType findProjectTypeById(int projectTypeId) {
        return projectTypeRepository.findById(projectTypeId)
                .orElseThrow(() -> new ProjectTypeNotFoundException(projectTypeId));
    }
}
