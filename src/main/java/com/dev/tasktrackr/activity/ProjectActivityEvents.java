package com.dev.tasktrackr.activity;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ProjectActivityEvents {

    public record TaskCompletedEvent(
            Long projectId,
            Long actorId,
            String actorName,
            Long taskId,
            String taskTitle
    ) implements ActivityEvent {

        @Override
        public RecordActivityParameter toActivityParameter() {
            return new RecordActivityParameter(
                    this.projectId,
                    ActivityType.TASK_COMPLETED,
                    this.actorId,
                    this.actorName,
                    this.taskId,
                    this.taskTitle,
                    TargetType.TASK,
                    null
            );
        }
    }

    public record TaskCreatedEvent(
            Long projectId,
            Long actorId,
            String actorName,
            Long taskId,
            String taskTitle
    ) implements ActivityEvent {

        @Override
        public RecordActivityParameter toActivityParameter() {
            return new RecordActivityParameter(
                    this.projectId,
                    ActivityType.TASK_CREATED,
                    this.actorId,
                    this.actorName,
                    this.taskId,
                    this.taskTitle,
                    TargetType.TASK,
                    null
            );
        }
    }

    public record TaskDeletedEvent(
            Long projectId,
            Long actorId,
            String actorName,
            Long taskId,
            String taskTitle
    ) implements ActivityEvent {

        @Override
        public RecordActivityParameter toActivityParameter() {
            return new RecordActivityParameter(
                    this.projectId,
                    ActivityType.TASK_DELETED,
                    this.actorId,
                    this.actorName,
                    this.taskId,
                    this.taskTitle,
                    TargetType.TASK,
                    null
            );
        }
    }
}
