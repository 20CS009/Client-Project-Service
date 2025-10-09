package com.example.client.project.repository;

import com.example.client.project.entity.ClientEntity;
import com.example.client.project.entity.ProjectEntity;
import com.example.client.project.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<ProjectEntity, Long> {

    Optional<ProjectEntity> findByTitleAndClient(String title, ClientEntity client);

    List<ProjectEntity> findByClient_Id(Long clientId);
    List<ProjectEntity> findByUser(UserEntity user);

    //This method prevent duplicate projects
    boolean existsByClient_IdAndTitle(Long clientId, String title);
}
