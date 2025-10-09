package com.example.client.project.dto;

import com.example.client.project.controller.AuthController;
import lombok.Getter;
import lombok.Setter;
import lombok.*;


@Getter
@Setter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {

    private String userName;

    private String emailAddress;

    private JwtResponse jwtResponse;

    private String refreshToken;


}



