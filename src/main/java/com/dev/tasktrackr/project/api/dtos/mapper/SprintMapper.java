package com.dev.tasktrackr.project.api.dtos.mapper;

import com.dev.tasktrackr.project.api.dtos.response.SprintResponseDto;
import com.dev.tasktrackr.project.api.dtos.response.SprintSummaryItemResponse;
import com.dev.tasktrackr.project.domain.scrum.Sprint;
import com.dev.tasktrackr.project.domain.scrum.SprintSummaryItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SprintMapper {
    private final SprintSummaryItemMapper sprintSummaryItemMapper;

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

        Set<SprintSummaryItem> sprintSummaryItems = sprint.getSprintSummaryItems();

        // 2. Abgeleitete Felder berechnen und Backlog-Items mappen
        if (sprintSummaryItems == null || sprintSummaryItems.isEmpty()) {
            // Standardwerte für einen leeren Sprint
            dto.setTotalStories(0);
            dto.setCompletedStories(0);
            dto.setTotalStoryPoints(0);
            dto.setCompletedStoryPoints(0);
            dto.setProgressPercentage(0.0);
            dto.setSprintSummaryItems(Collections.emptySet());
        } else {
            // Gesamtanzahl der Story Points berechnen
            int totalStoryPoints = sprintSummaryItems.stream()
                    .mapToInt(SprintSummaryItem::getStoryPoints)
                    .sum();

            // Abgeschlossene Items filtern (Annahme: UserStory hat einen Status)
            Set<SprintSummaryItem> completedItems = sprintSummaryItems.stream()
                    .filter(SprintSummaryItem::isCompleted)
                    .collect(Collectors.toSet());

            // Story Points der abgeschlossenen Items berechnen
            int completedStoryPoints = completedItems.stream()
                    .mapToInt(SprintSummaryItem::getStoryPoints)
                    .sum();

            // Fortschritt in Prozent berechnen (sicherstellen, dass nicht durch 0 geteilt wird)
            double progressPercentage = (totalStoryPoints > 0)
                    ? ((double) completedStoryPoints / totalStoryPoints) * 100.0
                    : 0.0;

            // Die Liste der Backlog Items ebenfalls in DTOs umwandeln
            Set<SprintSummaryItemResponse> itemDtos = sprintSummaryItems.stream()
                    .map(sprintSummaryItemMapper::toDto) // Verwendung des injizierten Mappers
                    .collect(Collectors.toSet());

            // Berechnete Werte im DTO setzen
            dto.setTotalStories(sprintSummaryItems.size());
            dto.setCompletedStories(completedItems.size());
            dto.setTotalStoryPoints(totalStoryPoints);
            dto.setCompletedStoryPoints(completedStoryPoints);
            dto.setProgressPercentage(Math.round(progressPercentage * 100.0) / 100.0); // Auf 2 Nachkommastellen runden
            dto.setSprintSummaryItems(itemDtos);
        }

        return dto;
    }
}
