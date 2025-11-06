package com.dev.tasktrackr.basicdetails.domain;

import com.dev.tasktrackr.project.domain.Project;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;


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
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "basicDetails")
    private Set<Task> tasks = new HashSet<>();
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "basicDetails")
    private Set<Link> links = new HashSet<>();
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "basicDetails")
    private Information information;

    public BasicDetails (Project project) {
        this.project = project;
        this.information = new Information(this);
    }
}
