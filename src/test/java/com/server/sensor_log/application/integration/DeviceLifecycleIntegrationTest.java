package com.server.sensor_log.application.integration;

import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.server.sensor_log.application.controllers.device.dto.ClaimDeviceResponse;
import com.server.sensor_log.application.controllers.device.dto.CreateDeviceResponse;
import com.server.sensor_log.application.integration.support.DeviceLifecycleScenario;
import com.server.sensor_log.application.integration.support.IntegrationMongoSupport;
import com.server.sensor_log.domain.model.device.ClaimStatus;
import com.server.sensor_log.domain.model.device.ConnectionStatus;
import com.server.sensor_log.domain.model.device.Device;
import com.server.sensor_log.domain.model.device.MetricsType;
import com.server.sensor_log.domain.model.device.Role;
import com.server.sensor_log.domain.model.user.User;

@Testcontainers
class DeviceLifecycleIntegrationTest {

    @Container
    static final MongoDBContainer MONGO = IntegrationMongoSupport.MONGO;
    private final IntegrationMongoSupport mongo = new IntegrationMongoSupport();
    private final PasswordEncoder passwordEncoder = Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    private DeviceLifecycleScenario fixture;

    @BeforeAll
    static void setUpMongo() {
        IntegrationMongoSupport.setUpRepositories();
    }

    @AfterAll
    static void closeMongo() {
        IntegrationMongoSupport.closeMongoClient();
    }

    @BeforeEach
    void cleanDatabase() {
        mongo.clear();
        fixture = new DeviceLifecycleScenario(
                mongo.deviceRepository(), mongo.userRepository(), passwordEncoder);
    }

    @Test
    void createsBootsAndClaimsDeviceForRegisteredUser() throws Exception {
        User user = fixture.registerUser();
        CreateDeviceResponse created = fixture.createDevice();

        assertCreatedDevice(created);

        var booted = fixture.bootDevice(created);
        assertBootedDevice(created, booted.redeemCode());

        ClaimDeviceResponse claim = fixture.claimDevice(created, booted.redeemCode(), user);
        assertClaimedDevice(claim, user, created);
    }

    private void assertCreatedDevice(CreateDeviceResponse created) {
        Device device = mongo.deviceRepository().findById(created.serialNumber()).orElseThrow();

        assertEquals(ClaimStatus.UNCLAIMED, device.getClaimStatus());
        assertEquals(ConnectionStatus.OFFLINE, device.getConnectionStatus());
        assertEquals(0.0, device.getMetrics().get(MetricsType.TEMPERATURE));
        assertEquals(device.getRole(), Role.DEVICE);
        assertFalse(device.getBootstrapToken().isBlank());
        assertNull(device.getRedeemCode());
        assertNull(device.getOwnerId());
        assertNull(device.getCertificate());
    }

    private void assertBootedDevice(CreateDeviceResponse created, String redeemCode) {
        Device device = mongo.deviceRepository().findById(created.serialNumber()).orElseThrow();

        assertEquals(ClaimStatus.PENDING, device.getClaimStatus());
        assertEquals(ConnectionStatus.ONLINE, device.getConnectionStatus());
        assertFalse(device.getBootstrapToken().isBlank());
        assertTrue(passwordEncoder.matches(created.bootstrapToken(), device.getBootstrapToken()));
        assertEquals(redeemCode, device.getRedeemCode().toString());
    }

    private void assertClaimedDevice(
            ClaimDeviceResponse claim,
            User user,
            CreateDeviceResponse created) {
        Device device = mongo.deviceRepository().findById(created.serialNumber()).orElseThrow();

        assertEquals(created.serialNumber(), claim.serialNumber());
        assertEquals(user.getUserId(), claim.ownerId());
        assertEquals(ClaimStatus.CLAIMED, claim.claimStatus());
        assertEquals(ClaimStatus.CLAIMED, device.getClaimStatus());
        assertEquals(user.getUserId(), device.getOwnerId());
        assertEquals(ConnectionStatus.ONLINE, device.getConnectionStatus());
        assertNull(device.getCertificate());
    }
}
