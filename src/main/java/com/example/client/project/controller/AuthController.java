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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

//    @PostMapping("/register")
    //    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequest request) {
    //        try {
    //            ApiResponse<String> response = userService.register(request);
    //            return ResponseEntity.ok(response);
    //        } catch (Exception e) {
    //            return ResponseEntity.badRequest()
    //                    .body(ApiResponse.error("Registration failed: " + e.getMessage()));
    //        }
    //    }
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

//    @PostMapping("/refresh-token")
   @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<JwtResponse>> refreshToken(@RequestBody TokenRefreshRequest request) {
        ApiResponse<JwtResponse> response = refreshTokenService.refreshAccessToken(request);
        return ResponseEntity.ok(response);
    }

}



