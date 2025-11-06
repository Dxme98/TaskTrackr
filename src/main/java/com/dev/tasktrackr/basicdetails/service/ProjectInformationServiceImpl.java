package com.dev.tasktrackr.basicdetails.service;

import com.dev.tasktrackr.basicdetails.api.dtos.request.CreateLinkRequest;
import com.dev.tasktrackr.basicdetails.api.dtos.request.UpdateInformationContentRequest;
import com.dev.tasktrackr.project.domain.*;
import com.dev.tasktrackr.basicdetails.domain.BasicDetails;
import com.dev.tasktrackr.basicdetails.domain.Information;
import com.dev.tasktrackr.basicdetails.domain.Link;
import com.dev.tasktrackr.basicdetails.repository.LinkRepository;
import com.dev.tasktrackr.basicdetails.repository.ProjectInformationRepository;
import com.dev.tasktrackr.project.service.ProjectAccessService;
import com.dev.tasktrackr.shared.exception.custom.ConflictExceptions.LinkTitleAlreadyExistsException;
import com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions.LinkNotFoundException;
import com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions.ProjectNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProjectInformationServiceImpl implements ProjectInformationService {
    private final ProjectInformationRepository projectInformationRepository;
    private final ProjectAccessService projectAccessService;
    private final LinkRepository linkRepository;

    @Override
    @Transactional
    public Information updateContent(Long projectId, String jwtUserId, UpdateInformationContentRequest updateInformationContentRequest) {
        ProjectMember member = projectAccessService.findProjectMemberWithPermissionsRolesAndUser(jwtUserId, projectId);
        Information projectInformation = findContentByProjectId(projectId);

        member.canEditInformation();
        projectInformation.updateContent(updateInformationContentRequest.getContent());

        return projectInformationRepository.save(projectInformation);
    }

    @Override
    @Transactional
    public Link addLink(Long projectId, String jwtUserId, CreateLinkRequest createLinkRequest) {
        ProjectMember member = projectAccessService.findProjectMemberWithPermissionsRolesAndUser(jwtUserId, projectId);
        BasicDetails basicDetails = projectAccessService.findProjectById(projectId).getBasicDetails();

        member.canEditInformation();

        checkForDuplicateLinkTitle(projectId, createLinkRequest.getTitle());

        Link createdLink = Link.create(createLinkRequest.getTitle(), createLinkRequest.getUrl(), createLinkRequest.getLinkType(), basicDetails);

        return linkRepository.save(createdLink);
    }

    @Override
    @Transactional
    public void deleteLink(Long projectId, String jwtUserId, Long linkId) {
        ProjectMember member = projectAccessService.findProjectMemberWithPermissionsRolesAndUser(jwtUserId, projectId);
        Link linkToDelete = findLinkById(linkId);

        member.canEditInformation();

        linkRepository.delete(linkToDelete);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Link> findLinksByProjectId(Long projectId, String jwtUserId) {
        projectAccessService.checkProjectMemberShip(projectId, jwtUserId);

        return linkRepository.findAllByBasicDetailsId(projectId);
    }


    @Override
    @Transactional(readOnly = true)
    public Information findContentByProjectId(Long projectId, String jwtUserId) {
        projectAccessService.checkProjectMemberShip(projectId, jwtUserId);
        return findContentByProjectId(projectId);
    }

    /** Helper Methods*/
    private Information findContentByProjectId(Long projectId) {
        return projectInformationRepository.findByBasicDetailsId(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));
    }


    private Link findLinkById(Long linkId) {
        return linkRepository.findById(linkId).orElseThrow(() -> new LinkNotFoundException(linkId));
    }

    void checkForDuplicateLinkTitle(Long projectId, String title) {
        if(linkRepository.existsByBasicDetailsIdAndTitle(projectId, title)) throw new LinkTitleAlreadyExistsException(title);
    }
}