package com.dev.tasktrackr.project;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "project_type")
@Getter
@Setter
public class ProjectType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // SERIAL in Postgres
    private Integer id;

    @Column(length = 32, unique = true, nullable = false)
    private String name;
}
