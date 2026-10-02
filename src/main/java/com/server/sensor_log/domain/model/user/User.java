package com.server.sensor_log.domain.model.user;
import com.server.sensor_log.domain.model.device.RandomGenerator;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.Getter;

@Document(collection = "users")
@Getter
@Setter
@RequiredArgsConstructor
public class User {
    @Id
    private final String userId;
    private final List<String> ownedDevices = new ArrayList<>();
    @Indexed(unique = true)
    private final String email;
    private String password;
    private Instant createdAt;

    public User(RandomGenerator idGenerator, String email, String password){
        this.userId = idGenerator.generateUserId();
        this.email = email;
        this.password = password;
        this.createdAt = Instant.now();
    }

    public void addDevice(String serialNumber) {
        this.ownedDevices.add(serialNumber);
    }
}