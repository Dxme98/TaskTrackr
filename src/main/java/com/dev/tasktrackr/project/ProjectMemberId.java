package com.dev.tasktrackr.project;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectMemberId implements Serializable {

    @Column(name = "user_id", length = 36)
    private String userId;

    @Column(name = "project_id")
    private Long projectId;
}
