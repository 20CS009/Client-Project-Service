package com.example.client.project.controller;

import com.example.client.project.dto.ApiResponse;
import com.example.client.project.dto.ClientRequest;
import com.example.client.project.dto.ClientResponseDto;
import com.example.client.project.entity.UserEntity;
import com.example.client.project.exception.ResourceNotFoundException;
import com.example.client.project.repository.UserRepository;
import com.example.client.project.security.CustomUserDetails;
import com.example.client.project.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;

    private UserEntity getCurrentUser(Authentication authentication) {

        if(authentication.getPrincipal() instanceof CustomUserDetails userDetails){
            return userDetails.getUser();
        }else {
            String email = authentication.getName(); // Spring Security gives the email or username
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ClientResponseDto>> addClient(
            @Valid @RequestBody ClientRequest request,
            Authentication authentication) {
        try {
//            String email = authentication.getName();
//            UserEntity currentUser = (UserEntity) authentication.getPrincipal();
            UserEntity currentUser=getCurrentUser(authentication);


            ApiResponse<ClientResponseDto> response = clientService.addClient(request,  currentUser);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw e; // Let GlobalExceptionHandler catch custom exceptions
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ClientResponseDto>>> getAllClients(Authentication authentication) {
        try {
//            UserEntity currentUser = (UserEntity) authentication.getPrincipal();
            UserEntity currentUser=getCurrentUser(authentication);


            ApiResponse<List<ClientResponseDto>> response = clientService.getAllClients(currentUser);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw e;
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ClientResponseDto>> getClientById(
            @PathVariable Long id,
            Authentication authentication) {
        try {
//            UserEntity currentUser = (UserEntity) authentication.getPrincipal();
            UserEntity currentUser=getCurrentUser(authentication);


            ApiResponse<ClientResponseDto> response = clientService.getClientById(id, currentUser);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw e;
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ClientResponseDto>> updateClient(
            @PathVariable Long id,
            @Valid @RequestBody ClientRequest request,
            Authentication authentication) {
        try {
//            UserEntity currentUser = (UserEntity) authentication.getPrincipal();
            UserEntity currentUser=getCurrentUser(authentication);



            ApiResponse<ClientResponseDto> response = clientService.updateClient(id, request, currentUser);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw e;
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteClient(
            @PathVariable Long id,
            Authentication authentication) {
        try {
//            UserEntity currentUser = (UserEntity) authentication.getPrincipal();
            UserEntity currentUser=getCurrentUser(authentication);


            ApiResponse<String> response = clientService.deleteClient(id, currentUser);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw e;
        }
    }
}