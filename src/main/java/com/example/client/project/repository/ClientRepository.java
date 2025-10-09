package com.example.client.project.repository;

import com.example.client.project.entity.ClientEntity;
import com.example.client.project.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<ClientEntity, Long> {
    Optional<ClientEntity> findByEmail(String email);
    List<ClientEntity> findByUser(UserEntity user);

    //  Add this method to prevent duplicate client names per user
    Optional<ClientEntity> findByUserAndName(UserEntity user, String name);
}
