package com.dev.tasktrackr.project.service;

import com.dev.tasktrackr.activity.ProjectActivityEvents;
import com.dev.tasktrackr.project.api.dtos.mapper.SprintMapper;
import com.dev.tasktrackr.project.api.dtos.request.CreateSprintRequest;
import com.dev.tasktrackr.project.api.dtos.response.SprintResponseDto;
import com.dev.tasktrackr.project.domain.Project;
import com.dev.tasktrackr.project.domain.ProjectMember;
import com.dev.tasktrackr.project.domain.scrum.*;
import com.dev.tasktrackr.project.repository.ProjectRepository;
import com.dev.tasktrackr.project.repository.SprintQueryRepository;
import com.dev.tasktrackr.project.repository.UserStoryQueryRepository;
import com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions.ProjectNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SprintServiceImpl implements SprintService{
    private final ProjectRepository projectRepository;
    private final SprintMapper sprintMapper;
    private final SprintQueryRepository sprintQueryRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    // Validation, checks usw fehlen

    @Override
    @Transactional
    public SprintResponseDto createSprint(CreateSprintRequest createSprintRequest, Long projectId, String jwtUserId) {
        Project project = findProjectById(projectId);
        ScrumDetails scrumDetails = project.getScrumDetails();
        ProjectMember member = project.findProjectMember(jwtUserId);

        member.canPlanSprint();

        // Erstelle Sprint
        Sprint createdSprint = scrumDetails.createSprint(createSprintRequest);

        // Extrahiere ausgewählte UserStories aus übergebenen IDs
        List<UserStory> userStories = scrumDetails.findUserStoriesByIds(createSprintRequest.getUserStoryIds());

        // Mappe die UserStories zu Sprintbacklogitems und füge sie dem sprint hinzu
        createdSprint.addUserStoriesToSprint(userStories);
        createdSprint.addSprintSummaryItems(userStories);

        projectRepository.save(project);

        // finde perisisted sprint
        Sprint perisistedSprint = scrumDetails.findSprint(createdSprint);


        var event = new ProjectActivityEvents.SprintCreatedEvent(
                projectId, member.getId(), member.getUser().getUsername(), perisistedSprint.getId(), perisistedSprint.getName());
        applicationEventPublisher.publishEvent(event);


        return sprintMapper.toDto(perisistedSprint);
    }

    @Override
    @Transactional(readOnly = true)
    public SprintResponseDto findActiveSprint(Long projectId, String jwtUserId) {
        Project project = findProjectById(projectId);
        // Die Logik, den aktiven Sprint zu finden, liegt in ScrumDetails
        Sprint activeSprint = project.getScrumDetails().findActiveSprint();

        return sprintMapper.toDto(activeSprint);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SprintResponseDto> findAllSprintsByProjectIdAndStatus(Long projectId, String jwtUserId, Pageable pageable, SprintStatus status) {
        // Sicherstellen, dass das Projekt existiert, bevor Sprints abgerufen werden
        if (!projectRepository.existsById(projectId)) {
            throw new ProjectNotFoundException(projectId);
        }
        // Paging direkt über ein spezialisiertes Repository für Lese-Performance (CQRS-Gedanke)
        Page<Sprint> sprintPage = sprintQueryRepository.findSprintsByProjectIdAndStatus(projectId, status, pageable);
        return sprintPage.map(sprintMapper::toDto);
    }

    @Override
    @Transactional
    public SprintResponseDto startSprint(Long sprintId, Long projectId, String jwtUserId) {
        Project project = findProjectById(projectId);
        ScrumDetails scrumDetails = project.getScrumDetails();
        ProjectMember member = project.findProjectMember(jwtUserId);

        member.canStartSprint();

        Sprint sprintToStart = scrumDetails.startSprint(sprintId);

        projectRepository.save(project);

        var event = new ProjectActivityEvents.SprintStartedEvent(
                projectId, member.getId(), member.getUser().getUsername(), sprintToStart.getId(), sprintToStart.getName());
        applicationEventPublisher.publishEvent(event);

        return sprintMapper.toDto(sprintToStart);
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

}
