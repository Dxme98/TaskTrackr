package com.dev.tasktrackr.project.api.dtos.response;

import com.dev.tasktrackr.project.domain.enums.ProjectInviteStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Schema(name = "ProjectInviteResponse")
public class ProjectInviteResponseDto {
    @Schema(description = "ID des Invites")
    private Long id;

    @Schema(description = "ID des Absenders", example = "46f6c4df-6303-41c4-b2dc-ec46c154xec4")
    private String senderId;

    @Schema(description = "Username des Absenders", example = "myUsername")
    private String senderUsername;

    @Schema(description = "ID des Empfängers", example = "c1a2b3d4-5678-9101-1121-314151617181")
    private String receiverId;

    @Schema(description = "Username des Empfängers", example = "myUsername")
    private String receiverUsername;

    @Schema(description = "ID des Projekts", example = "5")
    private Long projectId;

    @Schema(description = "Name des Projekts", example = "Projectname")
    private String projectName;

    @Schema(
            description = "Status der Einladung. Mögliche Werte: PENDING, ACCEPTED, DECLINED",
            implementation = InviteStatusResponseDto.class
    )
    private ProjectInviteStatus inviteStatus;

    @Schema(description = "Zeitpunkt der Erstellung", example = "2025-08-28T12:34:56Z")
    private Instant createdAt;
    @Schema(description = "Zeitpunkt des letzten Updates", example = "2025-08-28T12:34:56Z")
    private Instant updatedAt;

}
