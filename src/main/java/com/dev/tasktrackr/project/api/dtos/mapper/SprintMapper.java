package com.dev.tasktrackr.project.api.dtos.mapper;

import com.dev.tasktrackr.project.api.dtos.response.SprintBacklogItemResponse;
import com.dev.tasktrackr.project.api.dtos.response.SprintResponseDto;
import com.dev.tasktrackr.project.domain.scrum.Sprint;
import com.dev.tasktrackr.project.domain.scrum.SprintBacklogItem;
import com.dev.tasktrackr.project.domain.scrum.StoryStatus;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SprintMapper {
    private final SprintBacklogItemMapper sprintBacklogItemMapper;

    /**
     * Wandelt eine Sprint-Entität in ein SprintResponseDto um.
     *
     * @param sprint Die Sprint-Entität, die umgewandelt werden soll.
     * @return Das resultierende SprintResponseDto oder null, wenn die Eingabe null ist.
     */
    public SprintResponseDto toDto(Sprint sprint) {
        if (sprint == null) {
            return null;
        }

        SprintResponseDto dto = new SprintResponseDto();

        // 1. Direkte Felder mappen
        dto.setId(sprint.getId());
        dto.setName(sprint.getName());
        dto.setGoal(sprint.getGoal());
        dto.setDescription(sprint.getDescription());
        dto.setStatus(sprint.getStatus());
        dto.setStartDate(sprint.getStartDate());
        dto.setEndDate(sprint.getEndDate());

        Set<SprintBacklogItem> backlogItems = sprint.getBacklogItems();

        // 2. Abgeleitete Felder berechnen und Backlog-Items mappen
        if (backlogItems == null || backlogItems.isEmpty()) {
            // Standardwerte für einen leeren Sprint
            dto.setTotalStories(0);
            dto.setCompletedStories(0);
            dto.setTotalStoryPoints(0);
            dto.setCompletedStoryPoints(0);
            dto.setProgressPercentage(0.0);
            dto.setSprintBacklogItems(Collections.emptySet());
        } else {
            // Gesamtanzahl der Story Points berechnen
            int totalStoryPoints = backlogItems.stream()
                    .mapToInt(item -> item.getUserStory().getStoryPoints()) // Annahme: Methode existiert
                    .sum();

            // Abgeschlossene Items filtern (Annahme: UserStory hat einen Status)
            Set<SprintBacklogItem> completedItems = backlogItems.stream()
                    .filter(item -> item.getUserStory().getStatus().equals(StoryStatus.DONE)) // Annahme
                    .collect(Collectors.toSet());

            // Story Points der abgeschlossenen Items berechnen
            int completedStoryPoints = completedItems.stream()
                    .mapToInt(item -> item.getUserStory().getStoryPoints()) // Annahme
                    .sum();

            // Fortschritt in Prozent berechnen (sicherstellen, dass nicht durch 0 geteilt wird)
            double progressPercentage = (totalStoryPoints > 0)
                    ? ((double) completedStoryPoints / totalStoryPoints) * 100.0
                    : 0.0;

            // Die Liste der Backlog Items ebenfalls in DTOs umwandeln
            Set<SprintBacklogItemResponse> itemDtos = backlogItems.stream()
                    .map(sprintBacklogItemMapper::toDto) // Verwendung des injizierten Mappers
                    .collect(Collectors.toSet());

            // Berechnete Werte im DTO setzen
            dto.setTotalStories(backlogItems.size());
            dto.setCompletedStories(completedItems.size());
            dto.setTotalStoryPoints(totalStoryPoints);
            dto.setCompletedStoryPoints(completedStoryPoints);
            dto.setProgressPercentage(Math.round(progressPercentage * 100.0) / 100.0); // Auf 2 Nachkommastellen runden
            dto.setSprintBacklogItems(itemDtos);
        }

        return dto;
    }
}
