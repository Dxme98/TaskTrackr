package com.dev.tasktrackr.project.service;

import com.dev.tasktrackr.activity.ProjectActivityEvents;
import com.dev.tasktrackr.project.api.dtos.mapper.SprintMapper;
import com.dev.tasktrackr.project.api.dtos.request.CreateSprintRequest;
import com.dev.tasktrackr.project.api.dtos.response.SprintResponseDto;
import com.dev.tasktrackr.project.domain.Project;
import com.dev.tasktrackr.project.domain.ProjectMember;
import com.dev.tasktrackr.project.domain.scrum.*;
import com.dev.tasktrackr.project.repository.ProjectMemberQueryRepository;
import com.dev.tasktrackr.project.repository.ProjectRepository;
import com.dev.tasktrackr.project.repository.SprintQueryRepository;
import com.dev.tasktrackr.project.repository.UserStoryRepository;
import com.dev.tasktrackr.shared.exception.custom.AccessDeniedExceptions.UserNotProjectMemberException;
import com.dev.tasktrackr.shared.exception.custom.ConflictExceptions.ActiveSprintAlreadyExistsException;
import com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions.NoActiveSprintFoundException;
import com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions.ProjectNotFoundException;
import com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions.SprintNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class SprintServiceImpl implements SprintService{
    private final ProjectRepository projectRepository;
    private final SprintMapper sprintMapper;
    private final SprintQueryRepository sprintQueryRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final ProjectMemberQueryRepository projectMemberQueryRepository;
    private final UserStoryRepository userStoryRepository;


    @Override
    @Transactional
    public SprintResponseDto createSprint(CreateSprintRequest createSprintRequest, Long projectId, String jwtUserId) {
        // load data
        Project project = findProjectById(projectId);
        ScrumDetails scrumDetails = project.getScrumDetails();
        ProjectMember member = findProjectMemberWithPermissionsRolesAndUser(jwtUserId, projectId);

        // check permission
        member.canPlanSprint();

        // create sprint
        Sprint createdSprint = scrumDetails.createSprint(createSprintRequest);

        // load stories for sprint
        List<UserStory> userStories = userStoryRepository.findAllByIdsAndProjectId(createSprintRequest.getUserStoryIds(), projectId);

        // add stories to sprint, and create sprint summary item for each story
        createdSprint.addUserStoriesToSprint(userStories);
        createdSprint.addSprintSummaryItems(userStories);

        // save
        Sprint perisistedSprint = sprintQueryRepository.save(createdSprint);

        // send event
        var event = new ProjectActivityEvents.SprintCreatedEvent(
                projectId, member.getId(), member.getUser().getUsername(), perisistedSprint.getId(), perisistedSprint.getName());
        applicationEventPublisher.publishEvent(event);


        return sprintMapper.toDto(perisistedSprint);
    }

    @Override
    @Transactional(readOnly = true)
    public SprintResponseDto findActiveSprint(Long projectId, String jwtUserId) {

        // check projectmembership
        if(!projectMemberQueryRepository.existsByUserIdAndProjectId(jwtUserId, projectId)) {
            throw new UserNotProjectMemberException(jwtUserId);
        }

        // find active sprint with relevant data
        Sprint activeSprint = sprintQueryRepository.findActiveSprintByProjectId(projectId)
                .orElseThrow(() -> new NoActiveSprintFoundException(projectId));

        return sprintMapper.toDto(activeSprint);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SprintResponseDto> findAllSprintsByProjectIdAndStatus(Long projectId, String jwtUserId, Pageable pageable, SprintStatus status) {
        // check projectmembership
        if(!projectMemberQueryRepository.existsByUserIdAndProjectId(jwtUserId, projectId)) {
            throw new UserNotProjectMemberException(jwtUserId);
        }

        Page<Sprint> sprintPage = sprintQueryRepository.findSprintsByProjectIdAndStatus(projectId, status, pageable);
        return sprintPage.map(sprintMapper::toDto);
    }

    @Override
    @Transactional
    public SprintResponseDto startSprint(Long sprintId, Long projectId, String jwtUserId) {
        // load data
        ScrumDetails scrumDetails = findProjectById(projectId).getScrumDetails();
        ProjectMember member = findProjectMemberWithPermissionsRolesAndUser(jwtUserId, projectId);
        Sprint sprintToStart = sprintQueryRepository.findSprintById(sprintId)
                .orElseThrow(() -> new SprintNotFoundException(sprintId));

        // check permission
        member.canStartSprint();

        // check if active sprint exists (only 1 per project)
        if(sprintQueryRepository.existsActiveSprintForProject(projectId)) {
            throw new ActiveSprintAlreadyExistsException();
        }

        // start sprint
        scrumDetails.startSprint(sprintToStart);

        Sprint perisistedSprint = sprintQueryRepository.save(sprintToStart);

        var event = new ProjectActivityEvents.SprintStartedEvent(
                projectId, member.getId(), member.getUser().getUsername(), perisistedSprint.getId(), perisistedSprint.getName());
        applicationEventPublisher.publishEvent(event);

        return sprintMapper.toDto(perisistedSprint);
    }

    @Override
    @Transactional
    public SprintResponseDto endSprint(Long sprintId, Long projectId, String jwtUserId) {
        Project project = findProjectById(projectId);
        ScrumDetails scrumDetails = project.getScrumDetails();
        ProjectMember member = project.findProjectMember(jwtUserId);

        member.canEndSprint();

        Sprint sprintToEnd = scrumDetails.endSprint(sprintId);

        projectRepository.save(project);

        var event = new ProjectActivityEvents.SprintEndedEvent(
                projectId, member.getId(), member.getUser().getUsername(), sprintToEnd.getId(), sprintToEnd.getName());
        applicationEventPublisher.publishEvent(event);

        return sprintMapper.toDto(sprintToEnd);
    }

    private Project findProjectById(Long projectId) {
        return projectRepository.findById(projectId).orElseThrow(() -> new ProjectNotFoundException(projectId));
    }

    private ProjectMember findProjectMemberWithPermissionsRolesAndUser(String userId, Long projectId) {
        return projectMemberQueryRepository.findProjectMemberWithPermissionsRolesAndUser(projectId, userId)
                .orElseThrow(() -> new UserNotProjectMemberException(userId));
    }

}
