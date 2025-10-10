package com.example.client.project.controller;

import com.example.client.project.dto.*;
import com.example.client.project.security.JwtUtil;
import com.example.client.project.service.RefreshTokenService;
import com.example.client.project.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponseDto>> register(@Valid @RequestBody RegisterRequest request) {
        ApiResponse<UserResponseDto> response = userService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        ApiResponse<LoginResponse> apiResponse = userService.login(request);
        return ResponseEntity.status(HttpStatus.OK.value()).body(apiResponse);
    }

    // Allow both body-based and header-based refresh token
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<JwtResponse>> refreshToken(
            @RequestBody(required = false) TokenRefreshRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        String token = null;


        //Check data from body first
        if (request != null && request.getRefreshToken() != null) {
            token = request.getRefreshToken();

       //   If not found goes check header
        } else if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

       // Handle missing token
       if(token==null || token.isBlank()){
           return ResponseEntity.badRequest()
                   .body(ApiResponse.error("Refresh token missing." +
                           "Please provide it in body or Authorization " +
                           "header"));
       }

        ApiResponse<JwtResponse> response = refreshTokenService.refreshAccessToken(new TokenRefreshRequest(token));
        return ResponseEntity.ok(response);
    }
}
