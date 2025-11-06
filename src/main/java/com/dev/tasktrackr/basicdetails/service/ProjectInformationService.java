package com.dev.tasktrackr.basicdetails.service;

import com.dev.tasktrackr.basicdetails.api.dtos.request.CreateLinkRequest;
import com.dev.tasktrackr.basicdetails.api.dtos.request.UpdateInformationContentRequest;
import com.dev.tasktrackr.basicdetails.domain.Information;
import com.dev.tasktrackr.basicdetails.domain.Link;

import java.util.Set;

public interface ProjectInformationService {
    Information updateContent(Long projectId, String jwtUserId, UpdateInformationContentRequest updateInformationContentRequest);
    Link addLink(Long projectId, String jwtUserId, CreateLinkRequest createLinkRequest);
    void deleteLink(Long projectId, String jwtUserId, Long linkId);
    Set<Link> findLinksByProjectId(Long projectId, String jwtUserId);
    Information findContentByProjectId(Long projectId, String jwtUserId);
}
