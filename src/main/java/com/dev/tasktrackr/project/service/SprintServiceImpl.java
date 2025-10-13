package com.dev.tasktrackr.project.service;

import com.dev.tasktrackr.project.api.dtos.mapper.SprintMapper;
import com.dev.tasktrackr.project.api.dtos.request.CreateSprintRequest;
import com.dev.tasktrackr.project.api.dtos.request.UpdateSprintRequest;
import com.dev.tasktrackr.project.api.dtos.response.SprintResponseDto;
import com.dev.tasktrackr.project.domain.Project;
import com.dev.tasktrackr.project.domain.scrum.ScrumDetails;
import com.dev.tasktrackr.project.domain.scrum.Sprint;
import com.dev.tasktrackr.project.domain.scrum.SprintBacklogItem;
import com.dev.tasktrackr.project.domain.scrum.UserStory;
import com.dev.tasktrackr.project.repository.ProjectRepository;
import com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions.ProjectNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SprintServiceImpl implements SprintService{
    private final ProjectRepository projectRepository;
    private final SprintMapper sprintMapper;

    // Validation, checks usw fehlen

    @Override
    public SprintResponseDto createSprint(CreateSprintRequest createSprintRequest, Long projectId, String jwtUserId) {
        Project project = findProjectById(projectId);
        ScrumDetails scrumDetails = project.getScrumDetails();

        // Erstelle Sprint
        Sprint createdSprint = scrumDetails.createSprint(createSprintRequest);

        // Extrahiere ausgewählte UserStories aus übergebenen IDs
        List<UserStory> userStories = scrumDetails.findUserStoriesByIds(createSprintRequest.getUserStoryIds());

        // Mappe die UserStories zu Sprintbacklogitems und füge sie dem sprint hinzu
        createdSprint.addUserStoriesToSprint(userStories);

        projectRepository.save(project);

        // finde perisisted sprint
        Sprint perisistedSprint = scrumDetails.findSprint(createdSprint);


        return sprintMapper.toDto(perisistedSprint);
    }

    @Override
    public SprintResponseDto findActiveSprint(Long projectId, String jwtUserId) {
        return null;
    }

    @Override
    public Page<SprintResponseDto> findAllSprintsByProjectId(Long projectId, String jwtUserId, Pageable pageable) {
        return null;
    }

    @Override
    public SprintResponseDto editSprint(UpdateSprintRequest updateSprintRequest, Long sprintId, Long projectId, String jwtUserId) {
        Project project = findProjectById(projectId);
        ScrumDetails scrumDetails = project.getScrumDetails();

        // Finde den zu bearbeitenden Sprint über die Domänen-Logik
        Sprint sprintToEdit = scrumDetails.findSprintById(sprintId);

        // Finde die UserStories, die im Sprint sein sollen
        List<UserStory> updatedUserStories = scrumDetails.findUserStoriesByIds(updateSprintRequest.getUserStoryIds());

        // Die Entität selbst ist für die Aktualisierung ihrer Daten verantwortlich
        sprintToEdit.update(updateSprintRequest, updatedUserStories);

        projectRepository.save(project);

        return sprintMapper.toDto(sprintToEdit);
    }

    @Override
    public SprintResponseDto startSprint(Long sprintId, Long projectId, String jwtUserId) {
        return null;
    }

    @Override
    public SprintResponseDto endSprint(Long sprintId, Long projectId, String jwtUserId) {
        return null;
    }

    private Project findProjectById(Long projectId) {
        return projectRepository.findById(projectId).orElseThrow(() -> new ProjectNotFoundException(projectId));
    }

}
