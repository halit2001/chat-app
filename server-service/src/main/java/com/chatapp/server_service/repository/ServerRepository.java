package com.chatapp.server_service.repository;

import com.chatapp.server_service.model.Server;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServerRepository extends MongoRepository<Server, String> {
    Optional<Server> findByServerName(@NotBlank(message = "Server name cannot be blank.") String serverName);
}
