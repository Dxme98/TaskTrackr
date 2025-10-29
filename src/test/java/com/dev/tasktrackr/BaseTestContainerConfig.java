package com.dev.tasktrackr;

import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@Transactional
@ActiveProfiles("test")
@Import(TestContainersConfiguration.class) // Importiert die Container-Definition
public abstract class BaseTestContainerConfig {

    @Container
    static PostgreSQLContainer<?> postgres = TestContainersConfiguration.postgresContainer();
}
