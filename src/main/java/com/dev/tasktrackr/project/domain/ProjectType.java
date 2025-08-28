package com.dev.tasktrackr.project.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "project_type")
@Getter
public class ProjectType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // SERIAL in Postgres
    private Integer id;

    @Column(length = 32, unique = true, nullable = false)
    private String name;

    // Statische Konstanten für Typsicherheit
    public static final int BASIC_ID = 1;
    public static final int SCRUM_ID = 2;

    public static final String BASIC_NAME = "BASIC";
    public static final String SCRUM_NAME = "SCRUM";
}
