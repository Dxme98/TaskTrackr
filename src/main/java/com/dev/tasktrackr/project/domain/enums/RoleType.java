package com.dev.tasktrackr.project.domain.enums;

public enum RoleType {
    OWNER,      // Projekt-Owner, darf alles, muss immer mindestens 1x existieren, darf nicht gelöscht werden
    BASE,    // Default-Rolle für neue Mitglieder, darf nicht gelöscht werden
    CUSTOM      // Alle frei erstellbaren Rollen
}
