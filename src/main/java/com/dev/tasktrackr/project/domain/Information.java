package com.dev.tasktrackr.project.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "information")
public class Information {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private BasicDetails basicDetails;

    @Column(name = "content", nullable = false, length = 100000)
    private String content;
}
