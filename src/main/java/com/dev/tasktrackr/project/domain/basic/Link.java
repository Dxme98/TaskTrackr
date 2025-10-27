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

    public static Link create(String title, String url, LinkType type, BasicDetails basicDetails) {
        Link link = new Link();
        link.title = title;
        link.url = url;
        link.type = type;
        link.basicDetails = basicDetails;


        return link;
    }
}
