package com.example.client.project.service;

import com.example.client.project.dto.*;
import com.example.client.project.entity.Role;
import com.example.client.project.entity.UserEntity;
import com.example.client.project.exception.InvalidCredentialsException;
import com.example.client.project.exception.ResourceAlreadyExistsException;
import com.example.client.project.exception.ResourceException;
import com.example.client.project.exception.ResourceNotFoundException;
import com.example.client.project.repository.UserRepository;
import com.example.client.project.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final ModelMapper modelMapper;
    private final RefreshTokenService refreshTokenService;


    // Throw exception for registration
    public ApiResponse<UserResponseDto> register(RegisterRequest request) {
        // Check if user already exists by email
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ResourceAlreadyExistsException("User with email " + request.getEmail() + " already exists");
        }

        UserEntity user = new UserEntity();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);   // Default to User

        UserEntity savedUser = userRepository.save(user);
        UserResponseDto dto = modelMapper.map(user, UserResponseDto.class);
        return ApiResponse.<UserResponseDto>success("User registered successfully", dto);



    }

    // Return ApiResponse directly for login (no custom exceptions)
    public ApiResponse<LoginResponse> login(LoginRequest request) {
        try {
            Optional<UserEntity> optionalUser = userRepository.findByEmail(request.getEmail());

            // 1. Check if user exists
            if (optionalUser.isEmpty()) {
                throw new InvalidCredentialsException("Invalid email or password");
            }

            UserEntity userEntity = optionalUser.get();

            // 2. Check password
            boolean isValidUser = checkUserCredentials(userEntity, request);
            if (!isValidUser) {
                throw new InvalidCredentialsException("Invalid email or password"); // Return error ApiResponse
            }

            // 3. Generate JWT token
            String emailAddress = userEntity.getEmail();
            String userName = userEntity.getUsername();
            String jwt = jwtUtil.generateToken(emailAddress, userName);

            //  Generate refresh token
            var refreshToken = refreshTokenService.createRefreshToken(userEntity);

            // 4. Create response
            LoginResponse response = new LoginResponse();
            response.setUserName(userName);
            response.setEmailAddress(emailAddress);
            response.setJwtResponse(new JwtResponse(jwt, refreshToken.getToken(),userEntity.getEmail(),userEntity.getRole().name()));
            response.setRefreshToken(refreshToken.getToken());

            return ApiResponse.success("Login successful", response);
        } catch (Exception e) {
            throw new ResourceException("Login failed: " + e.getMessage()); //
        }
    }

    private boolean checkUserCredentials(UserEntity userEntity, LoginRequest request) {
        return passwordEncoder.matches(request.getPassword(), userEntity.getPassword());
    }

    // Throw exception for getUserByEmail (business logic)
    public ApiResponse<UserResponseDto> getUserByEmail(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        UserResponseDto dto = modelMapper.map(user, UserResponseDto.class);

        return ApiResponse.<UserResponseDto>success("User retrieved successfully", dto);
    }


    public ApiResponse<UserResponseDto> getUserById(Long id) {
        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        UserResponseDto dto = modelMapper.map(userEntity, UserResponseDto.class);

        return ApiResponse.<UserResponseDto>success("User retrieved successfully", dto);
    }
}