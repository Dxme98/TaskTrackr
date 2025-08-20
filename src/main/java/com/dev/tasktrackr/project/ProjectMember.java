package com.dev.tasktrackr.project;

import com.dev.tasktrackr.user.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.apache.catalina.User;

@Entity
@Table(name = "project_member")
@Getter
@Setter
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
}