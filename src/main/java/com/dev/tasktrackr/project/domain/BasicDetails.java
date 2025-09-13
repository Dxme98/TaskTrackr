package com.dev.tasktrackr.project.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "basic_details")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BasicDetails {
    @Id
    private Long id;
    @OneToOne
    @MapsId
    @JoinColumn(name = "project_id")
    private Project project;

    public BasicDetails (Project project) {
        this.project = project;
    }
}
