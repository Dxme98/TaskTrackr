package com.dev.tasktrackr.project.domain;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "invite_status")
@Getter
public class ProjectInviteStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 32, unique = true, nullable = false)
    private String name;

    // Statische Konstanten für bessere Typsicherheit
    public static final int PENDING_ID = 1;
    public static final int ACCEPTED_ID = 2;
    public static final int DECLINED_ID = 3;

    public static final String PENDING_NAME = "PENDING";
    public static final String ACCEPTED_NAME = "ACCEPTED";
    public static final String DECLINED_NAME = "DECLINED";
}
