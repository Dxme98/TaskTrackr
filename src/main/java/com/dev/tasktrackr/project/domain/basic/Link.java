package com.dev.tasktrackr.project.domain.basic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "links", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"project_id", "title"})
})
@NoArgsConstructor
@Getter
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
    @JsonIgnore
    private BasicDetails basicDetails;

    public Link(String title, String url, LinkType type, BasicDetails basicDetails) {
        this.title = title;
        this.url = url;
        this.type = type;
        this.basicDetails = basicDetails;
    }
}
