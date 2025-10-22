package com.dev.tasktrackr.project.service;

import com.dev.tasktrackr.activity.ProjectActivityEvents;
import com.dev.tasktrackr.project.api.dtos.mapper.UserStoryMapper;
import com.dev.tasktrackr.project.api.dtos.request.CreateUserStoryRequest;
import com.dev.tasktrackr.project.api.dtos.response.UserStoryResponseDto;
import com.dev.tasktrackr.project.domain.ProjectMember;
import com.dev.tasktrackr.project.domain.scrum.ScrumDetails;
import com.dev.tasktrackr.project.domain.scrum.UserStory;
import com.dev.tasktrackr.project.repository.UserStoryRepository;
import com.dev.tasktrackr.shared.exception.custom.ConflictExceptions.UserStoryTitleAlreadyExistsException;
import com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions.UserStoryNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserStoryServiceImpl implements UserStoryService{
    private final UserStoryMapper userStoryMapper;
    private final UserStoryRepository userStoryRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final ProjectAccessService projectAccessService;


    @Override
    @Transactional
    public UserStoryResponseDto createUserStory(Long projectId, CreateUserStoryRequest createUserStoryRequest, String jwtUserId) {
        // load
        ScrumDetails scrumDetails = projectAccessService.findProjectById(projectId).getScrumDetails();
        ProjectMember member = projectAccessService.findProjectMemberWithPermissionsRolesAndUser(jwtUserId, projectId);

        // check permission
        member.canCreateUserStory();

        // check if valid
        checkForUniqueUserStoryTitle(createUserStoryRequest.getTitle(), projectId);

        // create and save
        UserStory createdUserStory = scrumDetails.createUserStory(createUserStoryRequest);
        UserStory perisistedUserStory = userStoryRepository.save(createdUserStory);

        // send event
        var event = new ProjectActivityEvents.UserStoryCreatedEvent(
                projectId, member.getId(), member.getUser().getUsername(),
                perisistedUserStory.getId(), perisistedUserStory.getTitle());
        applicationEventPublisher.publishEvent(event);

        return userStoryMapper.toDto(perisistedUserStory);
    }

    @Override
    @Transactional
    public void deleteUserStory(Long projectId, Long userStoryId, String jwtUserId) {
        ProjectMember member = projectAccessService.findProjectMemberWithPermissionsRolesAndUser(jwtUserId, projectId);
        UserStory userStoryToDelete = findUserStoryById(userStoryId);

        member.canDeleteUserStory();
        userStoryRepository.delete(userStoryToDelete);

        var event = new ProjectActivityEvents.UserStoryDeletedEvent(
                projectId, member.getId(), member.getUser().getUsername(),
                userStoryToDelete.getId(), userStoryToDelete.getTitle());
        applicationEventPublisher.publishEvent(event);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserStoryResponseDto> getUserStoriesByProjectId(Long projectId, Pageable pageable, String jwtUserId) {

        projectAccessService.checkProjectMemberShip(projectId, jwtUserId);

        return userStoryRepository.findUserStoriesByScrumDetailsId(projectId, pageable);
    }


    /** --- Helper Methods ---  */

    private UserStory findUserStoryById(Long userStoryId) {
        return userStoryRepository.findById(userStoryId)
                .orElseThrow(() -> new UserStoryNotFoundException(userStoryId));
    }

    private void checkForUniqueUserStoryTitle(String title, Long projectId) {
        if(userStoryRepository.existsByTitleAndScrumDetailsId(title, projectId)) {
            throw new UserStoryTitleAlreadyExistsException(title);
        }
    }
}
