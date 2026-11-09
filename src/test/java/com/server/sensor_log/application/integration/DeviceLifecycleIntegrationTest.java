package com.server.sensor_log.application.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.server.sensor_log.application.controllers.device.dto.ClaimDeviceResponse;
import com.server.sensor_log.application.controllers.device.dto.CreateDeviceResponse;
import com.server.sensor_log.domain.model.device.ClaimStatus;
import com.server.sensor_log.domain.model.device.ConnectionStatus;
import com.server.sensor_log.domain.model.device.Device;
import com.server.sensor_log.domain.model.device.MetricsType;
import com.server.sensor_log.domain.model.user.User;
import com.server.sensor_log.application.integration.support.DeviceLifecycleScenario;
import com.server.sensor_log.application.integration.support.IntegrationMongoSupport;

@Testcontainers
class DeviceLifecycleIntegrationTest {

    @Container
    static final MongoDBContainer MONGO = IntegrationMongoSupport.MONGO;
    private final IntegrationMongoSupport mongo = new IntegrationMongoSupport();
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
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
        assertFalse(device.getBootstrapToken().isBlank());
        assertEquals("", device.getRedeemCode());
    }

    private void assertBootedDevice(CreateDeviceResponse created, String redeemCode) {
        Device device = mongo.deviceRepository().findById(created.serialNumber()).orElseThrow();

        assertEquals("redeem-1234", redeemCode);
        assertEquals(ClaimStatus.UNCLAIMED, device.getClaimStatus());
        assertEquals(ConnectionStatus.ONLINE, device.getConnectionStatus());
        assertEquals("", device.getBootstrapToken());
        assertFalse(redeemCode.equals(device.getRedeemCode()));
        org.junit.jupiter.api.Assertions.assertTrue(
                passwordEncoder.matches(redeemCode, device.getRedeemCode()));
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
        assertNotNull(claim.certificate());
        assertNotNull(device.getCertificate());
    }
}
