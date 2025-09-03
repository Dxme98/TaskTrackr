package com.dev.tasktrackr.project.domain;

import com.dev.tasktrackr.project.domain.enums.PermissionName;
import com.dev.tasktrackr.project.domain.enums.ProjectType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "permissions", uniqueConstraints = @UniqueConstraint(
        columnNames = {"name"}))
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(nullable = false, length = 64, unique = true)
    @Enumerated(EnumType.STRING)
    private PermissionName name;
    @Column(nullable = false, length = 32)
    private String type; // ändern im zukunft "COMMON" kein projectType, aber gut für diesen fall
}
