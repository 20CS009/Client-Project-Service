package com.example.client.project;


import com.example.client.project.dto.RegisterRequest;
import com.example.client.project.entity.UserEntity;
import com.example.client.project.repository.UserRepository;
import com.example.client.project.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void register_ShouldSaveEncodedPassword() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("John");
        request.setEmail("john@example.com");
        request.setPassword("123456");

        when(passwordEncoder.encode("123456")).thenReturn("encodedPass");
        when(userRepository.save(any())).thenReturn(new UserEntity());

        userService.register(request);

        verify(userRepository).save(any());
    }

}
