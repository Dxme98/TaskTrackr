package com.dev.tasktrackr.ProjectTests.service;

import com.dev.tasktrackr.basicdetails.api.dtos.request.CreateTaskRequest;
import com.dev.tasktrackr.basicdetails.domain.Task;
import com.dev.tasktrackr.basicdetails.repository.TaskRepository;
import com.dev.tasktrackr.project.api.dtos.request.ProjectRequest;
import com.dev.tasktrackr.project.domain.*;
import com.dev.tasktrackr.project.domain.enums.Priority;
import com.dev.tasktrackr.project.domain.enums.ProjectType;
import com.dev.tasktrackr.project.repository.*;
import com.dev.tasktrackr.scrumdetails.domain.*;
import com.dev.tasktrackr.scrumdetails.repository.SprintRepository;
import com.dev.tasktrackr.scrumdetails.repository.UserStoryRepository;
import com.dev.tasktrackr.user.domain.UserEntity;
import com.dev.tasktrackr.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Component
public class TestDataFactory {

    @Autowired
    protected UserRepository userRepository;
    @Autowired
    protected ProjectRepository projectRepository;
    @Autowired
    protected ProjectInviteRepository projectInviteRepository;
    @Autowired
    protected ProjectMemberRepository projectMemberRepository;
    @Autowired
    protected TaskRepository taskRepository;
    @Autowired
    protected SprintRepository sprintRepository;
    @Autowired
    protected UserStoryRepository userStoryRepository;

    public UserEntity createTestUser(String id, String username) {
        UserEntity user = UserEntity.builder()
                .id(id)
                .username(username)
                .build();
        return userRepository.save(user);
    }

    public Project createTestProject(String name, ProjectType type, UserEntity creator) {
        ProjectRequest request = new ProjectRequest(name, type);
        Project project = Project.create(request, creator);
        return projectRepository.save(project);
    }

    public ProjectInvite createTestInvite(Project project, UserEntity sender, UserEntity receiver) {
        ProjectInvite invite = ProjectInvite.createInvite(sender, receiver, project);
        return projectInviteRepository.save(invite);
    }

    public ProjectMember createTestMember(Project project, UserEntity user) {
        ProjectRole role = project.getBaseRole();
        ProjectMember member = ProjectMember.createMember(user, project, role);
        return projectMemberRepository.save(member);
    }

    public Task createTestTask(Project project, ProjectMember creator, String title, Set<ProjectMember> assignedMembers) {
        CreateTaskRequest request = CreateTaskRequest.builder()
                .title(title)
                .description("Test Description for " + title)
                .priority(Priority.MEDIUM)
                .build();

        Task task = Task.create(
                request,
                project.getBasicDetails(),
                creator,
                assignedMembers
        );
        return taskRepository.save(task);
    }

    public UserStory createTestUserStory(String title, int storyPoints, StoryStatus status, ScrumDetails scrumDetails) {
        UserStory story = UserStory.builder()
                .title(title)
                .priority(Priority.MEDIUM)
                .storyPoints(storyPoints)
                .status(status)
                .scrumDetails(scrumDetails)
                .build();
        return userStoryRepository.save(story);
    }

    public Sprint createTestSprint(String name, SprintStatus status, ScrumDetails scrumDetails) {
        Sprint sprint = Sprint.builder()
                .name(name)
                .status(status)
                .goal("Test Goal for " + name)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(14))
                .scrumDetails(scrumDetails)
                .sprintSummaryItems(new HashSet<>())
                .backlogItems(new HashSet<>())
                .build();
        return sprintRepository.save(sprint);
    }

}