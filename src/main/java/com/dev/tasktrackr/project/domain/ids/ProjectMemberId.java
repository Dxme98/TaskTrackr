package com.dev.tasktrackr.project.domain.ids;

import com.dev.tasktrackr.user.UserId;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
public class ProjectMemberId implements Serializable {

    @Column(name = "user_id", length = 36)
    private String userId;

    @Column(name = "project_id")
    private Long projectId;


    public ProjectMemberId(UserId userId, ProjectId projectId) {
        this.userId = userId.value();
        this.projectId = projectId.value();
    }
}
