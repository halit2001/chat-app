package com.chatapp.auth_service.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.chatapp.auth_service.model.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
}
