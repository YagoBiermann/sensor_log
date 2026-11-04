package com.server.sensor_log.domain.model.device;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

class DeviceTest {
    private static final String SERIAL = "123456789012";
    private static final String BOOTSTRAP = "bootstrap";
    private static final MetricsType METRIC = MetricsType.TEMPERATURE;

    @Test
    void createInitializesAnUnclaimedOfflineDevice() {
        Device device = Device.create(SERIAL, BOOTSTRAP, "test device", new Location(1.2, 3.4),
                List.of(METRIC, MetricsType.HUMIDITY));

        assertAll(
                () -> assertEquals(SERIAL, device.getSerialNumber()),
                () -> assertEquals(Role.DEVICE, device.getRole()),
                () -> assertEquals("1.0.0", device.getFirmwareVersion()),
                () -> assertEquals(BOOTSTRAP, device.getBootstrapToken()),
                () -> assertEquals("", device.getRedeemToken()),
                () -> assertEquals(ClaimStatus.UNCLAIMED, device.getClaimStatus()),
                () -> assertEquals(ConnectionStatus.OFFLINE, device.getConnectionStatus()),
                () -> assertEquals(Map.of(METRIC, 0.0, MetricsType.HUMIDITY, 0.0), device.getMetrics()),
                () -> assertNotNull(device.getCreatedAt()));
    }

    @Test
    void restorePreservesAllPersistedStateAndCopiesMetrics() {
        Instant createdAt = Instant.parse("2025-01-01T00:00:00Z");
        Map<MetricsType, Double> metrics = new EnumMap<>(Map.of(METRIC, 22.5));
        Certificate certificate = new Certificate("pem", "serial", createdAt.plusSeconds(10), createdAt);

        Device device = Device.restore(SERIAL, "owner", certificate, createdAt.plusSeconds(5),
                ClaimStatus.CLAIMED, ConnectionStatus.ONLINE, new Location(1, 2), "description",
                metrics, "bootstrap-hash", "redeem-hash", createdAt);
        metrics.put(METRIC, 99.0);

        assertAll(
                () -> assertEquals("owner", device.getOwnerId()),
                () -> assertEquals(certificate, device.getCertificate()),
                () -> assertEquals(createdAt.plusSeconds(5), device.getClaimedAt()),
                () -> assertEquals(ClaimStatus.CLAIMED, device.getClaimStatus()),
                () -> assertEquals(ConnectionStatus.ONLINE, device.getConnectionStatus()),
                () -> assertEquals(22.5, device.getMetrics().get(METRIC)));
    }

    @Test
    void bootRejectsWrongSerialInvalidTokenAndAlreadyBootedDevice() {
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        when(encoder.matches(BOOTSTRAP, BOOTSTRAP)).thenReturn(true);
        Device device = Device.create(SERIAL, BOOTSTRAP, "description", new Location(0, 0), List.of(METRIC));

        assertThrows(SecurityException.class, () -> device.boot("000000000000", BOOTSTRAP, mock(RandomGenerator.class), encoder));
        when(encoder.matches("wrong", BOOTSTRAP)).thenReturn(false);
        assertThrows(SecurityException.class, () -> device.boot(SERIAL, "wrong", mock(RandomGenerator.class), encoder));

        RandomGenerator generator = mock(RandomGenerator.class);
        when(generator.generateRedeemToken()).thenReturn("redeem");
        when(encoder.encode("redeem")).thenReturn("redeem-hash");
        assertEquals("redeem", device.boot(SERIAL, BOOTSTRAP, generator, encoder));
        assertEquals(ConnectionStatus.ONLINE, device.getConnectionStatus());
        assertEquals("", device.getBootstrapToken());
        assertEquals("redeem-hash", device.getRedeemToken());
        assertThrows(SecurityException.class, () -> device.boot(SERIAL, BOOTSTRAP, generator, encoder));
    }

    @Test
    void claimValidatesStateAndTransitionsToClaimed() {
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        Device initialDevice = Device.create(SERIAL, BOOTSTRAP, "description", new Location(0, 0), List.of(METRIC));
        Certificate certificate = new Certificate("pem", "serial", Instant.now().plusSeconds(100), Instant.now());

        assertThrows(SecurityException.class,
                () -> initialDevice.claim("owner", "redeem", certificate, encoder));

        Device device = Device.restore(SERIAL, null, null, null, ClaimStatus.UNCLAIMED, ConnectionStatus.ONLINE,
                new Location(0, 0), "description", Map.of(METRIC, 0.0), "", "redeem-hash", Instant.now());
        when(encoder.matches("wrong", "redeem-hash")).thenReturn(false);
        assertThrows(SecurityException.class, () -> device.claim("owner", "wrong", certificate, encoder));
        when(encoder.matches("redeem", "redeem-hash")).thenReturn(true);
        when(encoder.encode("redeem")).thenReturn("new-hash");

        device.claim("owner", "redeem", certificate, encoder);

        assertTrue(device.isClaimed());
        assertEquals("owner", device.getOwnerId());
        assertEquals("new-hash", device.getRedeemToken());
        assertEquals(certificate, device.getCertificate());
        assertEquals(ConnectionStatus.ONLINE, device.getConnectionStatus());
        assertNotNull(device.getClaimedAt());
    }

    @Test
    void revokeAndMutatorsUpdateStateAndMetrics() {
        Device device = Device.restore(SERIAL, "owner", null, Instant.now(), ClaimStatus.CLAIMED,
                ConnectionStatus.ONLINE, new Location(0, 0), "old", Map.of(METRIC, 1.0),
                "", "redeem", Instant.now());

        device.revoke();
        device.setLocation(new Location(5, 6));
        device.changeDescription("new");
        device.updateMetrics(Map.of(METRIC, 42.0, MetricsType.PRESSURE, 1000.0));

        assertAll(
                () -> assertEquals(ClaimStatus.REVOKED, device.getClaimStatus()),
                () -> assertEquals(ConnectionStatus.OFFLINE, device.getConnectionStatus()),
                () -> assertEquals(new Location(5, 6), device.getLocation()),
                () -> assertEquals("new", device.getDescription()),
                () -> assertEquals(42.0, device.getMetrics().get(METRIC)),
                () -> assertEquals(1000.0, device.getMetrics().get(MetricsType.PRESSURE)));
        assertThrows(SecurityException.class, () -> device.revoke());
        assertThrows(SecurityException.class, () -> device.setRedeemToken("another"));
    }

    @Test
    void setRedeemTokenUpdatesAnUnclaimedDeviceAndRevokeRejectsUnclaimedDevice() {
        Device device = Device.restore(SERIAL, null, null, null, ClaimStatus.UNCLAIMED,
                ConnectionStatus.ONLINE, new Location(0, 0), "description", Map.of(METRIC, 0.0),
                "", "old-redeem", Instant.now());

        device.setRedeemToken("new-redeem");

        assertEquals("new-redeem", device.getRedeemToken());
        assertThrows(SecurityException.class, device::revoke);
    }

    @Test
    void metricsGetterReturnsDefensiveCopy() {
        Device device = Device.create(SERIAL, BOOTSTRAP, "description", new Location(0, 0), List.of(METRIC));
        Map<MetricsType, Double> metrics = device.getMetrics();
        metrics.put(METRIC, 99.0);

        assertEquals(0.0, device.getMetrics().get(METRIC));
    }
}
