package com.dev.tasktrackr.project.service;

import static com.dev.tasktrackr.activity.ProjectActivityEvents.UserRemovedFromProjectEvent;

import com.dev.tasktrackr.project.api.dtos.response.ProjectMemberDto;
import com.dev.tasktrackr.project.api.dtos.mapper.ProjectMemberMapper;
import com.dev.tasktrackr.project.domain.ProjectInvite;
import com.dev.tasktrackr.project.domain.ProjectMember;
import com.dev.tasktrackr.project.repository.ProjectInviteQueryRepository;
import com.dev.tasktrackr.project.repository.ProjectMemberQueryRepository;
import com.dev.tasktrackr.shared.exception.custom.BadRequestExceptions.InvalidProjectMemberDeletion;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProjectMemberServiceImpl implements ProjectMemberService{
    private final ProjectMemberQueryRepository projectMemberQueryRepository;
    private final ProjectMemberMapper projectMemberMapper;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final ProjectAccessService projectAccessService;  // project with invites
    private final ProjectInviteQueryRepository projectInviteQueryRepository;


    @Override
    @Transactional
    public void removeMemberFromProject(String jwtUserId, Long projectId, Long memberId) {
        ProjectMember member = projectAccessService.findProjectMemberWithPermissionsRolesAndUser(jwtUserId, projectId);
        ProjectMember memberToRemove =  projectAccessService.findProjectMember(memberId, projectId); // needs to load with role
        ProjectInvite invite = findProjectInviteByProjectIdAndReceiverId(projectId, memberToRemove.getUser().getId());

        member.canRemoveUser();
        memberToRemove.canBeRemovedFromProject(); // Members with OWNER-Role can't be removed
        if(memberToRemove.getUser().getId().equals(jwtUserId)) throw new InvalidProjectMemberDeletion(); // self remove should not be possible

        // Only delete if invite exists (ProjectCreator does not have invite)
        if(invite != null) projectInviteQueryRepository.delete(invite); // Delete invite to enable reinvite


        projectMemberQueryRepository.delete(memberToRemove);


        var event = new UserRemovedFromProjectEvent(projectId, member.getId(), member.getUser().getUsername(), memberId, memberToRemove.getUser().getUsername());
        applicationEventPublisher.publishEvent(event);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectMemberDto> getAllProjectMembers(String jwtUserId, Long projectId, Pageable pageable) {
        projectAccessService.checkProjectMemberShip(projectId, jwtUserId);

        Page<ProjectMember> projectMembers = projectMemberQueryRepository.findAllProjectMembersByProjectId(projectId, pageable);

        return projectMembers.map(projectMemberMapper::toResponse);
    }

    /** Helper Methods */
    ProjectInvite findProjectInviteByProjectIdAndReceiverId(Long projectId, String receiverId) {
        return projectInviteQueryRepository.findProjectInviteByProjectIdAndReceiverId(projectId, receiverId);
    }
}
