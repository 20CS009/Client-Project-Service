package com.example.client.project.service;

import com.example.client.project.dto.ApiResponse;
import com.example.client.project.dto.ClientRequest;
import com.example.client.project.dto.ClientResponseDto;
import com.example.client.project.entity.ClientEntity;
import com.example.client.project.entity.Role;
import com.example.client.project.entity.UserEntity;
import com.example.client.project.exception.AccessDeniedException;
import com.example.client.project.exception.ResourceAlreadyExistsException;
import com.example.client.project.exception.ResourceNotFoundException;
import com.example.client.project.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final ModelMapper modelMapper;

    //Return ClientEntity (let controller convert to DTO)
    public ApiResponse<ClientResponseDto> addClient(ClientRequest request, UserEntity user) {
        // Check for duplicate email
//        String email = authentication.getName();
        if (clientRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ResourceAlreadyExistsException("Client with email " + request.getEmail() + " already exists");
        }

        // Check for duplicate name for the same user
        if (clientRepository.findByUserAndName(user, request.getName()).isPresent()) {
            throw new ResourceAlreadyExistsException("Client with name '" + request.getName() + "' already exists for you");
        }

        ClientEntity client = new ClientEntity();
        client.setName(request.getName());
        client.setEmail(request.getEmail());
        client.setPhone(request.getPhone());
        client.setCompanyName(request.getCompanyName());
        client.setUser(user);



        ClientEntity savedClient=clientRepository.save(client);
        ClientResponseDto dto=modelMapper.map(savedClient, ClientResponseDto.class);

        return ApiResponse.success("Client added successfully", dto);

    }

    // Get All Clients CLientResponseDto

    public ApiResponse<List<ClientResponseDto>> getAllClients(UserEntity user) {
        List<ClientEntity> clients = (user.getRole() == Role.ADMIN)
                ? clientRepository.findAll()
                : clientRepository.findByUser(user);

        List<ClientResponseDto> dtoList = clients.stream()
                .map(c -> modelMapper.map(c, ClientResponseDto.class))
                .collect(Collectors.toList());

        return ApiResponse.success("Clients retrieved successfully", dtoList);
    }

    //  Get Client By Id from ClientResponseDto
    public ApiResponse<ClientResponseDto> getClientById(Long id, UserEntity user) {
        ClientEntity client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));

        if (user.getRole() != Role.ADMIN && !client.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Access denied - You don't have permission to access this client");
        }

        ClientResponseDto dto = modelMapper.map(client, ClientResponseDto.class);
        return ApiResponse.success("Client retrieved successfully", dto);
    }

    //  Return ClientEntity (let controller convert to DTO)
    public ApiResponse<ClientResponseDto> updateClient(Long id, ClientRequest request, UserEntity user) {
        ClientEntity client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));

        if (user.getRole() != Role.ADMIN && !client.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Access denied - You don't have permission to update this client");
        }

        // Check for duplicate email (if it's being changed)
        if (!client.getEmail().equals(request.getEmail()) &&
                clientRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ResourceAlreadyExistsException("Client with email " + request.getEmail() + " already exists");
        }

        // Check for duplicate name (if it's being changed)
        if (!client.getName().equals(request.getName()) &&
                clientRepository.findByUserAndName(user, request.getName()).isPresent()) {
            throw new ResourceAlreadyExistsException("Client with name '" + request.getName() + "' already exists for you");
        }

        client.setName(request.getName());
        client.setEmail(request.getEmail());
        client.setPhone(request.getPhone());
        client.setCompanyName(request.getCompanyName());

        ClientEntity updatedClient = clientRepository.save(client);
        ClientResponseDto dto = modelMapper.map(updatedClient, ClientResponseDto.class);

        return ApiResponse.success("Client updated successfully", dto);

//        return clientRepository.save(client);
        //  Let controller handle DTO conversion
    }

    //  Return void (let controller handle success response)
    public ApiResponse<String> deleteClient(Long id, UserEntity user) {
        ClientEntity client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));

        if (user.getRole() != Role.ADMIN && !client.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Access denied - You don't have permission to delete this client");
        }

        clientRepository.delete(client);
        return ApiResponse.success("Client deleted successfully", null);
    }
}
