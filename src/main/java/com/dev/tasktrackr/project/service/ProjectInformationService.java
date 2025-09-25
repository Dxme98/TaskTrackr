package com.dev.tasktrackr.project.service;

import com.dev.tasktrackr.project.api.dtos.request.CreateLinkRequest;
import com.dev.tasktrackr.project.api.dtos.request.UpdateInformationContentRequest;
import com.dev.tasktrackr.project.domain.Information;
import com.dev.tasktrackr.project.domain.Link;

import java.util.Set;

public interface ProjectInformationService {
    Information updateContent(Long projectId, String jwtUserId, UpdateInformationContentRequest updateInformationContentRequest);
    Link addLink(Long projectId, String jwtUserId, CreateLinkRequest createLinkRequest);
    void deleteLink(Long projectId, String jwtUserId, Long linkId);
    Set<Link> findLinksByProjectId(Long projectId, String jwtUserId);
    Information findContentByProjectId(Long projectId, String jwtUserId);
}
