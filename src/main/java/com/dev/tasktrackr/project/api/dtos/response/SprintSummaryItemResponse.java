package com.dev.tasktrackr.project.api.dtos.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SprintSummaryItemResponse {
    private Long id;
    private String title;
    private Integer storyPoints;
    private boolean isCompleted;
}
