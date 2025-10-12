package com.dev.tasktrackr.project.domain.scrum;

import com.dev.tasktrackr.project.api.dtos.request.CreateSprintRequest;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "sprints")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Sprint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private ScrumDetails scrumDetails;

    @Column(nullable = false)
    private String name;

    @Lob
    private String description;

    @Column(name = "goal", nullable = false)
    private String goal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SprintStatus status = SprintStatus.PLANNED;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @OneToMany(mappedBy = "sprint", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<SprintBacklogItem> backlogItems = new HashSet<>();


    public static Sprint create(CreateSprintRequest createSprintRequest, ScrumDetails scrumDetails) {
        return Sprint.builder()
                .backlogItems(null)
                .startDate(createSprintRequest.getStartDate())
                .endDate(createSprintRequest.getEndDate())
                .description(createSprintRequest.getDescription())
                .name(createSprintRequest.getName())
                .status(createSprintRequest.getStatus())
                .goal(createSprintRequest.getGoal())
                .scrumDetails(scrumDetails)
                .build();
    }

    public Set<SprintBacklogItem> addUserStoriesToSprint(List<UserStory> userStories) {
        userStories
                .forEach(story -> backlogItems.add(SprintBacklogItem.create(story, this)));

        return backlogItems;
    }
}
