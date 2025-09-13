package com.dev.tasktrackr.project.domain;

import com.dev.tasktrackr.project.domain.enums.LinkType;
import jakarta.persistence.*;

@Entity
@Table(name = "links", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"project_id", "title"})
})
public class Link {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 80)
    private String title;

    @Column(name = "url", nullable = false, length = 500)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private LinkType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private BasicDetails basicDetails;
}
