package com.dev.tasktrackr.project.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "information")
@NoArgsConstructor
@Getter
public class Information {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @JsonIgnore
    private BasicDetails basicDetails;

    @Column(name = "content", nullable = false, length = 100000)
    private String content;

    public Information(BasicDetails basicDetails) {
        this.basicDetails = basicDetails;
        this.content = "Hier könnten wichtige Projectinformationen, Meilensteine oder Ankündigungen stehen." +
                " Klicken Sie auf 'Bearbeiten', um diesen Text zu ändern.";
    }

    public void updateContent(String updatedContent) {
        this.content = updatedContent;
    }
}
