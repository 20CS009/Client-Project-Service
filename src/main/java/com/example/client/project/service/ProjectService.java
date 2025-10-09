package com.example.client.project.service;

import com.example.client.project.dto.ApiResponse;
import com.example.client.project.dto.ProjectRequest;
import com.example.client.project.dto.ProjectResponseDto;
import com.example.client.project.entity.ClientEntity;
import com.example.client.project.entity.ProjectEntity;
import com.example.client.project.entity.Role;
import com.example.client.project.entity.UserEntity;
import com.example.client.project.exception.AccessDeniedException;
import com.example.client.project.exception.ResourceAlreadyExistsException;
import com.example.client.project.exception.ResourceNotFoundException;
import com.example.client.project.repository.ClientRepository;
import com.example.client.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ClientRepository clientRepository;
    private final ModelMapper modelMapper;

    public ApiResponse<ProjectResponseDto> addProject(ProjectRequest request, UserEntity currentUser) {
        ClientEntity client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + request.getClientId()));

        if (currentUser.getRole() != Role.ADMIN && !client.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You don't own this client with id: " + request.getClientId());
        }

        if (projectRepository.findByTitleAndClient(request.getTitle(), client).isPresent()) {
            throw new ResourceAlreadyExistsException("Project with title '" + request.getTitle() + "' already exists for this client");
        }

        ProjectEntity project = new ProjectEntity();
        project.setTitle(request.getTitle());
        project.setDescription(request.getDescription());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setStatus(request.getStatus());
        project.setClient(client);
        project.setUser(currentUser);

        ProjectEntity savedProject = projectRepository.save(project);
        ProjectResponseDto dto = modelMapper.map(savedProject, ProjectResponseDto.class);
        return ApiResponse.success("Project added successfully", dto);
    }

    public ApiResponse<List<ProjectResponseDto>> getProjectsByClient(Long clientId, UserEntity currentUser) {
        ClientEntity client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + clientId));

        if (currentUser.getRole() != Role.ADMIN && !client.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Access denied - You don't have permission to access this client's projects");
        }

        List<ProjectEntity> projects = projectRepository.findByClient_Id(clientId);
        List<ProjectResponseDto> dtoList = projects.stream()
                .map(p -> modelMapper.map(p, ProjectResponseDto.class))
                .collect(Collectors.toList());

        return ApiResponse.success("Projects retrieved successfully", dtoList);
    }

    public ApiResponse<List<ProjectResponseDto>> getAllProjects(UserEntity currentUser) {
        List<ProjectEntity> projects = (currentUser.getRole() == Role.ADMIN)
                ? projectRepository.findAll()
                : projectRepository.findByUser(currentUser);

        List<ProjectResponseDto> dtoList = projects.stream()
                .map(p -> modelMapper.map(p, ProjectResponseDto.class))
                .collect(Collectors.toList());

        return ApiResponse.success("Projects retrieved successfully", dtoList);
    }

    public ApiResponse<ProjectResponseDto> updateProject(Long id, ProjectRequest request, UserEntity currentUser) {
        ProjectEntity project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));

        if (currentUser.getRole() != Role.ADMIN && !project.getClient().getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Access denied - You don't own the client for this project");
        }

        ClientEntity newClient = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + request.getClientId()));

        if (currentUser.getRole() != Role.ADMIN && !newClient.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You don't own the new client with id: " + request.getClientId());
        }

        project.setTitle(request.getTitle());
        project.setDescription(request.getDescription());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setStatus(request.getStatus());
        project.setClient(newClient);

        ProjectEntity updated = projectRepository.save(project);
        ProjectResponseDto dto = modelMapper.map(updated, ProjectResponseDto.class);
        return ApiResponse.success("Project updated successfully", dto);
    }

    public ApiResponse<String> deleteProject(Long id, UserEntity currentUser) {
        ProjectEntity project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));

        if (currentUser.getRole() != Role.ADMIN && !project.getClient().getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Access denied - You don't own this project");
        }

        projectRepository.delete(project);
        return ApiResponse.success("Project deleted successfully", null);
    }
}

