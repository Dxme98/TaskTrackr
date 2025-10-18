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

    /**
    public record ProjectDescriptionChangedEvent(Long projectId, Long actorId, String actorName) implements ActivityEvent {
        @Override
        public RecordActivityParameter toActivityParameter() {
            return new RecordActivityParameter(projectId, ActivityType.CHANGED_PROJECT_DESCRIPTION, actorId, actorName, null, null, null, null);
        }
    }



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

    /**
    public record UserLeftProjectEvent(Long projectId, Long actorId, String actorName) implements ActivityEvent {
        @Override
        public RecordActivityParameter toActivityParameter() {
            return new RecordActivityParameter(projectId, ActivityType.USER_LEFT_PROJECT, actorId, actorName, null, null, null, null);
        }
    }

    // --- Link-Events ---

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


    // --- Scrum-Events
    public record UserStoryCreatedEvent(Long projectId, Long actorId, String actorName, Long userStoryId, String userStoryTitle) implements ActivityEvent {
        @Override
        public RecordActivityParameter toActivityParameter() {
            return new RecordActivityParameter(projectId, ActivityType.USER_STORY_CREATED, actorId, actorName, userStoryId, userStoryTitle, TargetType.USER_STORY, null);
        }
    }

    // TODO
    public record UserStoryDeletedEvent(Long projectId, Long actorId, String actorName, Long userStoryId, String userStoryTitle) implements ActivityEvent {
        @Override
        public RecordActivityParameter toActivityParameter() {
            return new RecordActivityParameter(projectId, ActivityType.USER_STORY_DELETED, actorId, actorName, userStoryId, userStoryTitle, TargetType.USER_STORY, null);
        }
    }

    public record UserStoryStatusUpdatedEvent(Long projectId, Long actorId, String actorName, Long userStoryId, String userStoryTitle, String newStatus) implements ActivityEvent {
        @Override
        public RecordActivityParameter toActivityParameter() {
            String contextJson = String.format("{\"storyStatus\":\"%s\"}", newStatus);
            return new RecordActivityParameter(projectId, ActivityType.USER_STORY_STATUS_UPDATED, actorId, actorName, userStoryId, userStoryTitle, TargetType.USER_STORY, contextJson);
        }
    }

    // --- Kommentar- & Blocker-Events ---
    public record CommentCreatedEvent(Long projectId, Long actorId, String actorName, Long storyId, String storyTitle) implements ActivityEvent {
        @Override
        public RecordActivityParameter toActivityParameter() {
            return new RecordActivityParameter(this.projectId, ActivityType.COMMENT_CREATED, this.actorId, this.actorName, this.storyId, this.storyTitle, TargetType.USER_STORY, null);
        }
    }

    public record BlockerCreatedEvent(Long projectId, Long actorId, String actorName, Long storyId, String storyTitle, String blockerName) implements ActivityEvent {
        @Override
        public RecordActivityParameter toActivityParameter() {
            String contextJson = String.format("{\"commentId\":%s}", blockerName);
            return new RecordActivityParameter(this.projectId, ActivityType.BLOCKER_CREATED, this.actorId, this.actorName, this.storyId, this.storyTitle, TargetType.USER_STORY, contextJson);
        }
    }

    public record BlockerResolvedEvent(Long projectId, Long actorId, String actorName, Long storyId, String storyTitle, String blockerName) implements ActivityEvent {
        @Override
        public RecordActivityParameter toActivityParameter() {
            String contextJson = String.format("{\"commentId\":%s}", blockerName);
            return new RecordActivityParameter(this.projectId, ActivityType.BLOCKER_RESOLVED, this.actorId, this.actorName, this.storyId, this.storyTitle, TargetType.USER_STORY, contextJson);
        }
    }

    // --- Sprint-Events ---

    public record SprintCreatedEvent(Long projectId, Long actorId, String actorName, Long sprintId, String sprintName) implements ActivityEvent {
        @Override
        public RecordActivityParameter toActivityParameter() {
            return new RecordActivityParameter(this.projectId, ActivityType.SPRINT_CREATED, this.actorId, this.actorName, this.sprintId, this.sprintName, TargetType.SPRINT, null);
        }
    }

    public record SprintStartedEvent(Long projectId, Long actorId, String actorName, Long sprintId, String sprintName) implements ActivityEvent {
        @Override
        public RecordActivityParameter toActivityParameter() {
            return new RecordActivityParameter(this.projectId, ActivityType.SPRINT_STARTED, this.actorId, this.actorName, this.sprintId, this.sprintName, TargetType.SPRINT, null);
        }
    }

    public record SprintEndedEvent(Long projectId, Long actorId, String actorName, Long sprintId, String sprintName) implements ActivityEvent {
        @Override
        public RecordActivityParameter toActivityParameter() {
            return new RecordActivityParameter(this.projectId, ActivityType.SPRINT_ENDED, this.actorId, this.actorName, this.sprintId, this.sprintName, TargetType.SPRINT, null);
        }
    }

}
