package com.resourcebooking.server.service;

import com.resourcebooking.server.dto.request.ResourceRequest;
import com.resourcebooking.server.dto.response.ResourceResponse;
import com.resourcebooking.server.entity.Resource;
import com.resourcebooking.server.mapper.ResourceMapper;
import com.resourcebooking.server.repository.ResourceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.resourcebooking.server.exception.ResourceNotFoundException;

import java.util.List;

@Service
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final ResourceMapper resourceMapper;

    public ResourceService(ResourceRepository resourceRepository, ResourceMapper resourceMapper) {
        this.resourceRepository = resourceRepository;
        this.resourceMapper = resourceMapper;
    }

    @Transactional
    public ResourceResponse createResource(ResourceRequest request) {
        Resource resource = resourceMapper.toEntity(request);
        Resource savedResource = resourceRepository.save(resource);

        return resourceMapper.toResponse(savedResource, false);
    }

    @Transactional(readOnly = true)
    public ResourceResponse getResourceById(Long id) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));

        return resourceMapper.toResponse(resource, false);
    }

    @Transactional(readOnly = true)
    public List<ResourceResponse> getAllResources() {
        return resourceRepository.findAll()
                .stream()
                .map(resource -> resourceMapper.toResponse(resource, false))
                .toList();
    }

    @Transactional
    public ResourceResponse updateResource(Long id, ResourceRequest request) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));

        resourceMapper.updateEntity(resource, request);
        Resource updatedResource = resourceRepository.save(resource);

        return resourceMapper.toResponse(updatedResource, false);
    }

    @Transactional
    public void deleteResource(Long id) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));

        resourceRepository.delete(resource);
    }
}