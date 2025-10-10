package com.dev.tasktrackr.project.service;

import com.dev.tasktrackr.project.api.dtos.mapper.UserStoryMapper;
import com.dev.tasktrackr.project.api.dtos.request.CreateUserStoryRequest;
import com.dev.tasktrackr.project.api.dtos.response.UserStoryResponseDto;
import com.dev.tasktrackr.project.domain.Project;
import com.dev.tasktrackr.project.domain.scrum.ScrumDetails;
import com.dev.tasktrackr.project.domain.scrum.UserStory;
import com.dev.tasktrackr.project.repository.ProjectRepository;
import com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions.ProjectNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserStoryServiceImpl implements UserStoryService{
    private final ProjectRepository projectRepository;
    private final UserStoryMapper userStoryMapper;

    @Override
    @Transactional
    public UserStoryResponseDto createUserStory(Long projectId, CreateUserStoryRequest createUserStoryRequest, String jwtUserId) {
        Project project = findProjectById(projectId);
        ScrumDetails scrumDetails = project.getScrumDetails();

        UserStory createdUserStory = scrumDetails.createUserStory(createUserStoryRequest);
        projectRepository.save(project);

        UserStory perisistedUserStory = scrumDetails.findUserStoryByTitle(createdUserStory.getTitle());

        return userStoryMapper.toDto(perisistedUserStory);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserStoryResponseDto> getUserStoriesByProjectId(Long projectId, Pageable pageable, String jwtUserId) {
        return null;
    }

    private Project findProjectById(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));
    }
}
