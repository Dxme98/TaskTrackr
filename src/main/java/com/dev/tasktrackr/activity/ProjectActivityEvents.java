package com.dev.tasktrackr.activity;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class ProjectActivityEvents {

    // === Task Events ===

    public record TaskCompletedEvent(Long projectId, Long actorId, String actorName, Long taskId, String taskTitle) implements ActivityEvent {
        @Override
        public RecordActivityParameter toActivityParameter() {
            return new RecordActivityParameter(this.projectId, ActivityType.TASK_COMPLETED, this.actorId, this.actorName, this.taskId, this.taskTitle, TargetType.TASK, null);
        }
    }

    public record TaskCreatedEvent(Long projectId, Long actorId, String actorName, Long taskId, String taskTitle) implements ActivityEvent {
        @Override
        public RecordActivityParameter toActivityParameter() {
            return new RecordActivityParameter(this.projectId, ActivityType.TASK_CREATED, this.actorId, this.actorName, this.taskId, this.taskTitle, TargetType.TASK, null);
        }
    }

    public record TaskDeletedEvent(Long projectId, Long actorId, String actorName, Long taskId, String taskTitle) implements ActivityEvent {
        @Override
        public RecordActivityParameter toActivityParameter() {
            return new RecordActivityParameter(this.projectId, ActivityType.TASK_DELETED, this.actorId, this.actorName, this.taskId, this.taskTitle, TargetType.TASK, null);
        }
    }

    // --- Projekt-Events ---

    public record ProjectNameChangedEvent(Long projectId, Long actorId, String actorName, String oldName, String newName) implements ActivityEvent {
        @Override
        public RecordActivityParameter toActivityParameter() {
            String contextJson = String.format("{\"oldName\":\"%s\",\"newName\":\"%s\"}", oldName, newName);
            return new RecordActivityParameter(projectId, ActivityType.CHANGED_PROJECT_NAME, actorId, actorName, projectId, null, null, contextJson);
        }
    }

    public record ProjectDescriptionChangedEvent(Long projectId, Long actorId, String actorName) implements ActivityEvent {
        @Override
        public RecordActivityParameter toActivityParameter() {
            return new RecordActivityParameter(projectId, ActivityType.CHANGED_PROJECT_DESCRIPTION, actorId, actorName, null, null, null, null);
        }
    }

    /**
    public record ProjectInfoUpdatedEvent(Long projectId, Long actorId, String actorName) implements ActivityEvent {
        @Override
        public RecordActivityParameter toActivityParameter() {
            return new RecordActivityParameter(projectId, ActivityType.PROJECT_INFO_UPDATED, actorId, actorName, null, null, null, null);
        }
    }
     */

    // --- Benutzer-Events ---

    public record UserJoinedProjectEvent(Long projectId, Long actorId, String actorName) implements ActivityEvent {
        @Override
        public RecordActivityParameter toActivityParameter() {
            return new RecordActivityParameter(projectId, ActivityType.USER_JOINED_PROJECT, actorId, actorName, null, null, null, null);
        }
    }

    public record UserRemovedFromProjectEvent(Long projectId, Long actorId, String actorName, Long targetUserId, String targetUserName) implements ActivityEvent {
        @Override
        public RecordActivityParameter toActivityParameter() {
            return new RecordActivityParameter(projectId, ActivityType.USER_REMOVED, actorId, actorName, targetUserId, targetUserName, TargetType.PROJECT_MEMBER, null);
        }
    }

    public record UserLeftProjectEvent(Long projectId, Long actorId, String actorName) implements ActivityEvent {
        @Override
        public RecordActivityParameter toActivityParameter() {
            return new RecordActivityParameter(projectId, ActivityType.USER_LEFT_PROJECT, actorId, actorName, null, null, null, null);
        }
    }

    // --- Link-Events ---

    /**
    public record LinkAddedEvent(Long projectId, Long actorId, String actorName, Long linkId, String linkName) implements ActivityEvent {
        @Override
        public RecordActivityParameter toActivityParameter() {
            return new RecordActivityParameter(projectId, ActivityType.LINK_ADDED, actorId, actorName, linkId, linkName, TargetType.PROJECT, null);
        }
    }
     */

    // --- Rollen-Events ---

    public record RoleCreatedEvent(Long projectId, Long actorId, String actorName, Long roleId, String roleName) implements ActivityEvent {
        @Override
        public RecordActivityParameter toActivityParameter() {
            return new RecordActivityParameter(projectId, ActivityType.ROLE_CREATED, actorId, actorName, roleId, roleName, TargetType.ROLE, null);
        }
    }

    public record RoleAssignedEvent(Long projectId, Long actorId, String actorName, Long targetUserId, String targetUserName, String assignedRoleName) implements ActivityEvent {
        @Override
        public RecordActivityParameter toActivityParameter() {
            String contextJson = String.format("{\"roleName\":\"%s\"}", assignedRoleName);
            return new RecordActivityParameter(projectId, ActivityType.ROLE_ASSIGNED, actorId, actorName, targetUserId, targetUserName, TargetType.PROJECT_MEMBER, contextJson);
        }
    }

    public record RoleDeletedEvent(Long projectId, Long actorId, String actorName, Long roleId, String roleName) implements ActivityEvent {
        @Override
        public RecordActivityParameter toActivityParameter() {
            return new RecordActivityParameter(projectId, ActivityType.ROLE_DELETED, actorId, actorName, roleId, roleName, TargetType.ROLE, null);
        }
    }
}
