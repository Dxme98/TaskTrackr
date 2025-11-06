package com.dev.tasktrackr.scrumdetails.api.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActiveSprintData {

    @Schema(
            description = "Gesamtzahl der Stories im aktiven Sprint.",
            example = "15"
    )
    private Long totalStories;

    @Schema(
            description = "Anzahl der bereits abgeschlossenen Stories im Sprint.",
            example = "8"
    )
    private Long finishedStories;

    @Schema(
            description = "Gesamtzahl der Story Points (Aufwand) im aktiven Sprint.",
            example = "50"
    )
    private Long totalPoints;

    @Schema(
            description = "Anzahl der bereits abgeschlossenen Story Points.",
            example = "25"
    )
    private Long finishedPoints;

    @Schema(
            description = "Verbleibende Tage im aktiven Sprint.",
            example = "3"
    )
    private int daysLeft;

    @Schema(
            description = "Durchschnittliche Velocity (abgeschlossene Points pro Tag) in diesem Sprint.",
            example = "7"
    )
    private int averageDailyVelocity;


    public ActiveSprintData(
            Long totalStories,
            Long finishedStories,
            Long totalPoints,
            Long finishedPoints,
            LocalDate startDate,
            LocalDate endDate
    ) {
        this.totalStories = totalStories;

        this.finishedStories = finishedStories;
        this.totalPoints = totalPoints;
        this.finishedPoints = finishedPoints;

        LocalDate today = LocalDate.now();
        long daysLeftLong = ChronoUnit.DAYS.between(today, endDate);
        this.daysLeft = (int) Math.max(0, daysLeftLong);

        int avgVelocity = 0;
        if (today.isAfter(startDate) || today.isEqual(startDate)) {
            long daysPassedLong = ChronoUnit.DAYS.between(startDate, today) + 1;

            if (daysPassedLong > 0 && finishedPoints != null && finishedPoints > 0) {
                avgVelocity = (int) (finishedPoints / daysPassedLong);
            }
        }
        this.averageDailyVelocity = avgVelocity;
    }
}