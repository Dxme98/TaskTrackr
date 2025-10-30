package com.dev.tasktrackr.ProjectTests.domain.basic;


import com.dev.tasktrackr.project.domain.*;
import com.dev.tasktrackr.project.domain.basic.BasicDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("BasicDetails Entity Tests")
class BasicDetailsTests {

    @Test
    @DisplayName("Should initialize Information object on creation")
    void shouldInitializeInformationOnCreation() {
        Project mockProject = mock(Project.class);

        BasicDetails details = new BasicDetails(mockProject);

        assertNotNull(details.getInformation(), "Information object should be initialized by the constructor");
    }
}