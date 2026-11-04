package com.server.sensor_log.application.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.server.sensor_log.application.controllers.device.dto.ClaimDeviceRequest;
import com.server.sensor_log.application.controllers.device.dto.FirstBootRequest;
import com.server.sensor_log.application.controllers.device.dto.NewDevice;
import com.server.sensor_log.application.controllers.user.dto.RegisterRequest;
import com.server.sensor_log.application.services.AuthenticationService;
import com.server.sensor_log.application.usecases.BootDeviceUseCase;
import com.server.sensor_log.application.usecases.ClaimDeviceUseCase;
import com.server.sensor_log.application.usecases.CreateDeviceUseCase;
import com.server.sensor_log.domain.model.device.ClaimStatus;
import com.server.sensor_log.domain.model.device.ConnectionStatus;
import com.server.sensor_log.domain.model.device.MetricsType;
import com.server.sensor_log.domain.model.device.RandomGenerator;
import com.server.sensor_log.domain.model.user.User;
import com.server.sensor_log.infra.repository.DeviceRepository;
import com.server.sensor_log.infra.repository.UserRepository;

@Testcontainers
class DeviceLifecycleIntegrationTest {

    @Container
    static final MongoDBContainer MONGO = new MongoDBContainer("mongo:7.0");

    private static MongoClient mongoClient;
    private static MongoTemplate mongoTemplate;
    private static DeviceRepository deviceRepository;
    private static UserRepository userRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final RandomGenerator identityGenerator = new FixedIdentityGenerator();

    @BeforeAll
    static void setUpMongoRepositories() {
        mongoClient = MongoClients.create(MONGO.getReplicaSetUrl());
        mongoTemplate = new MongoTemplate(
                new SimpleMongoClientDatabaseFactory(mongoClient, "sensor_log_integration"));
        MongoRepositoryFactory repositoryFactory = new MongoRepositoryFactory(mongoTemplate);
        deviceRepository = repositoryFactory.getRepository(DeviceRepository.class);
        userRepository = repositoryFactory.getRepository(UserRepository.class);
    }

    @AfterAll
    static void closeMongoClient() {
        mongoClient.close();
    }

    @BeforeEach
    void cleanDatabase() {
        deviceRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void createsBootsAndClaimsDeviceForRegisteredUser() throws Exception {
        CreateDeviceUseCase createDeviceUseCase = new CreateDeviceUseCase(
                deviceRepository, identityGenerator, passwordEncoder);
        BootDeviceUseCase bootDeviceUseCase = new BootDeviceUseCase(
                deviceRepository, identityGenerator, passwordEncoder);
        AuthenticationService authenticationService = new TestAuthenticationService(
                userRepository, passwordEncoder, identityGenerator);
        ClaimDeviceUseCase claimDeviceUseCase = new ClaimDeviceUseCase(
                deviceRepository, passwordEncoder, authenticationService);

        authenticationService.register(new RegisterRequest(
                "owner@example.com", "correct-password", "Owner"));
        User user = userRepository.findByEmail("owner@example.com");
        assertNotNull(user);

        var created = createDeviceUseCase.execute(
                new NewDevice(List.of(MetricsType.TEMPERATURE, MetricsType.HUMIDITY)));

        var createdDevice = deviceRepository.findById(created.serialNumber()).orElseThrow();
        assertEquals(ClaimStatus.UNCLAIMED, createdDevice.getClaimStatus());
        assertEquals(ConnectionStatus.OFFLINE, createdDevice.getConnectionStatus());
        assertEquals(0.0, createdDevice.getMetrics().get(MetricsType.TEMPERATURE));
        assertFalse(createdDevice.getBootstrapToken().isBlank());
        assertEquals("", createdDevice.getRedeemToken());

        var booted = bootDeviceUseCase.execute(new FirstBootRequest(
                created.serialNumber(), created.bootstrapToken()));

        var bootedDevice = deviceRepository.findById(created.serialNumber()).orElseThrow();
        assertEquals("redeem-1234", booted.redeemToken());
        assertEquals(ClaimStatus.UNCLAIMED, bootedDevice.getClaimStatus());
        assertEquals(ConnectionStatus.ONLINE, bootedDevice.getConnectionStatus());
        assertEquals("", bootedDevice.getBootstrapToken());
        assertTruePasswordIsStored(booted.redeemToken(), bootedDevice.getRedeemToken());

        var claim = claimDeviceUseCase.execute(new ClaimDeviceRequest(
                created.serialNumber(),
                user.getUserId(),
                booted.redeemToken(),
                "-----BEGIN CERTIFICATE REQUEST-----\nYWJj\n-----END CERTIFICATE REQUEST-----"));

        var claimedDevice = deviceRepository.findById(created.serialNumber()).orElseThrow();
        assertEquals(created.serialNumber(), claim.serialNumber());
        assertEquals(user.getUserId(), claim.ownerId());
        assertEquals(ClaimStatus.CLAIMED, claim.claimStatus());
        assertEquals(ClaimStatus.CLAIMED, claimedDevice.getClaimStatus());
        assertEquals(user.getUserId(), claimedDevice.getOwnerId());
        assertEquals(ConnectionStatus.ONLINE, claimedDevice.getConnectionStatus());
        assertNotNull(claim.certificate());
        assertNotNull(claimedDevice.getCertificate());
    }

    private void assertTruePasswordIsStored(String plainText, String encoded) {
        assertFalse(plainText.equals(encoded));
        org.junit.jupiter.api.Assertions.assertTrue(passwordEncoder.matches(plainText, encoded));
    }

    private static final class FixedIdentityGenerator implements RandomGenerator {
        @Override
        public String generateSerialNumber() {
            return "123456789012";
        }

        @Override
        public String generateBootstrapToken() {
            return "bootstrap-123";
        }

        @Override
        public String generateRedeemToken() {
            return "redeem-1234";
        }

        @Override
        public String generateUserId() {
            return "user-123";
        }
    }

    private static final class TestAuthenticationService extends AuthenticationService {
        private TestAuthenticationService(
                UserRepository userRepository,
                PasswordEncoder passwordEncoder,
                RandomGenerator randomGenerator) {
            super(null, userRepository, passwordEncoder, null, randomGenerator);
        }

        @Override
        public X509Certificate signCertificate(ClaimDeviceRequest request) {
            Instant issuedAt = Instant.parse("2026-01-01T00:00:00Z");
            X509Certificate certificate = mock(X509Certificate.class);
            try {
                when(certificate.getEncoded()).thenReturn(new byte[] { 1, 2, 3 });
                when(certificate.getSerialNumber()).thenReturn(BigInteger.ONE);
                when(certificate.getNotBefore()).thenReturn(Date.from(issuedAt));
                when(certificate.getNotAfter()).thenReturn(Date.from(issuedAt.plusSeconds(86400)));
            } catch (Exception exception) {
                throw new AssertionError("Could not configure test certificate", exception);
            }
            return certificate;
        }
    }
}
