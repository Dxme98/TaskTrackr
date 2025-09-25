package com.dev.tasktrackr.project.service;

import com.dev.tasktrackr.project.api.dtos.request.CreateLinkRequest;
import com.dev.tasktrackr.project.api.dtos.request.UpdateInformationContentRequest;
import com.dev.tasktrackr.project.domain.BasicDetails;
import com.dev.tasktrackr.project.domain.Information;
import com.dev.tasktrackr.project.domain.Link;
import com.dev.tasktrackr.project.domain.Project;
import com.dev.tasktrackr.project.repository.ProjectRepository;
import com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions.ProjectNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProjectInformationServiceImpl implements ProjectInformationService {
    private final ProjectRepository projectRepository;

    @Override
    @Transactional
    public Information updateContent(Long projectId, String jwtUserId, UpdateInformationContentRequest updateInformationContentRequest) {
        Project project =  findProjectById(projectId);
        BasicDetails basicDetails = project.getBasicDetails();

        Information updatedInformation = basicDetails.updateInformationContent(updateInformationContentRequest);

        projectRepository.save(project);

        return updatedInformation;
    }

    @Override
    @Transactional
    public Link addLink(Long projectId, String jwtUserId, CreateLinkRequest createLinkRequest) {
        Project project = findProjectById(projectId);
        BasicDetails basicDetails = project.getBasicDetails();

        Link addedLink = basicDetails.addLink(createLinkRequest);

        projectRepository.save(project);


        return addedLink;
    }

    @Override
    @Transactional
    public void deleteLink(Long projectId, String jwtUserId, Long linkId) {
        Project project = findProjectById(projectId);
        BasicDetails basicDetails = project.getBasicDetails();

        basicDetails.deleteLink(linkId);
    }

    @Override
    public Set<Link> findLinksByProjectId(Long projectId, String jwtUserId) {
        Project project = findProjectById(projectId);
        BasicDetails basicDetails = project.getBasicDetails();

        return basicDetails.getLinks();
    }

    @Override
    public Information findContentByProjectId(Long projectId, String jwtUserId) {
        Project project = findProjectById(projectId);
        BasicDetails basicDetails = project.getBasicDetails();

        return basicDetails.getInformation();
    }

    private Project findProjectById(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));
    }
}
