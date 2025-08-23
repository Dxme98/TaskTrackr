package com.dev.tasktrackr.project.domain;

import com.dev.tasktrackr.project.domain.ids.ProjectMemberId;
import com.dev.tasktrackr.user.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "project_member",
        indexes = {
                @Index(name = "idx_project_member_project_id", columnList = "project_id")
        }
)
@Getter
@NoArgsConstructor
public class ProjectMember {

    @EmbeddedId
    private ProjectMemberId id;

    @ManyToOne
    @MapsId("userId") // verbindet den FK mit der Embeddable-Spalte
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne
    @MapsId("projectId")
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;


    // Intuitiver Konstruktor
    public ProjectMember(UserEntity user, Project project) {
        this.user = user;
        this.project = project;
        this.id = new ProjectMemberId(user.getId(), project.getId());
    }
}