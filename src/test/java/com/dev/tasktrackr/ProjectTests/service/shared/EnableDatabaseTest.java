package com.dev.tasktrackr.ProjectTests.service.shared;

import com.dev.tasktrackr.config.JpaAuditingConfig;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)

@EnableJpaRepositories(basePackages = {"com.dev.tasktrackr.project.repository", "com.dev.tasktrackr.user.repository", "com.dev.tasktrackr.activity.repository", "com.dev.tasktrackr.scrumdetails.repository", "com.dev.tasktrackr.basicdetails.repository"})
@EntityScan(basePackages = {"com.dev.tasktrackr.project.domain", "com.dev.tasktrackr.user.domain", "com.dev.tasktrackr.activity.domain", "com.dev.tasktrackr.scrumdetails.domain", "com.dev.tasktrackr.basicdetails.domain"})
@Import(JpaAuditingConfig.class)
@AutoConfigureDataJpa
@AutoConfigureTestEntityManager
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public @interface EnableDatabaseTest {
}
