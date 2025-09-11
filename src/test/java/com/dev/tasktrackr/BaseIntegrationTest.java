package com.dev.tasktrackr;

import com.dev.tasktrackr.project.api.dtos.request.ProjectRequest;
import com.dev.tasktrackr.project.domain.Project;
import com.dev.tasktrackr.project.domain.enums.ProjectType;
import com.dev.tasktrackr.project.repository.ProjectRepository;
import com.dev.tasktrackr.user.UserEntity;
import com.dev.tasktrackr.user.UserRepository;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@Import(TestContainersConfiguration.class)
@Transactional
@ActiveProfiles("test")
public class BaseIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = TestContainersConfiguration.postgresContainer();

    @Autowired
    protected EntityManager entityManager;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected ProjectRepository projectRepository;

    /**
     * Standard Test-User erstellen
     */
    protected UserEntity createTestUser(String id, String username) {
        UserEntity user = UserEntity.builder()
                .id(id)
                .username(username)
                .build();
        return userRepository.save(user);
    }

    /**
     * Standard Test-Project erstellen
     */
    protected Project createTestProject(String name, ProjectType type, UserEntity creator) {
        ProjectRequest request = new ProjectRequest(name, type);
        Project project = Project.create(request, creator);
        return projectRepository.save(project);
    }
}
