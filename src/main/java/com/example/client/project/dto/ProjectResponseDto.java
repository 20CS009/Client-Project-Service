package com.example.client.project.dto;


import com.example.client.project.entity.ProjectStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ProjectResponseDto {
    private Long id;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private ProjectStatus status;
    private Long clientId;
    private String clientName;

    // Static factory method for easy conversion (optional but helpful)
    public static ProjectResponseDto fromEntity(com.example.client.project.entity.ProjectEntity projectEntity) {
        ProjectResponseDto dto = new ProjectResponseDto();
        dto.setId(projectEntity.getId());
        dto.setTitle(projectEntity.getTitle());
        dto.setDescription(projectEntity.getDescription());
        dto.setStartDate(projectEntity.getStartDate());
        dto.setEndDate(projectEntity.getEndDate());
        dto.setStatus(projectEntity.getStatus());

        // Safely flatten the relationship
        if (projectEntity.getClient() != null) {
            dto.setClientId(projectEntity.getClient().getId());
            dto.setClientName(projectEntity.getClient().getName());
        }

        return dto;
    }
}
