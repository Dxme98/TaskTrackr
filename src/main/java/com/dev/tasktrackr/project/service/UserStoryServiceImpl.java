package com.dev.tasktrackr.project.service;

import com.dev.tasktrackr.activity.ProjectActivityEvents;
import com.dev.tasktrackr.project.api.dtos.mapper.UserStoryMapper;
import com.dev.tasktrackr.project.api.dtos.request.CreateUserStoryRequest;
import com.dev.tasktrackr.project.api.dtos.response.UserStoryResponseDto;
import com.dev.tasktrackr.project.domain.Project;
import com.dev.tasktrackr.project.domain.ProjectMember;
import com.dev.tasktrackr.project.domain.scrum.ScrumDetails;
import com.dev.tasktrackr.project.domain.scrum.UserStory;
import com.dev.tasktrackr.project.repository.ProjectRepository;
import com.dev.tasktrackr.project.repository.UserStoryQueryRepository;
import com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions.ProjectNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserStoryServiceImpl implements UserStoryService{
    private final ProjectRepository projectRepository;
    private final UserStoryMapper userStoryMapper;
    private final UserStoryQueryRepository userStoryQueryRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    // TODO USERSTORY DELETION + EVENT

    @Override
    @Transactional
    public UserStoryResponseDto createUserStory(Long projectId, CreateUserStoryRequest createUserStoryRequest, String jwtUserId) {
        Project project = findProjectById(projectId);
        ScrumDetails scrumDetails = project.getScrumDetails();
        ProjectMember member = project.findProjectMember(jwtUserId);

        member.canCreateUserStory();

        UserStory createdUserStory = scrumDetails.createUserStory(createUserStoryRequest);
        projectRepository.save(project);

        UserStory perisistedUserStory = scrumDetails.findUserStoryByTitle(createdUserStory.getTitle());

        var event = new ProjectActivityEvents.UserStoryCreatedEvent(
                projectId, member.getId(), member.getUser().getUsername(),
                perisistedUserStory.getId(), perisistedUserStory.getTitle());
        applicationEventPublisher.publishEvent(event);

        return userStoryMapper.toDto(perisistedUserStory);
    }

    @Override
    @Transactional
    public void deleteUserStory(Long projectId, Long userStoryId, String jwtUserId) {
        Project project = findProjectById(projectId);
        ScrumDetails scrumDetails = project.getScrumDetails();
        ProjectMember member = project.findProjectMember(jwtUserId);

        member.canDeleteUserStory();

        UserStory deletedUserStory = scrumDetails.deleteUserStory(userStoryId);

        var event = new ProjectActivityEvents.UserStoryDeletedEvent(
                projectId, member.getId(), member.getUser().getUsername(),
                deletedUserStory.getId(), deletedUserStory.getTitle());
        applicationEventPublisher.publishEvent(event);

        projectRepository.save(project);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserStoryResponseDto> getUserStoriesByProjectId(Long projectId, Pageable pageable, String jwtUserId) {
        Project project = findProjectById(projectId);
        project.isProjectMember(jwtUserId);

        Page<UserStory> userStories = userStoryQueryRepository.findUserStoriesByProjectId(projectId, pageable);
        return userStories.map(userStoryMapper::toDto);
    }

    private Project findProjectById(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));
    }
}
