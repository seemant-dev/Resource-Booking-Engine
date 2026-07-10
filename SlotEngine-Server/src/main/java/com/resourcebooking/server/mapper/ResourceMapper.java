package com.resourcebooking.server.mapper;

import com.resourcebooking.server.dto.request.ResourceRequest;
import com.resourcebooking.server.dto.response.ResourceResponse;
import com.resourcebooking.server.entity.Resource;
import org.springframework.stereotype.Component;

@Component
public class ResourceMapper {

    public Resource toEntity(ResourceRequest request) {
        Resource resource = new Resource();
        resource.setName(request.getName());
        resource.setDescription(request.getDescription());
        return resource;
    }

    public ResourceResponse toResponse(Resource resource, boolean hasOpenSlots) {
        return new ResourceResponse(
                resource.getId(),
                resource.getName(),
                resource.getDescription(),
                hasOpenSlots,
                resource.getCreatedAt()
        );
    }

    public void updateEntity(Resource resource, ResourceRequest request) {
        resource.setName(request.getName());
        resource.setDescription(request.getDescription());
    }
}
