package com.example.client.project.repository;

import com.example.client.project.entity.RefreshToken;
import com.example.client.project.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByUser(UserEntity user);

    void deleteByUser(UserEntity user);


}