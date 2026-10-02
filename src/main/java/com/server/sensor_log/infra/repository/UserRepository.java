package com.server.sensor_log.infra.repository;

import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.server.sensor_log.domain.model.user.User;

public interface UserRepository extends MongoRepository<User, String> {
    User findByEmail(String email);
    Boolean ExistsByEmail(String email);
}
