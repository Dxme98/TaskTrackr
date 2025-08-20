package com.dev.tasktrackr.project;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "invite_status")
@Getter
@Setter
public class ProjectInviteStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 32, unique = true, nullable = false)
    private String name;
}
